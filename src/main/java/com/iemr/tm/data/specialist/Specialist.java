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
package com.iemr.tm.data.specialist;

import javax.persistence.Transient;

import com.google.gson.annotations.Expose;
import com.iemr.tm.data.schedule.SpecialistAvailability;
import com.iemr.tm.utils.mapper.OutputMapper;

import lombok.Data;

@Data
public class Specialist {

	@Expose
	private String userName;


	@Expose
	private Long userID;

	@Expose
	private String titleName;
	
	@Expose
	private String firstName;

	@Expose
	private String middleName;

	@Expose
	private String lastName;
	
	@Expose
	private String genderName;
	
	@Expose
	private String email;
	
	@Expose
	private String contactNo;

	@Expose
	private Long specializationID;
	
	@Expose
	private String specialization;

	@Transient
	@Expose
	private Long providerServiceMapID;
	
	@Transient
	@Expose
	private Long parkingPlaceID;
	
	@Transient
	@Expose
	private	SpecialistAvailability specialistAvailability;

	@Transient
	private OutputMapper outputMapper = new OutputMapper();

	@Override
	public String toString() {
		return outputMapper.gson().toJson(this);
	}

	public Specialist() {

	}

	public Specialist(String userName, Long userID, String firstName, String middleName, String lastName,
			Long specializationID,String genderName,String titleName,String email,String contactNo ,String specialization) {
		this.userName = userName;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.userID = userID;
		this.specializationID = specializationID;
		this.titleName=titleName;
		this.genderName=genderName;
		this.contactNo=contactNo;
		this.email=email;
		this.specialization=specialization;

	}

}
