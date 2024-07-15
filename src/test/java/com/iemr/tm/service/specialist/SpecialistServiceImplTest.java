package com.iemr.tm.service.specialist;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import com.iemr.tm.data.specialist.MUser;
import com.iemr.tm.data.specialist.Specialist;
import com.iemr.tm.data.specialist.Specialization;
import com.iemr.tm.repo.specialist.SpecializationRepo;
import com.iemr.tm.utils.exception.TMException;
@ExtendWith(MockitoExtension.class)
class SpecialistServiceImplTest {
	
	@InjectMocks
	SpecialistServiceImpl specialistServiceImpl;
	@Mock
	SpecializationRepo specializationRepo;

	@Test
	@Description("Tests successful retrieval of a specialization by its ID (TC_GetSpecialization_Success_001)")
	void testGetspecialization() {
		specializationRepo.findByDeleted(false);
		
		List<Specialization> getspecialization = specialistServiceImpl.getspecialization();
		assertNotNull(getspecialization);
	}

	@Test
	@Description("Tests successful retrieval of a specialist user by ID (TC_GetSpecialistUser_Success_001)")
	void testGetspecialistUser() throws TMException {
		
		List<Specialist> specialistList = new ArrayList<>();
		Specialist specialist = new Specialist();
		specialist.setContactNo("");
		specialist.setEmail("");
		specialist.setFirstName(null);
		specialist.setGenderName(null);
		specialist.setLastName(null);
		specialist.setMiddleName(null);
		specialist.setParkingPlaceID(null);
		specialist.setProviderServiceMapID(null);
		specialist.setSpecialization(null);
		specialist.setTitleName(null);
		specialist.setUserID(null);
		specialist.setUserName(null);
		specialist.hashCode();
		specialistList.add(specialist);
		Long providerservicemapID = 1234L;
		Long specializationID = 222L;
		Long userID = 321L;
		Long parkingplaceID = 123456L;
		when(specializationRepo.getspecialistSP(Mockito.any(), Mockito.any())).thenReturn(Collections.emptyList());
		when(specializationRepo.getPPID(Mockito.any(), Mockito.any())).thenReturn(parkingplaceID);
		specialistList=specialistServiceImpl.getspecialistUser(providerservicemapID, specializationID, userID);
		assertNotNull(specialistList);
	}

	@Test
	@Description("Tests successful retrieval of a list of all specialists (TC_GetAllSpecialist_Success_001)")
	void testGetAllSpecialist() {
		List<Specialist> specialistList = new ArrayList<>();
		Long providerservicemapID=1234L;
		when(specializationRepo.getAllSPecialistForProvider(Mockito.any())).thenReturn(Collections.emptyList());
		specialistList=specialistServiceImpl.getAllSpecialist(providerservicemapID);
		assertNotNull(specialistList);
	}

	@Test
	@Description("Tests successful retrieval of information for an entity (TC_GetInfo_Success_001)")
	void testGetinfo() {
		MUser user = new MUser();
		Long userID=123456L;
		Object[] objlist = {new Object[]{123, "abc", "def", "ghi", "jkl", "mno", "fgh", "1234", "jhg", "1234567890", "bgfd"}};
		when(specializationRepo.getspecialistinfo(Mockito.any())).thenReturn(objlist);
		user=specialistServiceImpl.getinfo(userID);
		assertNotNull(user);
	}

}
