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
package com.iemr.tm.service.specialist;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iemr.tm.data.specialist.M_User;
import com.iemr.tm.data.specialist.Specialist;
import com.iemr.tm.data.specialist.Specialization;
import com.iemr.tm.repo.specialist.SpecializationRepo;
import com.iemr.tm.utils.exception.TMException;

@Service
public class SpecialistServiceImpl implements SpecialistService {

	@Autowired
	SpecializationRepo specializationRepo;

	@Override
	public List<Specialization> getspecialization() {
		
		return specializationRepo.findByDeleted(false);
	}

	@Override
	public List<Specialist> getspecialistUser(Long providerservicemapID, Long specializationID, Long userID)
			throws TMException {
		
		// List<Object[]>
		// obj=specializationRepo.getspecialist(providerservicemapID,

		Long parkingplaceID = specializationRepo.getPPID(providerservicemapID, userID);
		if (parkingplaceID == null) {
			throw new TMException("User not mapped to a parking place");
		}
		List<Object[]> obj = specializationRepo.getspecialistSP(specializationID, parkingplaceID);
		List<Specialist> specialistList = new ArrayList<>();

		if (obj.size() > 0) {
			for (Object[] action : obj) {
				specialistList.add(new Specialist((String) action[0], ((Number) action[1]).longValue(),
						(String) action[2], (String) action[3], (String) action[4], ((Number) action[5]).longValue(),
						(String) action[6], (String) action[7], (String) action[8], (String) action[9],
						(String) action[10]));
			}
		}
		return specialistList;
	}

	// for patient app, 13-08-2020
	@Override
	public List<Specialist> getAllSpecialist(Long providerservicemapID) {
		List<Specialist> specialistList = new ArrayList<>();
		List<Object[]> obj = specializationRepo.getAllSPecialistForProvider(providerservicemapID);
		if (obj.size() > 0) {
			for (Object[] action : obj) {
				specialistList.add(new Specialist(null, ((Number) action[0]).longValue(), (String) action[1],
						(String) action[2], (String) action[3], ((Number) action[6]).longValue(), null, null, null,
						null, (String) action[4]));
			}
		}
		return specialistList;
	}

	@Override
	public M_User getinfo(Long userID) {
		
		M_User user = new M_User();
		Object[] objlist = specializationRepo.getspecialistinfo(userID);

		if (objlist != null && objlist.length > 0) {
			Object[] obj = (Object[]) objlist[0];
			user.setUserID(Long.valueOf((Integer) obj[0]));
			user.setTitleName((String) obj[1]);
			user.setFirstName((String) obj[2]);
			user.setMiddleName((String) obj[3]);
			user.setLastName((String) obj[4]);
			user.setGender((String) obj[6]);
			user.setSpecialization((String) obj[8]);
			user.setContactNumber((String) obj[9]);
			user.setEmailID((String) obj[10]);
		}

		return user;
	}

}
