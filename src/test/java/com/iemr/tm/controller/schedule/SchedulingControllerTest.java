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
package com.iemr.tm.controller.schedule;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iemr.tm.data.schedule.SpecialistAvailability;
import com.iemr.tm.data.schedule.SpecialistAvailabilityDetail;
import com.iemr.tm.data.schedule.SpecialistInput2;
import com.iemr.tm.data.specialist.Specialist;
import com.iemr.tm.service.schedule.SchedulingService;
import com.iemr.tm.utils.exception.TMException;
import com.iemr.tm.utils.response.OutputResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SchedulingController Test Suite")
class SchedulingControllerTest {

    private static final Long USER_ID = 4021L;
    private static final String AVAILABILITY_REQUEST = "{\"userID\":4021,"
            + "\"configuredFromDate\":\"2026-03-02T00:00:00.000\","
            + "\"configuredToDate\":\"2026-03-02T00:00:00.000\","
            + "\"configuredFromTime\":\"1970-01-01T09:00:00.000\","
            + "\"configuredToTime\":\"1970-01-01T11:00:00.000\","
            + "\"createdBy\":\"scheduler-admin\"}";
    private static final String SLOT_REQUEST = "{\"userID\":4021,"
            + "\"date\":\"2026-03-02T00:00:00.000\",\"fromTime\":\"09:30\",\"toTime\":\"10:00\"}";
    private static final String MALFORMED_REQUEST = "{\"userID\":";

    @Mock
    private SchedulingService schedulingService;

    @InjectMocks
    private SchedulingController schedulingController;

    private SpecialistAvailabilityDetail detail;
    private SpecialistAvailability availability;

    @BeforeEach
    @DisplayName("Prepare the payloads the service hands back before each test")
    void setUp() {
        detail = new SpecialistAvailabilityDetail();
        detail.setUserID(USER_ID);
        detail.setCreatedBy("scheduler-admin");

        availability = new SpecialistAvailability();
        availability.setUserID(USER_ID);
        availability.setTimeSlot("AAAA");
    }

    private static int statusCodeOf(String response) {
        return Integer.parseInt(response.replaceAll(".*\"statusCode\":(-?\\d+).*", "$1"));
    }

    @Nested
    @DisplayName("markavailability")
    class MarkAvailabilityTests {

        @Test
        @DisplayName("markavailability should answer the saved detail on success")
        void markavailability_shouldAnswerSavedDetail() throws Exception {
            when(schedulingService.markAvailability(any(SpecialistAvailabilityDetail.class))).thenReturn(detail);

            String response = schedulingController.markavailability(AVAILABILITY_REQUEST);

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
            assertTrue(response.contains("\"userID\":4021"), response);
        }

        @Test
        @DisplayName("markavailability should deserialise the request before handing it to the service")
        void markavailability_shouldDeserialiseTheRequest() throws Exception {
            when(schedulingService.markAvailability(any(SpecialistAvailabilityDetail.class))).thenReturn(detail);

            schedulingController.markavailability(AVAILABILITY_REQUEST);

            ArgumentCaptor<SpecialistAvailabilityDetail> captor =
                    ArgumentCaptor.forClass(SpecialistAvailabilityDetail.class);
            org.mockito.Mockito.verify(schedulingService).markAvailability(captor.capture());
            assertEquals(USER_ID, captor.getValue().getUserID());
            assertEquals("scheduler-admin", captor.getValue().getCreatedBy());
        }

        @Test
        @DisplayName("markavailability should report a scheduling failure as a TM error")
        void markavailability_shouldReportSchedulingFailure() throws Exception {
            when(schedulingService.markAvailability(any(SpecialistAvailabilityDetail.class)))
                    .thenThrow(new TMException("Appointment Booked. Please Cancel the Consultation first"));

            String response = schedulingController.markavailability(AVAILABILITY_REQUEST);

            assertEquals(OutputResponse.TM_FAILURE, statusCodeOf(response));
            assertTrue(response.contains("Appointment Booked"), response);
        }

        @Test
        @DisplayName("markavailability should report a malformed request as a failure")
        void markavailability_shouldReportMalformedRequest() {
            String response = schedulingController.markavailability(MALFORMED_REQUEST);

            assertEquals(OutputResponse.GENERIC_FAILURE, statusCodeOf(response));
        }
    }

    @Nested
    @DisplayName("unmarkavailability")
    class UnmarkAvailabilityTests {

        @Test
        @DisplayName("unmarkavailability should answer the submitted detail on success")
        void unmarkavailability_shouldAnswerSubmittedDetail() throws Exception {
            when(schedulingService.markUnavailability(any(SpecialistAvailabilityDetail.class))).thenReturn(detail);

            String response = schedulingController.unmarkavailability(AVAILABILITY_REQUEST);

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
            assertTrue(response.contains("\"userID\":4021"), response);
        }

        @Test
        @DisplayName("unmarkavailability should report a booked-slot conflict as a TM error")
        void unmarkavailability_shouldReportBookedSlotConflict() throws Exception {
            when(schedulingService.markUnavailability(any(SpecialistAvailabilityDetail.class)))
                    .thenThrow(new TMException("Appointment Booked. Please Cancel the Consultation first"));

            String response = schedulingController.unmarkavailability(AVAILABILITY_REQUEST);

            assertEquals(OutputResponse.TM_FAILURE, statusCodeOf(response));
        }

        @Test
        @DisplayName("unmarkavailability should report a malformed request as a failure")
        void unmarkavailability_shouldReportMalformedRequest() {
            assertEquals(OutputResponse.GENERIC_FAILURE,
                    statusCodeOf(schedulingController.unmarkavailability(MALFORMED_REQUEST)));
        }
    }

