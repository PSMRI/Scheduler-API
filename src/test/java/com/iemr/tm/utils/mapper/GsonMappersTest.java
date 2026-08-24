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
package com.iemr.tm.utils.mapper;

import java.lang.reflect.Type;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Gson mapper Test Suite")
class GsonMappersTest {

    /** A carrier that exercises both the exposed and the hidden field handling. */
    private static final class Appointment {
        @com.google.gson.annotations.Expose
        private LocalTime fromTime;
        @com.google.gson.annotations.Expose
        private String specialist;
        private String internalNote;
    }

    @org.junit.jupiter.api.BeforeEach
    @DisplayName("Prime the shared output builder, which only the constructor creates")
    void primeOutputMapper() {
        new OutputMapper();
    }

    @Nested
    @DisplayName("OutputMapper")
    class OutputMapperTests {

        @Test
        @DisplayName("gson should serialise only the exposed fields and render local times in ISO form")
        void gson_shouldSerialiseExposedFieldsWithIsoTimes() {
            Appointment appointment = new Appointment();
            appointment.fromTime = LocalTime.of(9, 30);
            appointment.specialist = "dr.rao";
            appointment.internalNote = "not for the wire";

            String json = OutputMapper.gson().toJson(appointment);

            assertTrue(json.contains("\"specialist\":\"dr.rao\""), json);
            assertTrue(json.contains("09:30:00"), json);
            assertFalse(json.contains("internalNote"), "a field without @Expose must stay off the wire");
        }

        @Test
        @DisplayName("gson should serialise nulls rather than omit them")
        void gson_shouldSerialiseNulls() {
            assertTrue(OutputMapper.gson().toJson(new Appointment()).contains("null"));
        }

        @Test
        @DisplayName("the constructor should reuse the shared builder across instances")
        void constructor_shouldReuseSharedBuilder() {
            new OutputMapper();

            assertNotNull(OutputMapper.gson());
        }
    }

    @Nested
    @DisplayName("LocalTimeAdapter")
    class LocalTimeAdapterTests {

        @Test
        @DisplayName("serialize should render a local time in ISO form")
        void serialize_shouldRenderIsoTime() {
            JsonElement element = new LocalTimeAdapter().serialize(LocalTime.of(9, 30), null, null);

            assertEquals("09:30:00", element.getAsString());
        }

        @Test
        @DisplayName("serialize should keep the seconds of a time that carries them")
        void serialize_shouldKeepSeconds() {
            assertEquals("23:59:59",
                    new LocalTimeAdapter().serialize(LocalTime.of(23, 59, 59), null, null).getAsString());
        }
    }

    @Nested
    @DisplayName("LocalTimeDeAdapter")
    class LocalTimeDeAdapterTests {

        private final LocalTimeDeAdapter deAdapter = new LocalTimeDeAdapter();
        private final Type type = LocalTime.class;

        @Test
        @DisplayName("deserialize should read an hour and minute pair")
        void deserialize_shouldReadHourAndMinute() {
            assertEquals(LocalTime.of(9, 30), deAdapter.deserialize(new JsonPrimitive("09:30"), type, null));
        }

        @Test
        @DisplayName("deserialize should ignore any seconds beyond the hour and minute")
        void deserialize_shouldIgnoreSeconds() {
            assertEquals(LocalTime.of(9, 30), deAdapter.deserialize(new JsonPrimitive("09:30:45"), type, null));
        }

        @Test
        @DisplayName("deserialize should reject a value that carries no minute")
        void deserialize_shouldRejectValueWithoutMinute() {
            assertThrows(ArrayIndexOutOfBoundsException.class,
                    () -> deAdapter.deserialize(new JsonPrimitive("09"), type, null));
        }

        @Test
        @DisplayName("deserialize should reject a non-numeric time")
        void deserialize_shouldRejectNonNumericTime() {
            assertThrows(NumberFormatException.class,
                    () -> deAdapter.deserialize(new JsonPrimitive("nine:thirty"), type, null));
        }
    }

    @Nested
    @DisplayName("InputMapper and OutputMapper together")
    class RoundTripTests {

        @Test
        @DisplayName("a time written by the output mapper should be readable by the input mapper")
        void time_shouldSurviveARoundTrip() {
            Appointment appointment = new Appointment();
            appointment.fromTime = LocalTime.of(9, 30);
            Gson writer = OutputMapper.gson();

            Appointment restored = InputMapper.gson().fromJson(writer.toJson(appointment), Appointment.class);

            assertEquals(LocalTime.of(9, 30), restored.fromTime);
            assertNull(restored.specialist);
        }
    }
}
