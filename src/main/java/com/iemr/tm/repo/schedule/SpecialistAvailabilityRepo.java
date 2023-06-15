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
package com.iemr.tm.repo.schedule;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import com.iemr.tm.data.schedule.SpecialistAvailability;

@Repository
@RestResource(exported = false)
public interface SpecialistAvailabilityRepo extends CrudRepository<SpecialistAvailability, Integer> {

	List<SpecialistAvailability> findByConfiguredDateBetweenAndUserID(Date configuredFromDate, Date configuredToDate,
			Long userID);

	SpecialistAvailability findOneByConfiguredDateAndUserID(Date configuredFromDate, Long userID);

	@Query("select u from SpecialistAvailability u "
			+ "where u.userID=:userID "
			+ "and (year(u.configuredDate)=:year1) "
			+ "and (:month1 is null or :month1=0 or month(  u.configuredDate)=:month1) "
			+ "and (:day1 is null or :day1=0 or day(u.configuredDate)=:day1 ) ")
	List<SpecialistAvailability> findByMonthAndUserID(@Param("day1")Integer day,@Param("month1")Integer month, @Param("year1")Integer year, @Param("userID")Long userID);

	List<SpecialistAvailability> findByConfiguredDateAndUserIDIn(Date date, List<Long> userids);

}
