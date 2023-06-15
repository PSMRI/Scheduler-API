/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.tm.controller.specialist;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.tm.data.specialist.M_User;
import com.iemr.tm.data.specialist.Specialist;
import com.iemr.tm.data.specialist.Specialization;
import com.iemr.tm.service.specialist.SpecialistService;
import com.iemr.tm.utils.response.OutputResponse;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/specialist", headers = "Authorization")
public class SpecialistController {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	private SpecialistService specialistService;

	@CrossOrigin()
	@ApiOperation(value = "fetch all specialization", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = "masterspecialization", method = RequestMethod.POST)
	public String markavailability() {
		OutputResponse response = new OutputResponse();
		try {
			List<Specialization> specialization = specialistService.getspecialization();
			response.setResponse(specialization.toString());

		} catch (Exception e) {
			logger.error("exception occured while fetching specialization", e);
			response.setError(e);
		}
		return response.toString();
	}

	@CrossOrigin()
	@ApiOperation(value = "fetch  specialist", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = "getSpecialist", method = RequestMethod.POST)
	public String getSpecialist(@RequestBody Specialist specialist) {
		OutputResponse response = new OutputResponse();
		try {
			List<Specialist> specialization = specialistService.getspecialistUser(specialist.getProviderServiceMapID(),
					specialist.getSpecializationID(), specialist.getUserID());
			response.setResponse(specialization.toString());

		} catch (Exception e) {
			logger.error("exception occured while fetching specialization", e);
			response.setError(e);
		}
		return response.toString();
	}

	@CrossOrigin()
	@ApiOperation(value = "fetch  specialist", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = "info/{userID}", method = RequestMethod.GET)
	public String info(@PathVariable("userID") Long userID) {
		OutputResponse response = new OutputResponse();
		try {

			M_User user = specialistService.getinfo(userID);

			response.setResponse(user.toString());

		} catch (Exception e) {
			logger.error("exception occured while fetching specialization", e);
			response.setError(e);
		}
		return response.toString();
	}

	@CrossOrigin()
	@ApiOperation(value = "fetch  specialist", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = "getSpecialistAll", method = RequestMethod.POST)
	public String getSpecialistAll(@RequestBody Specialist specialist) {
		OutputResponse response = new OutputResponse();
		try {
			List<Specialist> specialization = specialistService.getAllSpecialist(specialist.getProviderServiceMapID());
			response.setResponse(specialization.toString());

		} catch (Exception e) {
			logger.error("exception occured while fetching all specialist", e);
			response.setError(e);
		}
		return response.toString();
	}

}
