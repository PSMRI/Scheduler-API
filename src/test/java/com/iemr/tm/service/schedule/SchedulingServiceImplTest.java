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
package com.iemr.tm.service.schedule;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.tm.data.schedule.Slot;
import com.iemr.tm.data.schedule.SpecialistAvailability;
import com.iemr.tm.data.schedule.SpecialistAvailabilityDetail;
import com.iemr.tm.data.schedule.SpecialistInput2;
import com.iemr.tm.data.specialist.Specialist;
import com.iemr.tm.repo.schedule.SpecialistAvailabilityDetailRepo;
import com.iemr.tm.repo.schedule.SpecialistAvailabilityRepo;
import com.iemr.tm.service.specialist.SpecialistService;
import com.iemr.tm.utils.exception.TMException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SchedulingServiceImpl Test Suite")
class SchedulingServiceImplTest {

    /** One day is split into 288 five-minute slots. */
    private static final int SLOTS_PER_DAY = 288;
    private static final Long USER_ID = 4021L;
    private static final Long PROVIDER_SERVICE_MAP_ID = 8L;
    private static final Long SPECIALIZATION_ID = 12L;
    private static final String CREATED_BY = "scheduler-admin";

    @Mock
    private SpecialistAvailabilityRepo specialistAvailabilityRepo;

    @Mock
    private SpecialistAvailabilityDetailRepo specialistAvailabilityDetailRepo;

    @Mock
    private SpecialistService specialistService;

    private SchedulingServiceImpl schedulingService;

    @BeforeEach
    @DisplayName("Wire the service to its mocked repositories before each test")
    void setUp() {
        schedulingService = new SchedulingServiceImpl(specialistAvailabilityRepo, specialistAvailabilityDetailRepo);
        ReflectionTestUtils.setField(schedulingService, "specialistAvailabilityRepo", specialistAvailabilityRepo);
        ReflectionTestUtils.setField(schedulingService, "specialistAvailabilityDetailRepo",
                specialistAvailabilityDetailRepo);
        ReflectionTestUtils.setField(schedulingService, "specialistService", specialistService);
    }

    /** A day on which the specialist is unavailable from midnight to midnight. */
    private static String emptyDay() {
        return "U".repeat(SLOTS_PER_DAY);
    }

    /** The five-minute slot index that a wall-clock time falls into. */
    private static int slotIndex(int hour, int minute) {
        return ((hour * 60) + minute) / 5;
    }

    private static String dayWith(char marker, int fromHour, int toHour) {
        StringBuilder day = new StringBuilder(emptyDay());
        for (int i = slotIndex(fromHour, 0); i < slotIndex(toHour, 0); i++) {
            day.setCharAt(i, marker);
        }
        return day.toString();
    }

    @SuppressWarnings("deprecation")
    private static Date dayOf(int year, int month, int date) {
        return new Date(year - 1900, month - 1, date);
    }

    @SuppressWarnings("deprecation")
    private static Timestamp timeOf(int hour, int minute) {
        return new Timestamp(0, 0, 0, hour, minute, 0, 0);
    }

    private SpecialistAvailabilityDetail availabilityDetail(Date from, Date to, List<Integer> excludeDays) {
        SpecialistAvailabilityDetail detail = new SpecialistAvailabilityDetail();
        detail.setUserID(USER_ID);
        detail.setCreatedBy(CREATED_BY);
        detail.setConfiguredFromDate(from);
        detail.setConfiguredToDate(to);
        detail.setConfiguredFromTime(timeOf(9, 0));
        detail.setConfiguredToTime(timeOf(11, 0));
        detail.setExcludeDays(excludeDays);
        return detail;
    }

    private SpecialistAvailability storedAvailability(Date date, String timeSlot) {
        SpecialistAvailability availability = new SpecialistAvailability();
        availability.setUserID(USER_ID);
        availability.setConfiguredDate(date);
        availability.setTimeSlot(timeSlot);
        return availability;
    }

    private SpecialistInput2 slotRequest(Date date, LocalTime from, LocalTime to) {
        SpecialistInput2 input = new SpecialistInput2();
        input.setUserID(USER_ID);
        input.setDate(date);
        input.setFromTime(from);
        input.setToTime(to);
        return input;
    }

