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
package com.iemr.tm.controller.schedule;

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

import com.iemr.tm.data.schedule.SpecialistAvailability;
import com.iemr.tm.data.schedule.SpecialistAvailabilityDetail;
import com.iemr.tm.data.schedule.SpecialistInput2;
import com.iemr.tm.data.specialist.Specialist;
import com.iemr.tm.service.schedule.SchedulingService;
import com.iemr.tm.utils.mapper.InputMapper;
import com.iemr.tm.utils.response.OutputResponse;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/schedule", headers = "Authorization")
public class SchedulingController {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Autowired
	private SchedulingService schedulingService;

	@CrossOrigin()
	@ApiOperation(value = "mark availability of specialist", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = "markavailability", method = RequestMethod.POST)
	public String markavailability(@RequestBody String specialistInput1) {
		OutputResponse response = new OutputResponse();
		try {

			SpecialistAvailabilityDetail specialistInput = InputMapper.gson().fromJson(specialistInput1,
					SpecialistAvailabilityDetail.class);
			SpecialistAvailabilityDetail specialistAvailabilityDetail=schedulingService.markAvailability(specialistInput);
					response.setResponse(specialistAvailabilityDetail.toString());
			

		} catch (Exception e) {
			logger.error("exception occured while marking availability of specialist", e);
			response.setError(e);
		}
		return response.toString();
	}

	@CrossOrigin()
	@ApiOperation(value = "mark unavailability of specialist", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = "unmarkavailability", method = RequestMethod.POST)
	public String unmarkavailability(@RequestBody String specialistInput1) {
		OutputResponse response = new OutputResponse();
		try {

			SpecialistAvailabilityDetail specialistInput = InputMapper.gson().fromJson(specialistInput1,
					SpecialistAvailabilityDetail.class);
			schedulingService.markUnavailability(specialistInput);
					response.setResponse(specialistInput.toString());
			

		} catch (Exception e) {
			logger.error("exception occured while unmarking availability of specialist", e);
			response.setError(e);
		}
		return response.toString();
	}
	
	@CrossOrigin()
	@ApiOperation(value = "get available slots of specialist for a particular day", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = "getavailableSlot", method = RequestMethod.POST)
	public String getavailableSlot(@RequestBody String specialistInput1) {
		OutputResponse response = new OutputResponse();
		try {

			SpecialistInput2 specialistInput = InputMapper.gson().fromJson(specialistInput1,
					SpecialistInput2.class);
			SpecialistAvailability slotdetails=schedulingService.fetchavailability(specialistInput);
					response.setResponse(slotdetails.toString());
			

		} catch (Exception e) {
			logger.error("exception occured while fetching available slot", e);
			response.setError(e);
		}
		return response.toString();
	}
	
	@CrossOrigin()
	@ApiOperation(value = "get available slots of specialist for a particular day", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = {"/monthview/{year}","/monthview/{year}/{month}","/monthview/{year}/{month}/{day}"}, method = RequestMethod.POST)
	public String view(@RequestBody String specialistInput1,@PathVariable("year") Integer year,@PathVariable(value="month",required=false) Integer month,@PathVariable(value="day",required=false) Integer day) {
		OutputResponse response = new OutputResponse();
		try {

			SpecialistInput2 specialistInput = InputMapper.gson().fromJson(specialistInput1,
					SpecialistInput2.class);
			List<SpecialistAvailability> slotdetails=schedulingService.fetchmonthavailability(specialistInput, year, month,day);
					response.setResponse(slotdetails.toString());
			

		} catch (Exception e) {
			logger.error("exception occured while fetching available slot", e);
			response.setError(e);
		}
		return response.toString();
	}
	
	@CrossOrigin()
	@ApiOperation(value = "book available slots of specialist of a particular day", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = "bookSlot", method = RequestMethod.POST)
	public String bookSlot(@RequestBody String specialistInput1) {
		OutputResponse response = new OutputResponse();
		try {

			SpecialistInput2 specialistInput = InputMapper.gson().fromJson(specialistInput1,
					SpecialistInput2.class);
			String slotdetails=schedulingService.bookSlot(specialistInput,'B');
					response.setResponse(slotdetails);
			

		} catch (Exception e) {
			logger.error("exception occured while fetching available slot", e);
			response.setError(e);
		}
		return response.toString();
	}
	
	@CrossOrigin()
	@ApiOperation(value = "cancel booked slots of specialist of a particular day", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = "cancelBookedSlot", method = RequestMethod.POST)
	public String cancelBookedSlot(@RequestBody String specialistInput1) {
		OutputResponse response = new OutputResponse();
		try {

			SpecialistInput2 specialistInput = InputMapper.gson().fromJson(specialistInput1,
					SpecialistInput2.class);
			String slotdetails=schedulingService.bookSlot(specialistInput,'C');
					response.setResponse(slotdetails);
			

		} catch (Exception e) {
			logger.error("exception occured while fetching available slot", e);
			response.setError(e);
		}
		return response.toString();
	}
	
	@CrossOrigin()
	@ApiOperation(value = "get day view of particular specilization", consumes = "application/json", produces = "application/json")
	@RequestMapping(value = "getdayview", method = RequestMethod.POST)
	public String getdayview(@RequestBody String specialistInput1) {
		OutputResponse response = new OutputResponse();
		try {

			SpecialistInput2 specialistInput = InputMapper.gson().fromJson(specialistInput1,
					SpecialistInput2.class);
			List<Specialist> slotdetails=schedulingService.fetchAllAvailability(specialistInput);
					response.setResponse(slotdetails.toString());
			

		} catch (Exception e) {
			logger.error("exception occured while fetching available slot", e);
			response.setError(e);
		}
		return response.toString();
	}
}
