package com.iemr.tm.utils.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.iemr.tm.utils.response.OutputResponse;
import com.iemr.tm.utils.sessionobject.SessionObject;
import com.iemr.tm.utils.validator.Validator;

@Component
public class HTTPRequestInterceptor extends HandlerInterceptorAdapter {
	private Validator validator;

	Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	private SessionObject sessionObject;

	@Autowired
	public void setSessionObject(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}

	@Override

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception

	{
//		if(true){
//			return true;
//		}
		boolean status = true;
		logger.debug("In preHandle we are Intercepting the Request");
		String authorization = request.getHeader("Authorization");
		logger.debug("RequestURI::" + request.getRequestURI() + " || Authorization ::" + authorization
				+ " || method :: " + request.getMethod());
		if (!request.getMethod().equalsIgnoreCase("OPTIONS")) {
			try {
				String[] requestURIParts = request.getRequestURI().split("/");
				String requestAPI = requestURIParts[requestURIParts.length - 1];
				switch (requestAPI) {
				case "userAuthenticate":
				case "userAuthenticateNew":
				case "userAuthenticateV1":
				case "forgetPassword":
				case "setForgetPassword":
				case "changePassword":
				case "saveUserSecurityQuesAns":
				case "swagger-ui.html":
				case "ui":
				case "swagger-resources":
				case "api-docs":
					break;
				case "error":
					status = false;
					break;
				default:
					String remoteAddress = request.getHeader("X-FORWARDED-FOR");
					if (remoteAddress == null || remoteAddress.trim().length() == 0) {
						remoteAddress = request.getRemoteAddr();
					}
					validator.checkKeyExists(authorization, remoteAddress);
					break;
				}

			} catch (Exception e)

			{
				OutputResponse output = new OutputResponse();
				output.setError(e);
				response.getOutputStream().print(output.toString());
				response.setContentType(MediaType.APPLICATION_JSON.toString());
				response.setContentLength(output.toString().length());
				response.setHeader("Access-Control-Allow-Origin", "*");
				status = false;
			}

		}

		return status;

	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object object, ModelAndView model)

			throws Exception

	{

		try

		{

			logger.debug("In postHandle we are Intercepting the Request");

			String authorization = request.getHeader("Authorization");

			logger.debug("RequestURI::" + request.getRequestURI() + " || Authorization ::" + authorization);

			if (authorization != null)

			{

				sessionObject.updateSessionObject(authorization, sessionObject.getSessionObject(authorization));

			}

		} catch (Exception e)

		{

			logger.debug("postHandle failed with error " + e.getMessage());

		}

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object object, Exception arg3)
			throws Exception {
		logger.debug("In afterCompletion Request Completed");
	}
}
