package com.iemr.tm.controller.specialist;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iemr.tm.data.specialist.Specialist;
import com.iemr.tm.service.specialist.SpecialistService;
import com.iemr.tm.utils.exception.TMException;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
@ExtendWith(MockitoExtension.class)
class SpecialistControllerTest {
	@InjectMocks
	SpecialistController specialistController;
	
	@Mock
	private SpecialistService specialistService;
	
	@Test
	void testMarkavailability() throws ParseException {
		String resp = specialistController.markavailability();
		String status = getStatus(resp);
		Assertions.assertEquals("Success", status);
	}
	@Test
	void testMarkavailabilityException() throws ParseException {
		when(specialistService.getspecialization()).thenThrow(NullPointerException.class);
		String resp = specialistController.markavailability();
		String status = getStatus(resp);
		Assertions.assertTrue(status.contains("Failed"));
	}

	@Test
	void testGetSpecialist() throws ParseException {
		Specialist specialist = new Specialist();
		specialist.setContactNo("9876543210");
		String resp = specialistController.getSpecialist(specialist);
		String status = getStatus(resp);
		Assertions.assertEquals("Success", status);
	}
	
	@Test
	void testGetSpecialistException() throws ParseException, TMException {
		Specialist specialist = new Specialist();
		specialist.setContactNo("9876543210");
		when(specialistService.getspecialistUser(any(),any(), any())).thenThrow(NullPointerException.class);
		String resp = specialistController.getSpecialist(specialist);
		String status = getStatus(resp);
		Assertions.assertTrue(status.contains("Failed"));
	}

	@Test
	void testInfo() throws ParseException {
		Long userId=9l;
		String resp = specialistController.info(userId);
		String status = getStatus(resp);
		Assertions.assertTrue(status.contains("Failed"));
	}

	@Test
	void testGetSpecialistAll() throws ParseException {
		Specialist specialist = new Specialist();
		specialist.setContactNo("9876543210");
		String resp = specialistController.getSpecialistAll(specialist);
		String status = getStatus(resp);
		Assertions.assertEquals("Success", status);
	}
	@Test
	void testGetSpecialistAllException() throws ParseException {
		Specialist specialist = new Specialist();
		specialist.setContactNo("9876543210");
		when(specialistService.getAllSpecialist(any())).thenThrow(NullPointerException.class);
		String resp = specialistController.getSpecialistAll(specialist);
		String status = getStatus(resp);
		Assertions.assertTrue(status.contains("Failed"));
	}
	public String getStatus(String resp) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(resp);
		String asString = json.getAsString("status");
		return asString;
	}
}
