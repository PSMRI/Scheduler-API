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
package com.iemr.tm.repo.specialist;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import com.iemr.tm.data.specialist.Specialization;

@Repository
@RestResource(exported = false)
public interface SpecializationRepo extends JpaRepository<Specialization, Integer> {

	List<Specialization> findByDeleted(boolean b);

	@Query(value = "select usr.UserName,usr.UserID,usr.FirstName,usr.MiddleName ,"
			+ " usr.LastName, usrspecmap.SpecializationID  from m_userparkingplacemap usrpp "
			+ "	left join	m_userspecializationmapping usrspecmap	on usrspecmap.UserID=	usrpp.UserID and usrspecmap.deleted=false	and usrspecmap.SpecializationID=:specializationID	"
			+ "left join	m_user usr	on usr.UserID=	usrpp.UserID and usr.Deleted=false	"
			+ "left join	m_userservicerolemapping usrm	on usrm.UserID=	usrpp.UserID and usrm.ProviderServiceMapID=:providerservicemapID	and usrm.Deleted=false	"
			+ "join m_role	role on role.RoleID=	usrm.RoleID and role.Deleted=false	"
			+ "join m_servicerolescreenmapping	srm on srm.RoleID=	usrm.RoleID and srm.Deleted=false	"
			+ "join m_screen	scrn on scrn.ScreenID=	srm.ScreenID and scrn.ScreenName=\"TC Specialist\"	and scrn.Deleted=false	"
			+ " where usrpp.ProviderServiceMapID=:providerservicemapID	and usrpp.Deleted=false", nativeQuery = true)
	List<Object[]> getspecialist(@Param("specializationID") Long specializationID,
			@Param("providerservicemapID") Long providerservicemapID);

	@Query(value = "call PR_FetchSpecialist(:specializationID,:ppID)", nativeQuery = true)
	List<Object[]> getspecialistSP(@Param("specializationID") Long specializationID, @Param("ppID") Long ppID);

	@Query(value = "select u.ParkingPlaceID from m_userparkingplacemap u where u.deleted=false and u.userid=:uid and u.ProviderServiceMapID=:psmid limit 1", nativeQuery = true)
	Long getPPID(@Param("psmid") Long providerservicemapID, @Param("uid") Long userID);

	@Query(value = "select u.userid,title.TitleName ,u.FirstName,u.MiddleName,u.LastName,u.GenderID,gen.GenderName,spec.SpecializationID,spec.Specialization,u.ContactNo,u.EmailID from m_user u left join m_gender gen on u.GenderID=gen.GenderID left join m_title title on title.TitleID=u.TitleID left join m_userspecializationmapping uspec on uspec.UserID=u.UserID  and uspec.Deleted=false left join m_specialization spec on spec.SpecializationID=uspec.SpecializationID  where u.userid=:userID limit 1", nativeQuery = true)
	Object[] getspecialistinfo(@Param("userID") Long userID);

	@Query(value = "SELECT DISTINCT t1.UserID, t1.FirstName, t1.MiddleName, t1.LastName, t4.Specialization, "
			+ "t2.ProviderServiceMapID, t3.SpecializationID FROM m_user t1 "
			+ " INNER JOIN m_userservicerolemapping t2 ON t1.UserID = t2.UserID"
			+ " INNER JOIN m_userspecializationmapping t3 ON t1.UserID = t3.UserID"
			+ " INNER JOIN m_specialization t4 ON t3.SpecializationID = t4.SpecializationID"
			+ " where t2.ProviderServiceMapID =:psmid ", nativeQuery = true)
	List<Object[]> getAllSPecialistForProvider(@Param("psmid") Long providerservicemapID);

}
