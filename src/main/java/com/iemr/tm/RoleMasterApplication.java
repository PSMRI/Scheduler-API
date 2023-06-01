package com.iemr.tm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import com.iemr.tm.utils.IEMRApplBeans;
import com.iemr.tm.utils.config.ConfigProperties;

@SpringBootApplication
public class RoleMasterApplication {

	public static void main(String[] args) {
		SpringApplication.run(roleMasterApplication, args);
	}

	@Bean
	public ConfigProperties configProperties() {
		return new ConfigProperties();
	}

	@Bean
	public IEMRApplBeans instantiateBeans() {
		return new IEMRApplBeans();
	}

	private static Class<RoleMasterApplication> roleMasterApplication = RoleMasterApplication.class;

	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(new Class[] { RoleMasterApplication.class });
	}
}
