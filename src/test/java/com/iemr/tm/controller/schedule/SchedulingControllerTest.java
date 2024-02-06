package com.iemr.tm.controller.schedule;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.rmi.ConnectIOException;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.iemr.tm.data.schedule.SpecialistAvailability;
import com.iemr.tm.data.schedule.SpecialistAvailabilityDetail;
import com.iemr.tm.data.schedule.SpecialistInput2;
import com.iemr.tm.service.schedule.SchedulingService;
import com.iemr.tm.utils.exception.TMException;
import com.iemr.tm.utils.response.OutputResponse;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
@ExtendWith(MockitoExtension.class)
class SchedulingControllerTest {
	@InjectMocks
	SchedulingController schedulingController;
	@Mock
	private SchedulingService schedulingService;
	@Mock
	ObjectMapper mapper;

	@Test
	void testMarkavailability() throws TMException, ParseException {
		SpecialistAvailabilityDetail specialistAvailabilityDetail = new SpecialistAvailabilityDetail();
		specialistAvailabilityDetail.setConfiguredFromDate(new Date(0));
		specialistAvailabilityDetail.setConfiguredFromTime(Timestamp.valueOf("2011-10-02 18:48:05.123"));
		specialistAvailabilityDetail.setConfiguredToDate(Timestamp.valueOf("2011-10-02 18:48:05.123"));
		specialistAvailabilityDetail.setConfiguredToTime(Timestamp.valueOf("2011-10-02 18:48:05.123"));
		specialistAvailabilityDetail.setCreatedBy(null);
		specialistAvailabilityDetail.setCreatedDate(Timestamp.valueOf("2011-10-02 18:48:05.123"));
		specialistAvailabilityDetail.setDeleted(null);
		specialistAvailabilityDetail.setExcludeDays(null);
		specialistAvailabilityDetail.setIsAvailability(false);
		specialistAvailabilityDetail.setLastModDate(Timestamp.valueOf("2011-10-02 18:48:05.123"));
		specialistAvailabilityDetail.setModifiedBy(null);
		specialistAvailabilityDetail.setOutputMapper(null);
		specialistAvailabilityDetail.setProcessed(null);
		specialistAvailabilityDetail.setSpecialistAvailabilityDetailID(9l);
		specialistAvailabilityDetail.setUserID(8l);
		String req = new Gson().toJson(specialistAvailabilityDetail);
		//when(schedulingService.markAvailability(any())).thenReturn(specialistAvailabilityDetail);
		String resp = schedulingController.markavailability(req);
		JSONParser parser = new JSONParser();  
		JSONObject json = (JSONObject) parser.parse(resp); 
		Integer response = (Integer) json.get("statusCode");
		assertTrue(5005 != response);
	}
	
	@Test
	void testMarkavailabilityException() throws TMException, ParseException {
		SpecialistAvailabilityDetail specialistAvailabilityDetail = new SpecialistAvailabilityDetail();
		String req = new Gson().toJson(specialistAvailabilityDetail);
		when(schedulingService.markAvailability(any())).thenThrow(NullPointerException.class);
		String resp = schedulingController.markavailability(req);
		JSONParser parser = new JSONParser();  
		JSONObject json = (JSONObject) parser.parse(resp); 
		Integer response = (Integer) json.get("statusCode");
		assertTrue(5005 == response);
	
	}

	@Test
	void testUnmarkavailability() {
		SpecialistAvailabilityDetail specialistAvailabilityDetail = new SpecialistAvailabilityDetail();
		String req = new Gson().toJson(specialistAvailabilityDetail);
		String resp = schedulingController.unmarkavailability(req);
		assertTrue(!resp.isEmpty());
	}
	@Test
	void testUnmarkavailabilityNullReq() throws ParseException {
		String resp = schedulingController.unmarkavailability(null);
		JSONParser parser = new JSONParser();  
		JSONObject json = (JSONObject) parser.parse(resp); 
		Integer response = (Integer) json.get("statusCode");
		assertTrue(5005 == response);
	}

	@Test
	void testGetavailableSlot() throws ParseException {
		String resp = schedulingController.getavailableSlot(null);
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(resp);
		Assertions.assertTrue(json.getAsString("status").contains("Failed"));
	}

	@Test
	void testView() throws ParseException {
		String resp = schedulingController.view(null, null, null, null);
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(resp);
		Assertions.assertEquals("Success", json.getAsString("status"));
		
	}
	@Test
	void testViewException() throws ParseException {
		when(schedulingService.fetchmonthavailability(any(), any(),
				any(), any())).thenThrow(NullPointerException.class);
		String resp = schedulingController.view(null, null, null, null);
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(resp);
		Assertions.assertTrue(json.getAsString("status").contains("Failed"));
		
	}

	@Test
	void testBookSlot() throws ParseException {
		String resp = schedulingController.bookSlot(null);
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(resp);
		Assertions.assertTrue(json.getAsString("status").contains("Failed"));
	}

	@Test
	void testCancelBookedSlot() throws ParseException {
		String resp = schedulingController.cancelBookedSlot(null);
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(resp);
		Assertions.assertTrue(json.getAsString("status").contains("Failed"));
	}

	@Test
	void testGetdayview() throws ParseException {
		String resp = schedulingController.getdayview(null);
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(resp);
		Assertions.assertEquals("Success", json.getAsString("status"));
	}

}
