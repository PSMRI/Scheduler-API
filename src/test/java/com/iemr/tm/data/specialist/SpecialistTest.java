/*
* AMRIT - Accessible Medical Records via Integrated Technologies
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.iemr.tm.data.schedule.SpecialistAvailability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Specialist Test Suite")
class SpecialistTest {

    private static final Long USER_ID = 4021L;
    private static final Long SPECIALIZATION_ID = 12L;
    private static final Long PROVIDER_SERVICE_MAP_ID = 8L;

    private Specialist fullSpecialist() {
        return new Specialist("dr.rao", USER_ID, "Anita", "K", "Rao", SPECIALIZATION_ID,
                "Female", "Dr", "anita.rao@amrit.example.org", "9000000000", "Cardiology");
    }

    @Test
    @DisplayName("the no-argument constructor should leave every property unset")
    void defaultConstructor_shouldLeaveEveryPropertyUnset() {
        Specialist specialist = new Specialist();

        assertNull(specialist.getUserID());
        assertNull(specialist.getUserName());
        assertNull(specialist.getSpecialization());
        assertNull(specialist.getSpecialistAvailability());
    }

    @Test
    @DisplayName("the projection constructor should map every column of the specialist query")
    void projectionConstructor_shouldMapEveryColumn() {
        Specialist specialist = fullSpecialist();

        assertEquals("dr.rao", specialist.getUserName());
        assertEquals(USER_ID, specialist.getUserID());
        assertEquals("Dr", specialist.getTitleName());
        assertEquals("Anita", specialist.getFirstName());
        assertEquals("K", specialist.getMiddleName());
        assertEquals("Rao", specialist.getLastName());
        assertEquals("Female", specialist.getGenderName());
        assertEquals("anita.rao@amrit.example.org", specialist.getEmail());
        assertEquals("9000000000", specialist.getContactNo());
        assertEquals(SPECIALIZATION_ID, specialist.getSpecializationID());
        assertEquals("Cardiology", specialist.getSpecialization());
    }

    @Test
    @DisplayName("the lookup constructor should retain only the three identifiers it is given")
    void lookupConstructor_shouldRetainOnlyIdentifiers() {
        Specialist specialist = new Specialist(USER_ID, SPECIALIZATION_ID, PROVIDER_SERVICE_MAP_ID);

        assertEquals(USER_ID, specialist.getUserID());
        assertEquals(SPECIALIZATION_ID, specialist.getSpecializationID());
        assertEquals(PROVIDER_SERVICE_MAP_ID, specialist.getProviderServiceMapID());
        assertNull(specialist.getFirstName());
        assertNull(specialist.getSpecialization());
    }

    @Test
    @DisplayName("the availability attached to a specialist should be readable back")
    void specialistAvailability_shouldBeReadableBack() {
        SpecialistAvailability availability = new SpecialistAvailability();
        availability.setTimeSlot("AAAA");
        Specialist specialist = fullSpecialist();

        specialist.setSpecialistAvailability(availability);

        assertSame(availability, specialist.getSpecialistAvailability());
    }

    @Test
    @DisplayName("toString should render the specialist and the attached availability as JSON")
    void toString_shouldRenderSpecialistAsJson() {
        Specialist specialist = fullSpecialist();
        SpecialistAvailability availability = new SpecialistAvailability();
        availability.setTimeSlot("AAAA");
        specialist.setSpecialistAvailability(availability);

        String json = specialist.toString();

        assertTrue(json.contains("\"userName\":\"dr.rao\""), json);
        assertTrue(json.contains("\"specialization\":\"Cardiology\""), json);
        assertTrue(json.contains("\"timeSlot\":\"AAAA\""), json);
    }
}