    @SuppressWarnings("unchecked")
    private List<SpecialistAvailability> captureSavedDays() {
        ArgumentCaptor<List<SpecialistAvailability>> captor = ArgumentCaptor.forClass(List.class);
        verify(specialistAvailabilityRepo).saveAll(captor.capture());
        return captor.getValue();
    }

    @Nested
    @DisplayName("markAvailability")
    class MarkAvailabilityTests {

        @Test
        @DisplayName("markAvailability should open the requested hours on a day that has no record yet")
        void markAvailability_shouldOpenRequestedHoursOnANewDay() throws Exception {
            Date day = dayOf(2026, 3, 2);
            SpecialistAvailabilityDetail detail = availabilityDetail(day, day, null);
            when(specialistAvailabilityRepo.findByConfiguredDateBetweenAndUserID(any(), any(), eq(USER_ID)))
                    .thenReturn(new ArrayList<>());
            when(specialistAvailabilityDetailRepo.save(detail)).thenReturn(detail);

            SpecialistAvailabilityDetail saved = schedulingService.markAvailability(detail);

            assertSame(detail, saved);
            assertTrue(saved.getIsAvailability(), "marking availability must flag the detail as available");

            List<SpecialistAvailability> days = captureSavedDays();
            assertEquals(1, days.size());
            String slots = days.get(0).getTimeSlot();
            assertEquals(SLOTS_PER_DAY, slots.length());
            assertEquals('A', slots.charAt(slotIndex(9, 0)));
            assertEquals('A', slots.charAt(slotIndex(10, 55)));
            assertEquals('U', slots.charAt(slotIndex(11, 0)), "the closing hour itself stays unavailable");
            assertEquals('U', slots.charAt(slotIndex(8, 55)));
            assertEquals(CREATED_BY, days.get(0).getCreatedBy());
            assertEquals(USER_ID, days.get(0).getUserID());
        }

        @Test
        @DisplayName("markAvailability should extend an existing day and record who modified it")
        void markAvailability_shouldExtendAnExistingDay() throws Exception {
            Date day = dayOf(2026, 3, 2);
            SpecialistAvailability existing = storedAvailability(day, dayWith('A', 14, 16));
            SpecialistAvailabilityDetail detail = availabilityDetail(day, day, null);
            when(specialistAvailabilityRepo.findByConfiguredDateBetweenAndUserID(any(), any(), eq(USER_ID)))
                    .thenReturn(new ArrayList<>(List.of(existing)));
            when(specialistAvailabilityDetailRepo.save(detail)).thenReturn(detail);

            schedulingService.markAvailability(detail);

            List<SpecialistAvailability> days = captureSavedDays();
            assertEquals(1, days.size());
            assertSame(existing, days.get(0), "an existing day must be updated rather than replaced");
            assertEquals(CREATED_BY, existing.getModifiedBy());
            assertEquals('A', existing.getTimeSlot().charAt(slotIndex(9, 0)));
            assertEquals('A', existing.getTimeSlot().charAt(slotIndex(14, 0)),
                    "the hours already open must survive the update");
        }

        @Test
        @DisplayName("markAvailability should cover every day of an inclusive date range")
        void markAvailability_shouldCoverEveryDayOfTheRange() throws Exception {
            SpecialistAvailabilityDetail detail = availabilityDetail(dayOf(2026, 3, 2), dayOf(2026, 3, 4), null);
            when(specialistAvailabilityRepo.findByConfiguredDateBetweenAndUserID(any(), any(), eq(USER_ID)))
                    .thenReturn(new ArrayList<>());
            when(specialistAvailabilityDetailRepo.save(detail)).thenReturn(detail);

            schedulingService.markAvailability(detail);

            assertEquals(3, captureSavedDays().size());
        }