    @Nested
    @DisplayName("getavailableSlot")
    class GetAvailableSlotTests {

        @Test
        @DisplayName("getavailableSlot should answer the day the service resolves")
        void getavailableSlot_shouldAnswerResolvedDay() {
            when(schedulingService.fetchavailability(any(SpecialistInput2.class))).thenReturn(availability);

            String response = schedulingController.getavailableSlot(SLOT_REQUEST);

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
            assertTrue(response.contains("\"timeSlot\":\"AAAA\""), response);
        }

        @Test
        @DisplayName("getavailableSlot should report a lookup failure")
        void getavailableSlot_shouldReportLookupFailure() {
            when(schedulingService.fetchavailability(any(SpecialistInput2.class)))
                    .thenThrow(new IllegalStateException("availability lookup failed"));

            assertEquals(OutputResponse.GENERIC_FAILURE,
                    statusCodeOf(schedulingController.getavailableSlot(SLOT_REQUEST)));
        }
    }

    @Nested
    @DisplayName("view")
    class MonthViewTests {

        @Test
        @DisplayName("view should answer the days the service resolves for the requested month")
        void view_shouldAnswerResolvedDays() {
            when(schedulingService.fetchmonthavailability(any(SpecialistInput2.class), eq(2026), eq(3), eq(2)))
                    .thenReturn(new ArrayList<>(List.of(availability)));

            String response = schedulingController.view(SLOT_REQUEST, 2026, 3, 2);

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
            assertTrue(response.contains("\"timeSlot\":\"AAAA\""), response);
        }

        @Test
        @DisplayName("view should pass a year-only request through with no month or day")
        void view_shouldPassYearOnlyRequestThrough() {
            when(schedulingService.fetchmonthavailability(any(SpecialistInput2.class), eq(2026), eq(null), eq(null)))
                    .thenReturn(new ArrayList<>());

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(schedulingController.view(SLOT_REQUEST, 2026, null, null)));
        }

        @Test
        @DisplayName("view should report a month lookup failure")
        void view_shouldReportMonthLookupFailure() {
            when(schedulingService.fetchmonthavailability(any(SpecialistInput2.class), eq(2026), eq(3), eq(2)))
                    .thenThrow(new IllegalStateException("month lookup failed"));

            assertEquals(OutputResponse.GENERIC_FAILURE,
                    statusCodeOf(schedulingController.view(SLOT_REQUEST, 2026, 3, 2)));
        }
    }

    @Nested
    @DisplayName("bookSlot and cancelBookedSlot")
    class BookingTests {

        @Test
        @DisplayName("bookSlot should ask the service to mark the window as booked")
        void bookSlot_shouldMarkWindowAsBooked() throws Exception {
            when(schedulingService.bookSlot(any(SpecialistInput2.class), eq('B'))).thenReturn("Booked");

            String response = schedulingController.bookSlot(SLOT_REQUEST);

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
            assertTrue(response.contains("Booked"), response);
        }

        @Test
        @DisplayName("cancelBookedSlot should ask the service to release the window")
        void cancelBookedSlot_shouldReleaseWindow() throws Exception {
            when(schedulingService.bookSlot(any(SpecialistInput2.class), eq('C'))).thenReturn("Booked");

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(schedulingController.cancelBookedSlot(SLOT_REQUEST)));
        }

        @Test
        @DisplayName("bookSlot should report an unavailable specialist as a TM error")
        void bookSlot_shouldReportUnavailableSpecialist() throws Exception {
            when(schedulingService.bookSlot(any(SpecialistInput2.class), eq('B')))
                    .thenThrow(new TMException("Doctor not available"));

            String response = schedulingController.bookSlot(SLOT_REQUEST);

            assertEquals(OutputResponse.TM_FAILURE, statusCodeOf(response));
            assertTrue(response.contains("Doctor not available"), response);
        }

        @Test
        @DisplayName("cancelBookedSlot should report a cancellation failure as a TM error")
        void cancelBookedSlot_shouldReportCancellationFailure() throws Exception {
            when(schedulingService.bookSlot(any(SpecialistInput2.class), eq('C')))
                    .thenThrow(new TMException("Doctor not available"));

            assertEquals(OutputResponse.TM_FAILURE,
                    statusCodeOf(schedulingController.cancelBookedSlot(SLOT_REQUEST)));
        }
    }

    @Nested
    @DisplayName("getdayview")
    class DayViewTests {

        @Test
        @DisplayName("getdayview should answer the specialists the service resolves")
        void getdayview_shouldAnswerResolvedSpecialists() throws Exception {
            Specialist specialist = new Specialist("dr.rao", USER_ID, "Anita", "K", "Rao", 12L,
                    "Female", "Dr", "anita.rao@amrit.example.org", "9000000000", "Cardiology");
            when(schedulingService.fetchAllAvailability(any(SpecialistInput2.class)))
                    .thenReturn(new ArrayList<>(List.of(specialist)));

            String response = schedulingController.getdayview(SLOT_REQUEST);

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
            assertTrue(response.contains("\"userName\":\"dr.rao\""), response);
        }

        @Test
        @DisplayName("getdayview should report an unmapped user as a TM error")
        void getdayview_shouldReportUnmappedUser() throws Exception {
            when(schedulingService.fetchAllAvailability(any(SpecialistInput2.class)))
                    .thenThrow(new TMException("User not mapped to a parking place"));

            String response = schedulingController.getdayview(SLOT_REQUEST);

            assertEquals(OutputResponse.TM_FAILURE, statusCodeOf(response));
            assertTrue(response.contains("User not mapped to a parking place"), response);
        }
    }
}
