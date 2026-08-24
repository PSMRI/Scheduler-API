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
package com.iemr.tm.service.specialist;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iemr.tm.data.specialist.MUser;
import com.iemr.tm.data.specialist.Specialist;
import com.iemr.tm.data.specialist.Specialization;
import com.iemr.tm.repo.specialist.SpecializationRepo;
import com.iemr.tm.utils.exception.TMException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpecialistServiceImpl Test Suite")
class SpecialistServiceImplTest {

    private static final Long USER_ID = 4021L;
    private static final Long PROVIDER_SERVICE_MAP_ID = 8L;
    private static final Long SPECIALIZATION_ID = 12L;
    private static final Long PARKING_PLACE_ID = 55L;

    @Mock
    private SpecializationRepo specializationRepo;

    @InjectMocks
    private SpecialistServiceImpl specialistService;

    /** One row of the PR_FetchSpecialist stored procedure result. */
    private Object[] specialistRow() {
        return new Object[] { "dr.rao", 4021, "Anita", "K", "Rao", 12,
                "Female", "Dr", "anita.rao@amrit.example.org", "9000000000", "Cardiology" };
    }

    /** One row of the all-specialists-for-a-provider query. */
    private Object[] providerSpecialistRow() {
        return new Object[] { 4021, "Anita", "K", "Rao", "Cardiology", 8, 12 };
    }

    @Nested
    @DisplayName("getspecialization")
    class GetSpecializationTests {

        @Test
        @DisplayName("getspecialization should return the specializations that are not deleted")
        void getspecialization_shouldReturnUndeletedSpecializations() {
            Specialization specialization = new Specialization();
            specialization.setSpecializationID(SPECIALIZATION_ID);
            specialization.setSpecialization("Cardiology");
            List<Specialization> stored = List.of(specialization);
            when(specializationRepo.findByDeleted(false)).thenReturn(stored);

            assertSame(stored, specialistService.getspecialization());
        }

        @Test
        @DisplayName("getspecialization should pass an empty master list straight through")
        void getspecialization_shouldPassEmptyMasterListThrough() {
            when(specializationRepo.findByDeleted(false)).thenReturn(new ArrayList<>());

            assertTrue(specialistService.getspecialization().isEmpty());
        }
    }

    @Nested
    @DisplayName("getspecialistUser")
    class GetSpecialistUserTests {

        @Test
        @DisplayName("getspecialistUser should map every stored-procedure column onto a specialist")
        void getspecialistUser_shouldMapEveryColumn() throws Exception {
            when(specializationRepo.getPPID(PROVIDER_SERVICE_MAP_ID, USER_ID)).thenReturn(PARKING_PLACE_ID);
            when(specializationRepo.getspecialistSP(SPECIALIZATION_ID, PARKING_PLACE_ID))
                    .thenReturn(new ArrayList<>(List.<Object[]>of(specialistRow())));

            List<Specialist> result =
                    specialistService.getspecialistUser(PROVIDER_SERVICE_MAP_ID, SPECIALIZATION_ID, USER_ID);

            assertEquals(1, result.size());
            Specialist specialist = result.get(0);
            assertEquals("dr.rao", specialist.getUserName());
            assertEquals(USER_ID, specialist.getUserID());
            assertEquals("Anita", specialist.getFirstName());
            assertEquals("K", specialist.getMiddleName());
            assertEquals("Rao", specialist.getLastName());
            assertEquals(SPECIALIZATION_ID, specialist.getSpecializationID());
            assertEquals("Female", specialist.getGenderName());
            assertEquals("Dr", specialist.getTitleName());
            assertEquals("anita.rao@amrit.example.org", specialist.getEmail());
            assertEquals("9000000000", specialist.getContactNo());
            assertEquals("Cardiology", specialist.getSpecialization());
        }

        @Test
        @DisplayName("getspecialistUser should return an empty list when the parking place has no specialists")
        void getspecialistUser_shouldReturnEmptyListWhenNoSpecialists() throws Exception {
            when(specializationRepo.getPPID(PROVIDER_SERVICE_MAP_ID, USER_ID)).thenReturn(PARKING_PLACE_ID);
            when(specializationRepo.getspecialistSP(SPECIALIZATION_ID, PARKING_PLACE_ID))
                    .thenReturn(new ArrayList<>());

            assertTrue(specialistService
                    .getspecialistUser(PROVIDER_SERVICE_MAP_ID, SPECIALIZATION_ID, USER_ID).isEmpty());
        }