        @Test
        @DisplayName("markAvailability should skip the excluded weekdays of the range")
        void markAvailability_shouldSkipExcludedWeekdays() throws Exception {
            // 2 March 2026 is a Monday, so the range covers Monday through Sunday.
            SpecialistAvailabilityDetail detail =
                    availabilityDetail(dayOf(2026, 3, 2), dayOf(2026, 3, 8), List.of(0, 6));
            when(specialistAvailabilityRepo.findByConfiguredDateBetweenAndUserID(any(), any(), eq(USER_ID)))
                    .thenReturn(new ArrayList<>());
            when(specialistAvailabilityDetailRepo.save(detail)).thenReturn(detail);

            schedulingService.markAvailability(detail);

            List<SpecialistAvailability> days = captureSavedDays();
            assertEquals(5, days.size(), "the weekend must be excluded");
            days.forEach(day -> assertFalse(List.of(0, 6).contains(day.getConfiguredDate().getDay()),
                    "no excluded weekday may be saved"));
        }

        @Test
        @DisplayName("markAvailability should treat an empty exclusion list as excluding nothing")
        void markAvailability_shouldTreatEmptyExclusionListAsNoExclusions() throws Exception {
            SpecialistAvailabilityDetail detail =
                    availabilityDetail(dayOf(2026, 3, 2), dayOf(2026, 3, 8), new ArrayList<>());
            when(specialistAvailabilityRepo.findByConfiguredDateBetweenAndUserID(any(), any(), eq(USER_ID)))
                    .thenReturn(new ArrayList<>());
            when(specialistAvailabilityDetailRepo.save(detail)).thenReturn(detail);

            schedulingService.markAvailability(detail);

            assertEquals(7, captureSavedDays().size());
        }

        @Test
        @DisplayName("markAvailability should abort without saving when a stored day is malformed")
        void markAvailability_shouldAbortWithoutSavingOnAMalformedDay() {
            Date day = dayOf(2026, 3, 2);
            // A day shorter than a full 288-slot string makes the range update fail.
            SpecialistAvailability corrupt = storedAvailability(day, "AAAA");
            SpecialistAvailabilityDetail detail = availabilityDetail(day, day, null);
            when(specialistAvailabilityRepo.findByConfiguredDateBetweenAndUserID(any(), any(), eq(USER_ID)))
                    .thenReturn(new ArrayList<>(List.of(corrupt)));

            assertThrows(StringIndexOutOfBoundsException.class, () -> schedulingService.markAvailability(detail));

            verify(specialistAvailabilityRepo, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("markUnavailability")
    class MarkUnavailabilityTests {

        @Test
        @DisplayName("markUnavailability should close the requested hours of a configured day")
        void markUnavailability_shouldCloseRequestedHours() throws Exception {
            Date day = dayOf(2026, 3, 2);
            SpecialistAvailability existing = storedAvailability(day, dayWith('A', 8, 12));
            SpecialistAvailabilityDetail detail = availabilityDetail(day, day, null);
            when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(any(), eq(USER_ID)))
                    .thenReturn(existing);
            when(specialistAvailabilityDetailRepo.save(detail)).thenReturn(detail);

            SpecialistAvailabilityDetail saved = schedulingService.markUnavailability(detail);

            assertFalse(saved.getIsAvailability(), "unmarking must flag the detail as unavailable");
            assertEquals(CREATED_BY, existing.getModifiedBy());
            assertEquals('U', existing.getTimeSlot().charAt(slotIndex(9, 0)));
            assertEquals('A', existing.getTimeSlot().charAt(slotIndex(8, 0)),
                    "hours outside the request stay open");
            assertEquals('A', existing.getTimeSlot().charAt(slotIndex(11, 0)));
        }

        @Test
        @DisplayName("markUnavailability should create a fresh closed day when none is configured")
        void markUnavailability_shouldCreateAFreshClosedDay() throws Exception {
            Date day = dayOf(2026, 3, 2);
            SpecialistAvailabilityDetail detail = availabilityDetail(day, day, null);
            when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(any(), eq(USER_ID))).thenReturn(null);
            when(specialistAvailabilityDetailRepo.save(detail)).thenReturn(detail);

            schedulingService.markUnavailability(detail);

            List<SpecialistAvailability> days = captureSavedDays();
            assertEquals(1, days.size());
            assertEquals(emptyDay(), days.get(0).getTimeSlot());
            assertEquals(CREATED_BY, days.get(0).getCreatedBy());
            assertNull(days.get(0).getModifiedBy());
        }

        @Test
        @DisplayName("markUnavailability should refuse to close hours that hold a booking")
        void markUnavailability_shouldRefuseToCloseBookedHours() {
            Date day = dayOf(2026, 3, 2);
            SpecialistAvailability existing = storedAvailability(day, dayWith('B', 9, 10));
            SpecialistAvailabilityDetail detail = availabilityDetail(day, day, null);
            when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(any(), eq(USER_ID)))
                    .thenReturn(existing);

            TMException thrown =
                    assertThrows(TMException.class, () -> schedulingService.markUnavailability(detail));

            assertEquals("Appointment Booked. Please Cancel the Consultation first", thrown.getMessage());
            verify(specialistAvailabilityRepo, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("fetchavailability")
    class FetchAvailabilityTests {

        @Test
        @DisplayName("fetchavailability should split a configured day into fixed-width slots")
        void fetchavailability_shouldSplitConfiguredDayIntoSlots() {
            Date day = dayOf(2026, 3, 2);
            SpecialistAvailability existing = storedAvailability(day, dayWith('A', 9, 11));
            SpecialistInput2 input = slotRequest(day, null, null);
            when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(day, USER_ID)).thenReturn(existing);

            SpecialistAvailability result = schedulingService.fetchavailability(input);

            assertSame(existing, result);
            assertEquals(SLOTS_PER_DAY, result.getSlots().size(), "a five-minute slot size yields 288 slots");
            assertEquals("Available", result.getSlots().get(slotIndex(9, 0)).getStatus());
            assertEquals("Unavailable", result.getSlots().get(slotIndex(8, 0)).getStatus());
        }

        @Test
        @DisplayName("fetchavailability should answer an empty day when nothing is configured")
        void fetchavailability_shouldAnswerEmptyDayWhenNothingConfigured() {
            Date day = dayOf(2026, 3, 2);
            SpecialistInput2 input = slotRequest(day, null, null);
            when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(day, USER_ID)).thenReturn(null);

            SpecialistAvailability result = schedulingService.fetchavailability(input);

            assertEquals(USER_ID, result.getUserID());
            assertSame(day, result.getConfiguredDate());
            assertTrue(result.getSlots().isEmpty());
            assertNull(result.getTimeSlot());
        }
    }

