package com.iemr.tm.service.schedule;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
	void testMarkAvailability() throws TMException {

		SpecialistAvailabilityDetail specialistAvailabilityDetail = new SpecialistAvailabilityDetail();
		Date date = new Date();
		specialistAvailabilityDetail.setConfiguredFromDate(date);
		specialistAvailabilityDetail.setConfiguredToDate(date);
		specialistAvailabilityDetail.setCreatedBy("createdBy");
		specialistAvailabilityDetail.setUserID(Long.valueOf(123));
		specialistAvailabilityDetail.setConfiguredFromTime(Timestamp.valueOf("2018-09-01 09:01:15"));
		specialistAvailabilityDetail.setConfiguredToTime(Timestamp.valueOf("2018-11-01 09:01:15"));
		when(specialistAvailabilityDetailRepo.save(Mockito.any())).thenReturn(specialistAvailabilityDetail);
		specialistAvailabilityRepo.findByConfiguredDateBetweenAndUserID(Mockito.any(), Mockito.any(), Mockito.any());
		specialistAvailabilityDetail = schedulingServiceImpl.markAvailability(specialistAvailabilityDetail);
		assertNotNull(specialistAvailabilityDetail);

	}

	@Test
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
	void testFetchavailability() {
		SpecialistAvailability specialistAvailability = new SpecialistAvailability();
		SpecialistInput2 specialistInput2 = new SpecialistInput2();
		specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(Mockito.any(), Mockito.any());
		specialistAvailability = schedulingServiceImpl.fetchavailability(specialistInput2);
		assertNotNull(specialistAvailability);
	}

	@Test
	void testGetslotsplit() {
		List<Slot> slotList = new ArrayList<>();
		String slot = "ABCDE";
		slotList = schedulingServiceImpl.getslotsplit(slot);
	}

	@Test
	void testBookSlot() throws TMException {
		SpecialistAvailability specialistAvailability = new SpecialistAvailability();
		SpecialistInput2 specialistInput2 = new SpecialistInput2();
		Date date = new Date();
		char status = 'A';
		String result = null;
		specialistInput2.setDate(date);
		specialistInput2.setFromTime(LocalTime.now());
		specialistInput2.setToTime(LocalTime.now().plusHours(1));
//		specialistAvailability.setTimeSlot("UABC");
//		int startslot=(specialistInput2.getFromTime().getHour() * 60) + (specialistInput2.getFromTime().getMinute()) / 5;
//		int endslot=(specialistInput2.getToTime().getHour() * 60) + (specialistInput2.getToTime().getMinute()) / 5;
		when(specialistAvailabilityRepo.save(Mockito.any())).thenReturn(specialistAvailability);
		when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(Mockito.any(), Mockito.any()))
				.thenReturn(specialistAvailability);
		result = schedulingServiceImpl.bookSlot(specialistInput2, status);
		assertNotNull(result);
		assertEquals("Booked", result);
	}

	@Test
	void testFetchmonthavailability() {
		SpecialistInput2 specialistInput2 = new SpecialistInput2();
		Integer year = 2023;
		Integer month = 11;
		Integer day = 26;
		List<SpecialistAvailability> slotdetails = new ArrayList<>();
		when(specialistAvailabilityRepo.findByMonthAndUserID(Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(slotdetails);
		slotdetails = schedulingServiceImpl.fetchmonthavailability(specialistInput2, year, month, day);
	}

	@Test
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
