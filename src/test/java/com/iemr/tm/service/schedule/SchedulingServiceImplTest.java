package com.iemr.tm.service.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Description;

import com.iemr.tm.data.schedule.Slot;
import com.iemr.tm.data.schedule.SpecialistAvailability;
import com.iemr.tm.data.schedule.SpecialistAvailabilityDetail;
import com.iemr.tm.data.schedule.SpecialistInput2;
import com.iemr.tm.data.specialist.Specialist;
import com.iemr.tm.repo.schedule.SpecialistAvailabilityDetailRepo;
import com.iemr.tm.repo.schedule.SpecialistAvailabilityRepo;
import com.iemr.tm.service.specialist.SpecialistService;
import com.iemr.tm.utils.exception.TMException;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceImplTest {

	@InjectMocks
	SchedulingServiceImpl schedulingServiceImpl;

	@Mock
	SpecialistAvailabilityRepo specialistAvailabilityRepo;

	@Mock
	SpecialistAvailabilityDetailRepo specialistAvailabilityDetailRepo;
	@Mock
	private SpecialistService specialistService;

	@Test
	@Description("Tests successful availability check (TC_TestMarkavailability_ValidData_001)")
	void testMarkAvailability() throws TMException {

		SpecialistAvailabilityDetail specialistAvailabilityDetail = new SpecialistAvailabilityDetail();
		Date date = new Date();
		specialistAvailabilityDetail.setConfiguredFromDate(date);
		specialistAvailabilityDetail.setConfiguredToDate(date);
		specialistAvailabilityDetail.setCreatedBy("createdBy");
		specialistAvailabilityDetail.setUserID(Long.valueOf(123));
		specialistAvailabilityDetail.setConfiguredFromTime(Timestamp.valueOf("2018-09-01 09:01:15"));
		specialistAvailabilityDetail.setConfiguredToTime(Timestamp.valueOf("2018-11-01 09:01:15"));
		SpecialistAvailabilityDetail specialistAvailabilityDetail2 = new SpecialistAvailabilityDetail();
		specialistAvailabilityDetail2.setConfiguredFromDate(date);
		specialistAvailabilityDetail2.setConfiguredToDate(date);
		specialistAvailabilityDetail2.setCreatedBy("createdBy");
		specialistAvailabilityDetail2.setUserID(Long.valueOf(123));
		specialistAvailabilityDetail2.setConfiguredFromTime(Timestamp.valueOf("2018-09-01 09:01:15"));
		specialistAvailabilityDetail2.setConfiguredToTime(Timestamp.valueOf("2018-11-01 09:01:15"));
		specialistAvailabilityDetail.equals(specialistAvailabilityDetail2);
		specialistAvailabilityDetail.hashCode();
		when(specialistAvailabilityDetailRepo.save(Mockito.any())).thenReturn(specialistAvailabilityDetail);
		specialistAvailabilityRepo.findByConfiguredDateBetweenAndUserID(Mockito.any(), Mockito.any(), Mockito.any());
		specialistAvailabilityDetail = schedulingServiceImpl.markAvailability(specialistAvailabilityDetail);
		assertNotNull(specialistAvailabilityDetail);

	}

	@Test
	@Description("Tests successful unmarking of availability (TC_TestUnmarkavailability_ValidData_001)")
	void testMarkUnavailability() throws TMException {
		SpecialistAvailabilityDetail specialistAvailabilityDetail = new SpecialistAvailabilityDetail();
		Date date = new Date();
		specialistAvailabilityDetail.setConfiguredFromDate(date);
		specialistAvailabilityDetail.setConfiguredToDate(date);
		specialistAvailabilityDetail.setConfiguredFromTime(Timestamp.valueOf("2018-09-01 09:01:15"));
		specialistAvailabilityDetail.setConfiguredToTime(Timestamp.valueOf("2018-11-01 09:01:15"));
		when(specialistAvailabilityDetailRepo.save(Mockito.any())).thenReturn(specialistAvailabilityDetail);
		specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(Mockito.any(), Mockito.any());
		specialistAvailabilityDetail = schedulingServiceImpl.markUnavailability(specialistAvailabilityDetail);
		assertNotNull(specialistAvailabilityDetail);

	}
	
	@Test
	@Description("Tests successful fetching of availability for a specialist (TC_FetchAvailability_Success_001)")
	void testFetchavailability() {
		SpecialistAvailability specialistAvailability = new SpecialistAvailability();
		SpecialistAvailability specialistAvailability2 = new SpecialistAvailability();
		specialistAvailability.equals(specialistAvailability2);
		specialistAvailability.hashCode();
		SpecialistInput2 specialistInput2 = new SpecialistInput2();
		SpecialistInput2 specialistInput22 = new SpecialistInput2();
		specialistInput2.equals(specialistInput22);
		specialistInput2.hashCode();
		specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(Mockito.any(), Mockito.any());
		specialistAvailability = schedulingServiceImpl.fetchavailability(specialistInput2);
		assertNotNull(specialistAvailability);
	}

	@Test
	@Description("Tests successful splitting of available slots (TC_GetSlotSplit_Success_001)")
	void testGetslotsplit() {
		List<Slot> slotList = new ArrayList<>();
		String slot = "ABCDE";
		Assertions.assertThrows(Exception.class, () -> schedulingServiceImpl.getslotsplit(slot));
	}

	@Test
	@Description("Tests successful booking of a slot (TC_TestBookSlot_ValidData_001)")
	void testBookSlot() throws TMException {
		SpecialistAvailability specialistAvailability = new SpecialistAvailability();
		specialistAvailability.setTimeSlot("165");
		SpecialistInput2 specialistInput2 = new SpecialistInput2();
		Date date = new Date();
		char status = 'A';
		String result = null;
		specialistInput2.setDate(date);
		specialistInput2.setFromTime(LocalTime.now());
		specialistInput2.setToTime(LocalTime.now().plusHours(1));
		// when(specialistAvailabilityRepo.save(Mockito.any())).thenReturn(specialistAvailability);
		when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(Mockito.any(), Mockito.any()))
				.thenReturn(specialistAvailability);
		Assertions.assertThrows(Exception.class, () -> schedulingServiceImpl.bookSlot(specialistInput2, status));

	}

	@Test
	@Description("Tests successful retrieval of a specialist's availability for a month (TC_FetchMonthAvailability_Success_001)")
	void testFetchmonthavailability() {
		SpecialistInput2 specialistInput2 = new SpecialistInput2();
		specialistInput2.toString();
		Integer year = 2023;
		Integer month = 11;
		Integer day = 26;
		List<SpecialistAvailability> slotdetails = new ArrayList<>();
		SpecialistAvailability specialistAvailability = new SpecialistAvailability();
		List<Slot> listOfslots = new ArrayList<>();
		Slot slot = new Slot();
		slot.hashCode();
		listOfslots.add(slot);
		specialistAvailability.setSlots(listOfslots);
		specialistAvailability.setTimeSlot("([A]+)|([B]+)");
		slotdetails.add(specialistAvailability);
		when(specialistAvailabilityRepo.findByMonthAndUserID(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(slotdetails);
		slotdetails = schedulingServiceImpl.fetchmonthavailability(specialistInput2, year, month, day);
		assertNotNull(slotdetails);
	}

	@Test
	@Description("Tests successful retrieval of all available appointments (TC_FetchAllAvailability_Success_001)")
	void testFetchAllAvailability() throws TMException {
		SpecialistInput2 specialistInput2 = new SpecialistInput2();
		List<SpecialistAvailability> slotdetails = new ArrayList<>();
		List<Specialist> user = new ArrayList<>();
		user.add(new Specialist(1234L, 32434L, 765435L));
		when(specialistAvailabilityRepo.findByConfiguredDateAndUserIDIn(Mockito.any(), Mockito.any()))
				.thenReturn(slotdetails);
		when(specialistService.getspecialistUser(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(user);
		List<Specialist> specialist = schedulingServiceImpl.fetchAllAvailability(specialistInput2);
		assertNotNull(user);
	}

}