    @Nested
    @DisplayName("getslotsplit")
    class GetSlotSplitTests {

        @Test
        @DisplayName("getslotsplit should classify each slot as available, booked or unavailable")
        void getslotsplit_shouldClassifyEachSlot() {
            StringBuilder day = new StringBuilder(dayWith('A', 9, 11));
            day.setCharAt(slotIndex(10, 0), 'B');

            List<Slot> slots = schedulingService.getslotsplit(day.toString());

            assertEquals(SLOTS_PER_DAY, slots.size());
            assertEquals("Available", slots.get(slotIndex(9, 0)).getStatus());
            assertEquals("Booked", slots.get(slotIndex(10, 0)).getStatus());
            assertEquals("Unavailable", slots.get(slotIndex(0, 0)).getStatus());
            assertEquals(LocalTime.of(9, 0), slots.get(slotIndex(9, 0)).getFromTime());
        }
    }

    @Nested
    @DisplayName("bookSlot")
    class BookSlotTests {

        @Test
        @DisplayName("bookSlot should book the requested window of an open day")
        void bookSlot_shouldBookRequestedWindow() throws Exception {
            Date day = dayOf(2026, 3, 2);
            SpecialistAvailability existing = storedAvailability(day, dayWith('A', 9, 11));
            SpecialistInput2 input = slotRequest(day, LocalTime.of(9, 30), LocalTime.of(10, 0));
            input.setModifiedBy("reception-desk");
            when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(any(), eq(USER_ID)))
                    .thenReturn(existing);

            assertEquals("Booked", schedulingService.bookSlot(input, 'B'));

