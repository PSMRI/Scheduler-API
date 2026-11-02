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
package com.iemr.tm.data.schedule;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SpecialistAvailability Test Suite")
class SpecialistAvailabilityTest {

    private static final Long USER_ID = 4021L;

    @Test
    @DisplayName("the no-argument constructor should leave every property unset")
    void defaultConstructor_shouldLeaveEveryPropertyUnset() {
        SpecialistAvailability availability = new SpecialistAvailability();

        assertNull(availability.getUserID());
        assertNull(availability.getConfiguredDate());
        assertNull(availability.getTimeSlot());
        assertNull(availability.getSlots());
    }

    @Test
    @DisplayName("the slot constructor should carry the user, date and slot list through unchanged")
    void slotConstructor_shouldCarryUserDateAndSlots() {
        Date date = new Date(1_771_286_400_000L);
        ArrayList<Slot> slots = new ArrayList<>(List.of(new Slot()));

        SpecialistAvailability availability = new SpecialistAvailability(USER_ID, date, slots);

        assertEquals(USER_ID, availability.getUserID());
        assertSame(date, availability.getConfiguredDate());
        assertSame(slots, availability.getSlots());
        assertNull(availability.getTimeSlot(), "an availability built from slots holds no raw slot string");
    }

    @Test
    @DisplayName("toString should render the availability, including its slots, as JSON")
    void toString_shouldRenderAvailabilityAsJson() {
        Slot slot = new Slot();
        slot.setFromTime(LocalTime.of(9, 0));
        slot.setToTime(LocalTime.of(9, 15));
        slot.setStatus("Available");

        SpecialistAvailability availability =
                new SpecialistAvailability(USER_ID, new Date(1_771_286_400_000L), new ArrayList<>(List.of(slot)));
        availability.setTimeSlot("AAAA");

        String json = availability.toString();

        assertTrue(json.contains("\"userID\":4021"), json);
        assertTrue(json.contains("\"timeSlot\":\"AAAA\""), json);
        assertTrue(json.contains("\"status\":\"Available\""), json);
    }
}