        @Test
        @DisplayName("getspecialistUser should reject a user who is not mapped to a parking place")
        void getspecialistUser_shouldRejectUnmappedUser() {
            when(specializationRepo.getPPID(PROVIDER_SERVICE_MAP_ID, USER_ID)).thenReturn(null);

            TMException thrown = assertThrows(TMException.class,
                    () -> specialistService.getspecialistUser(PROVIDER_SERVICE_MAP_ID, SPECIALIZATION_ID, USER_ID));

            assertEquals("User not mapped to a parking place", thrown.getMessage());
            verify(specializationRepo, never()).getspecialistSP(SPECIALIZATION_ID, PARKING_PLACE_ID);
        }
    }

    @Nested
    @DisplayName("getAllSpecialist")
    class GetAllSpecialistTests {

        @Test
        @DisplayName("getAllSpecialist should map the provider query onto specialists without contact details")
        void getAllSpecialist_shouldMapProviderQuery() {
            when(specializationRepo.getAllSPecialistForProvider(PROVIDER_SERVICE_MAP_ID))
                    .thenReturn(new ArrayList<>(List.<Object[]>of(providerSpecialistRow())));

            List<Specialist> result = specialistService.getAllSpecialist(PROVIDER_SERVICE_MAP_ID);

            assertEquals(1, result.size());
            Specialist specialist = result.get(0);
            assertEquals(USER_ID, specialist.getUserID());
            assertEquals("Anita", specialist.getFirstName());
            assertEquals("K", specialist.getMiddleName());
            assertEquals("Rao", specialist.getLastName());
            assertEquals(SPECIALIZATION_ID, specialist.getSpecializationID());
            assertEquals("Cardiology", specialist.getSpecialization());
            assertNull(specialist.getUserName());
            assertNull(specialist.getEmail());
            assertNull(specialist.getContactNo());
        }

        @Test
        @DisplayName("getAllSpecialist should return an empty list when the provider has no specialists")
        void getAllSpecialist_shouldReturnEmptyListWhenNoSpecialists() {
            when(specializationRepo.getAllSPecialistForProvider(PROVIDER_SERVICE_MAP_ID))
                    .thenReturn(new ArrayList<>());

            assertTrue(specialistService.getAllSpecialist(PROVIDER_SERVICE_MAP_ID).isEmpty());
        }
    }

    @Nested
    @DisplayName("getinfo")
    class GetInfoTests {

        /** The specialist-info query returns a single row nested inside the result array. */
        private Object[] infoResult() {
            return new Object[] { new Object[] { 4021, "Dr", "Anita", "K", "Rao", 2,
                    "Female", 12, "Cardiology", "9000000000", "anita.rao@amrit.example.org" } };
        }

        @Test
        @DisplayName("getinfo should map the queried columns onto the user")
        void getinfo_shouldMapQueriedColumns() {
            when(specializationRepo.getspecialistinfo(USER_ID)).thenReturn(infoResult());

            MUser user = specialistService.getinfo(USER_ID);

            assertEquals(USER_ID, user.getUserID());
            assertEquals("Dr", user.getTitleName());
            assertEquals("Anita", user.getFirstName());
            assertEquals("K", user.getMiddleName());
            assertEquals("Rao", user.getLastName());
            assertEquals("Female", user.getGender());
            assertEquals("Cardiology", user.getSpecialization());
            assertEquals("9000000000", user.getContactNumber());
            assertEquals("anita.rao@amrit.example.org", user.getEmailID());
        }

        @Test
        @DisplayName("getinfo should answer an empty user when the query returns nothing")
        void getinfo_shouldAnswerEmptyUserWhenQueryReturnsNothing() {
            when(specializationRepo.getspecialistinfo(USER_ID)).thenReturn(null);

            MUser user = specialistService.getinfo(USER_ID);

            assertNull(user.getUserID());
            assertNull(user.getFirstName());
        }

        @Test
        @DisplayName("getinfo should answer an empty user when the query returns an empty row set")
        void getinfo_shouldAnswerEmptyUserForEmptyRowSet() {
            when(specializationRepo.getspecialistinfo(USER_ID)).thenReturn(new Object[0]);

            assertNull(specialistService.getinfo(USER_ID).getUserID());
        }
    }
}