            assertEquals('B', existing.getTimeSlot().charAt(slotIndex(9, 30)));
            assertEquals('B', existing.getTimeSlot().charAt(slotIndex(9, 55)));
            assertEquals('A', existing.getTimeSlot().charAt(slotIndex(10, 0)),
                    "the closing minute of the window stays free");
            assertEquals("reception-desk", existing.getModifiedBy());
            verify(specialistAvailabilityRepo).save(existing);
        }

        @Test
        @DisplayName("bookSlot should leave the modifier untouched when the request carries none")
        void bookSlot_shouldLeaveModifierUntouchedWhenAbsent() throws Exception {
            Date day = dayOf(2026, 3, 2);
            SpecialistAvailability existing = storedAvailability(day, dayWith('A', 9, 11));
            when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(any(), eq(USER_ID)))
                    .thenReturn(existing);

            schedulingService.bookSlot(slotRequest(day, LocalTime.of(9, 30), LocalTime.of(10, 0)), 'B');

            assertNull(existing.getModifiedBy());
        }

        @Test
        @DisplayName("bookSlot should reject a window that is already booked")
        void bookSlot_shouldRejectAnAlreadyBookedWindow() {
            Date day = dayOf(2026, 3, 2);
            SpecialistAvailability existing = storedAvailability(day, dayWith('B', 9, 11));
            SpecialistInput2 input = slotRequest(day, LocalTime.of(9, 30), LocalTime.of(10, 0));
            when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(any(), eq(USER_ID)))
                    .thenReturn(existing);

            TMException thrown = assertThrows(TMException.class, () -> schedulingService.bookSlot(input, 'B'));

