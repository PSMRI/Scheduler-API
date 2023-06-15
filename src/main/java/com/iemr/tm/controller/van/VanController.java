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
package com.iemr.tm.controller.van;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.tm.data.van.M_Van;
import com.iemr.tm.service.van.VanService;
import com.iemr.tm.utils.response.OutputResponse;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/van", headers = "Authorization")
public class VanController {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	private VanService vanService;

	@CrossOrigin()
	@ApiOperation(value = "fetch all specialization", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = "getvan/{vanid}", method = RequestMethod.GET)
	public String markavailability(@PathVariable("vanid") Integer vanid) {
		OutputResponse response = new OutputResponse();
		try {
			M_Van van = vanService.getvan(vanid);
			if (van == null) {
				van = new M_Van();
			}
			response.setResponse(van.toString());

		} catch (Exception e) {
			logger.error("exception occured while fetching specialization", e);
			response.setError(e);
		}
		return response.toString();
	}

}
