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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Slot Test Suite")
class SlotTest {

    private static final String AVAILABLE_TEMPLATE = "AAA";
    private static final String BOOKED_TEMPLATE = "BBB";

    @Nested
    @DisplayName("The no-argument constructor")
    class DefaultConstructorTests {

        @Test
        @DisplayName("a default slot should leave every property unset")
        void defaultSlot_shouldLeaveEveryPropertyUnset() {
            Slot slot = new Slot();

            assertNull(slot.getFromTime());
            assertNull(slot.getToTime());
            assertNull(slot.getSlot());
            assertNull(slot.getStatus());
        }

        @Test
        @DisplayName("setters should populate a default slot and equality should follow the values")
        void setters_shouldPopulateDefaultSlot() {
            Slot slot = new Slot();
            slot.setFromTime(LocalTime.of(8, 0));
            slot.setToTime(LocalTime.of(8, 15));
            slot.setSlot(AVAILABLE_TEMPLATE);
            slot.setStatus("Available");

            assertEquals(LocalTime.of(8, 0), slot.getFromTime());
            assertEquals(LocalTime.of(8, 15), slot.getToTime());
            assertEquals(AVAILABLE_TEMPLATE, slot.getSlot());
            assertEquals("Available", slot.getStatus());
            assertNotEquals(new Slot(), slot);
        }
    }

    @Nested
    @DisplayName("The fixed-width constructor used when splitting a day into slots")
    class FixedWidthConstructorTests {

        @Test
        @DisplayName("a slot matching the available template should start at its index and read as Available")
        void availableSlot_shouldMapIndexToTimeRange() {
            Slot slot = new Slot(12, 3, AVAILABLE_TEMPLATE, AVAILABLE_TEMPLATE, BOOKED_TEMPLATE);

            assertEquals(LocalTime.of(1, 0), slot.getFromTime());
            assertEquals(LocalTime.of(1, 15), slot.getToTime());
            assertEquals("Available", slot.getStatus());
            assertEquals(AVAILABLE_TEMPLATE, slot.getSlot());
        }

        @Test
        @DisplayName("a slot matching the booked template should read as Booked")
        void bookedSlot_shouldReadAsBooked() {
            Slot slot = new Slot(0, 3, BOOKED_TEMPLATE, AVAILABLE_TEMPLATE, BOOKED_TEMPLATE);

            assertEquals(LocalTime.MIDNIGHT, slot.getFromTime());
            assertEquals("Booked", slot.getStatus());
        }

        @Test
        @DisplayName("a slot matching neither template should read as Unavailable")
        void mixedSlot_shouldReadAsUnavailable() {
            assertEquals("Unavailable", new Slot(0, 3, "UUU", AVAILABLE_TEMPLATE, BOOKED_TEMPLATE).getStatus());
            assertEquals("Unavailable", new Slot(0, 3, "ABU", AVAILABLE_TEMPLATE, BOOKED_TEMPLATE).getStatus());
        }

        @Test
        @DisplayName("the final slot of the day should be clamped to 23:59:59 rather than overflow")
        void finalSlotOfDay_shouldBeClampedToEndOfDay() {
            Slot slot = new Slot(285, 3, AVAILABLE_TEMPLATE, AVAILABLE_TEMPLATE, BOOKED_TEMPLATE);

            assertEquals(LocalTime.of(23, 45), slot.getFromTime());
            assertEquals(LocalTime.of(23, 59, 59), slot.getToTime());
        }
    }

    @Nested
    @DisplayName("The run-length constructor used for the month view")
    class RunLengthConstructorTests {

        @Test
        @DisplayName("a run of A should be reported as Available across its whole span")
        void availableRun_shouldSpanTheWholeMatch() {
            Slot slot = new Slot(108, 132, "AAAAAAAAAAAAAAAAAAAAAAAA");

            assertEquals(LocalTime.of(9, 0), slot.getFromTime());
            assertEquals(LocalTime.of(11, 0), slot.getToTime());
            assertEquals("Available", slot.getStatus());
        }

        @Test
        @DisplayName("a run of B should be reported as Booked")
        void bookedRun_shouldReadAsBooked() {
            assertEquals("Booked", new Slot(0, 6, "BBBBBB").getStatus());
        }

        @Test
        @DisplayName("a run of any other marker should be reported as Unavailable")
        void otherRun_shouldReadAsUnavailable() {
            assertEquals("Unavailable", new Slot(0, 6, "UUUUUU").getStatus());
        }

        @Test
        @DisplayName("a run reaching midnight should be clamped to 23:59:59")
        void runReachingMidnight_shouldBeClampedToEndOfDay() {
            Slot slot = new Slot(282, 288, "AAAAAA");

            assertEquals(LocalTime.of(23, 30), slot.getFromTime());
            assertEquals(LocalTime.of(23, 59, 59), slot.getToTime());
        }
    }

    @Test
    @DisplayName("toString should render the slot as JSON with ISO times")
    void toString_shouldRenderSlotAsJson() {
        String json = new Slot(108, 111, AVAILABLE_TEMPLATE, AVAILABLE_TEMPLATE, BOOKED_TEMPLATE).toString();

        assertTrue(json.contains("\"status\":\"Available\""), json);
        assertTrue(json.contains("09:00"), json);
    }
}