            assertEquals("Already Booked Slot", thrown.getMessage());
            verify(specialistAvailabilityRepo, never()).save(any(SpecialistAvailability.class));
        }

        @Test
        @DisplayName("bookSlot should reject a window in which the specialist is not on duty")
        void bookSlot_shouldRejectAWindowOutsideDutyHours() {
            Date day = dayOf(2026, 3, 2);
            SpecialistAvailability existing = storedAvailability(day, dayWith('A', 9, 11));
            SpecialistInput2 input = slotRequest(day, LocalTime.of(12, 0), LocalTime.of(12, 30));
            when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(any(), eq(USER_ID)))
                    .thenReturn(existing);

            TMException thrown = assertThrows(TMException.class, () -> schedulingService.bookSlot(input, 'B'));

            assertEquals("Specialist unavailable at that time", thrown.getMessage());
        }

        @Test
        @DisplayName("bookSlot should reject a day the specialist has not configured at all")
        void bookSlot_shouldRejectAnUnconfiguredDay() {
            Date day = dayOf(2026, 3, 2);
            SpecialistInput2 input = slotRequest(day, LocalTime.of(9, 30), LocalTime.of(10, 0));
            when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(any(), eq(USER_ID))).thenReturn(null);

            TMException thrown = assertThrows(TMException.class, () -> schedulingService.bookSlot(input, 'B'));

            assertEquals("Doctor not available", thrown.getMessage());
        }

        @Test
        @DisplayName("bookSlot with a cancellation should release the booked window back to available")
        void bookSlot_withCancellation_shouldReleaseTheWindow() throws Exception {
            Date day = dayOf(2026, 3, 2);
            SpecialistAvailability existing = storedAvailability(day, dayWith('B', 9, 11));
            SpecialistInput2 input = slotRequest(day, LocalTime.of(9, 30), LocalTime.of(10, 0));
            when(specialistAvailabilityRepo.findOneByConfiguredDateAndUserID(any(), eq(USER_ID)))
                    .thenReturn(existing);

            assertEquals("Booked", schedulingService.bookSlot(input, 'C'));

            assertEquals('A', existing.getTimeSlot().charAt(slotIndex(9, 30)));
            assertEquals('B', existing.getTimeSlot().charAt(slotIndex(10, 30)),
                    "bookings outside the cancelled window survive");
        }
    }

    @Nested
    @DisplayName("fetchmonthavailability")
    class FetchMonthAvailabilityTests {

        @Test
        @DisplayName("fetchmonthavailability should collapse each day into runs of available and booked slots")
        void fetchmonthavailability_shouldCollapseDaysIntoRuns() {
            StringBuilder day = new StringBuilder(dayWith('A', 9, 11));
            for (int i = slotIndex(10, 0); i < slotIndex(10, 30); i++) {
                day.setCharAt(i, 'B');
            }
            SpecialistAvailability stored = storedAvailability(dayOf(2026, 3, 2), day.toString());
            when(specialistAvailabilityRepo.findByMonthAndUserID(2, 3, 2026, USER_ID))
                    .thenReturn(new ArrayList<>(List.of(stored)));

            List<SpecialistAvailability> result = schedulingService
                    .fetchmonthavailability(slotRequest(null, null, null), 2026, 3, 2);

            assertEquals(1, result.size());
            List<Slot> runs = result.get(0).getSlots();
            assertEquals(3, runs.size(), "the day collapses into available, booked and available runs");
            assertEquals("Available", runs.get(0).getStatus());
            assertEquals(LocalTime.of(9, 0), runs.get(0).getFromTime());
            assertEquals("Booked", runs.get(1).getStatus());
            assertEquals(LocalTime.of(10, 0), runs.get(1).getFromTime());
            assertEquals("Available", runs.get(2).getStatus());
        }

        @Test
        @DisplayName("fetchmonthavailability should return an empty list when the month holds no records")
        void fetchmonthavailability_shouldReturnEmptyListWhenNoRecords() {
            when(specialistAvailabilityRepo.findByMonthAndUserID(null, null, 2026, USER_ID))
                    .thenReturn(new ArrayList<>());

            List<SpecialistAvailability> result = schedulingService
                    .fetchmonthavailability(slotRequest(null, null, null), 2026, null, null);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("fetchmonthavailability should leave a fully unavailable day without any run")
        void fetchmonthavailability_shouldLeaveClosedDayWithoutRuns() {
            SpecialistAvailability stored = storedAvailability(dayOf(2026, 3, 2), emptyDay());
            when(specialistAvailabilityRepo.findByMonthAndUserID(null, 3, 2026, USER_ID))
                    .thenReturn(new ArrayList<>(List.of(stored)));

            List<SpecialistAvailability> result = schedulingService
                    .fetchmonthavailability(slotRequest(null, null, null), 2026, 3, null);

            assertTrue(result.get(0).getSlots().isEmpty());
        }
    }

    @Nested
    @DisplayName("fetchAllAvailability")
    class FetchAllAvailabilityTests {

        private SpecialistInput2 dayViewRequest(Date day) {
            SpecialistInput2 input = slotRequest(day, null, null);
            input.setProviderServiceMapID(PROVIDER_SERVICE_MAP_ID);
            input.setSpecializationID(SPECIALIZATION_ID);
            return input;
        }

        private Specialist specialist(Long userID) {
            return new Specialist("dr.rao", userID, "Anita", "K", "Rao", SPECIALIZATION_ID,
                    "Female", "Dr", "anita.rao@amrit.example.org", "9000000000", "Cardiology");
        }

        @Test
        @DisplayName("fetchAllAvailability should attach each specialist's split day to their record")
        void fetchAllAvailability_shouldAttachSplitDayToEachSpecialist() throws Exception {
            Date day = dayOf(2026, 3, 2);
            Specialist specialist = specialist(USER_ID);
            SpecialistAvailability stored = storedAvailability(day, dayWith('A', 9, 11));
            when(specialistService.getspecialistUser(PROVIDER_SERVICE_MAP_ID, SPECIALIZATION_ID, USER_ID))
                    .thenReturn(new ArrayList<>(List.of(specialist)));
            when(specialistAvailabilityRepo.findByConfiguredDateAndUserIDIn(day, List.of(USER_ID)))
                    .thenReturn(new ArrayList<>(List.of(stored)));

            List<Specialist> result = schedulingService.fetchAllAvailability(dayViewRequest(day));

            assertEquals(1, result.size());
            assertSame(stored, result.get(0).getSpecialistAvailability());
            assertEquals(SLOTS_PER_DAY, stored.getSlots().size());
            assertEquals("Available", stored.getSlots().get(slotIndex(9, 0)).getStatus());
        }

        @Test
        @DisplayName("fetchAllAvailability should leave a specialist without a configured day unattached")
        void fetchAllAvailability_shouldLeaveSpecialistWithoutADayUnattached() throws Exception {
            Date day = dayOf(2026, 3, 2);
            Specialist specialist = specialist(USER_ID);
            when(specialistService.getspecialistUser(PROVIDER_SERVICE_MAP_ID, SPECIALIZATION_ID, USER_ID))
                    .thenReturn(new ArrayList<>(List.of(specialist)));
            when(specialistAvailabilityRepo.findByConfiguredDateAndUserIDIn(day, List.of(USER_ID)))
                    .thenReturn(new ArrayList<>());

            List<Specialist> result = schedulingService.fetchAllAvailability(dayViewRequest(day));

            assertNull(result.get(0).getSpecialistAvailability());
        }

        @Test
        @DisplayName("fetchAllAvailability should attach a day that holds no raw slot string without splitting it")
        void fetchAllAvailability_shouldAttachDayWithoutSlotStringUnsplit() throws Exception {
            Date day = dayOf(2026, 3, 2);
            Specialist specialist = specialist(USER_ID);
            SpecialistAvailability stored = storedAvailability(day, null);
            when(specialistService.getspecialistUser(PROVIDER_SERVICE_MAP_ID, SPECIALIZATION_ID, USER_ID))
                    .thenReturn(new ArrayList<>(List.of(specialist)));
            when(specialistAvailabilityRepo.findByConfiguredDateAndUserIDIn(day, List.of(USER_ID)))
                    .thenReturn(new ArrayList<>(List.of(stored)));

            List<Specialist> result = schedulingService.fetchAllAvailability(dayViewRequest(day));

            assertSame(stored, result.get(0).getSpecialistAvailability());
            assertNull(stored.getSlots());
        }

        @Test
        @DisplayName("fetchAllAvailability should skip the availability lookup when no specialist matches")
        void fetchAllAvailability_shouldSkipLookupWhenNoSpecialistMatches() throws Exception {
            Date day = dayOf(2026, 3, 2);
            when(specialistService.getspecialistUser(PROVIDER_SERVICE_MAP_ID, SPECIALIZATION_ID, USER_ID))
                    .thenReturn(new ArrayList<>());

            List<Specialist> result = schedulingService.fetchAllAvailability(dayViewRequest(day));

            assertTrue(result.isEmpty());
            verify(specialistAvailabilityRepo, never()).findByConfiguredDateAndUserIDIn(any(), anyList());
        }

        @Test
        @DisplayName("fetchAllAvailability should tolerate a null specialist list from the lookup")
        void fetchAllAvailability_shouldTolerateNullSpecialistList() throws Exception {
            Date day = dayOf(2026, 3, 2);
            when(specialistService.getspecialistUser(PROVIDER_SERVICE_MAP_ID, SPECIALIZATION_ID, USER_ID))
                    .thenReturn(null);

            assertNull(schedulingService.fetchAllAvailability(dayViewRequest(day)));
        }

        @Test
        @DisplayName("fetchAllAvailability should propagate a specialist lookup failure")
        void fetchAllAvailability_shouldPropagateLookupFailure() throws Exception {
            Date day = dayOf(2026, 3, 2);
            when(specialistService.getspecialistUser(PROVIDER_SERVICE_MAP_ID, SPECIALIZATION_ID, USER_ID))
                    .thenThrow(new TMException("User not mapped to a parking place"));

            TMException thrown = assertThrows(TMException.class,
                    () -> schedulingService.fetchAllAvailability(dayViewRequest(day)));

            assertEquals("User not mapped to a parking place", thrown.getMessage());
        }
    }

    @Test
    @DisplayName("the service should be constructible through its repository constructor")
    void constructor_shouldBuildTheService() {
        assertNotNull(new SchedulingServiceImpl(specialistAvailabilityRepo, specialistAvailabilityDetailRepo));
    }
}
