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
package com.iemr.tm.service.schedule;

import java.util.List;

import com.iemr.tm.data.schedule.SpecialistAvailability;
import com.iemr.tm.data.schedule.SpecialistAvailabilityDetail;
import com.iemr.tm.data.schedule.SpecialistInput2;
import com.iemr.tm.data.specialist.Specialist;
import com.iemr.tm.utils.exception.TMException;

public interface SchedulingService {

	SpecialistAvailabilityDetail markAvailability(SpecialistAvailabilityDetail specialistInput) throws TMException;

	SpecialistAvailabilityDetail markUnavailability(SpecialistAvailabilityDetail specialistInput) throws TMException;

	SpecialistAvailability fetchavailability(SpecialistInput2 specialistInput);

	String bookSlot(SpecialistInput2 specialistInput,char status) throws TMException;

	List<SpecialistAvailability> fetchmonthavailability(SpecialistInput2 specialistInput,Integer year,Integer month,Integer day);

	List<Specialist> fetchAllAvailability(SpecialistInput2 specialistInput) throws TMException;

}
