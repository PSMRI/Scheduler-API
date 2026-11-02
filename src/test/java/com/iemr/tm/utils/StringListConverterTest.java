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
package com.iemr.tm.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StringListConverter Test Suite")
class StringListConverterTest {

    private final StringListConverter converter = new StringListConverter();

    @Nested
    @DisplayName("convertToDatabaseColumn")
    class ToDatabaseColumnTests {

        @Test
        @DisplayName("a populated list should be joined with commas and no trailing separator")
        void populatedList_shouldBeJoinedWithCommas() {
            assertEquals("0,6", converter.convertToDatabaseColumn(List.of(0, 6)));
        }

        @Test
        @DisplayName("a single-element list should be written without a separator")
        void singleElementList_shouldBeWrittenWithoutSeparator() {
            assertEquals("3", converter.convertToDatabaseColumn(List.of(3)));
        }

        @Test
        @DisplayName("an empty list should be stored as null")
        void emptyList_shouldBeStoredAsNull() {
            assertNull(converter.convertToDatabaseColumn(new ArrayList<>()));
        }

        @Test
        @DisplayName("a null list should be stored as null")
        void nullList_shouldBeStoredAsNull() {
            assertNull(converter.convertToDatabaseColumn(null));
        }
    }

    @Nested
    @DisplayName("convertToEntityAttribute")
    class ToEntityAttributeTests {

        @Test
        @DisplayName("a comma-separated column should be split back into integers")
        void commaSeparatedColumn_shouldBeSplitIntoIntegers() {
            assertEquals(List.of(0, 6), converter.convertToEntityAttribute("0,6"));
        }

        @Test
        @DisplayName("a single-value column should yield a single-element list")
        void singleValueColumn_shouldYieldSingleElementList() {
            assertEquals(List.of(3), converter.convertToEntityAttribute("3"));
        }

        @Test
        @DisplayName("a null column should yield an empty list rather than null")
        void nullColumn_shouldYieldEmptyList() {
            assertTrue(converter.convertToEntityAttribute(null).isEmpty());
        }

        @Test
        @DisplayName("a non-numeric column should be reported rather than silently dropped")
        void nonNumericColumn_shouldBeReported() {
            assertThrows(NumberFormatException.class, () -> converter.convertToEntityAttribute("mon,tue"));
        }

        @Test
        @DisplayName("a round trip through both directions should preserve the excluded days")
        void roundTrip_shouldPreserveExcludedDays() {
            List<Integer> days = List.of(0, 3, 6);

            assertEquals(days, converter.convertToEntityAttribute(converter.convertToDatabaseColumn(days)));
        }
    }
}
