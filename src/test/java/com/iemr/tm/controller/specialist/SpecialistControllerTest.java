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
package com.iemr.tm.controller.specialist;

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
import com.iemr.tm.service.specialist.SpecialistService;
import com.iemr.tm.utils.exception.TMException;
import com.iemr.tm.utils.response.OutputResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpecialistController Test Suite")
class SpecialistControllerTest {

    private static final Long USER_ID = 4021L;
    private static final Long PROVIDER_SERVICE_MAP_ID = 8L;
    private static final Long SPECIALIZATION_ID = 12L;

    @Mock
    private SpecialistService specialistService;

    @InjectMocks
    private SpecialistController specialistController;

    private static int statusCodeOf(String response) {
        return Integer.parseInt(response.replaceAll(".*\"statusCode\":(-?\\d+).*", "$1"));
    }

    private Specialist specialistRequest() {
        return new Specialist(USER_ID, SPECIALIZATION_ID, PROVIDER_SERVICE_MAP_ID);
    }

    private Specialist storedSpecialist() {
        return new Specialist("dr.rao", USER_ID, "Anita", "K", "Rao", SPECIALIZATION_ID,
                "Female", "Dr", "anita.rao@amrit.example.org", "9000000000", "Cardiology");
    }

    @Nested
    @DisplayName("masterspecialization")
    class MasterSpecializationTests {

        @Test
        @DisplayName("masterspecialization should answer the master list the service resolves")
        void masterspecialization_shouldAnswerMasterList() {
            Specialization specialization = new Specialization();
            specialization.setSpecializationID(SPECIALIZATION_ID);
            specialization.setSpecialization("Cardiology");
            when(specialistService.getspecialization()).thenReturn(List.of(specialization));

            String response = specialistController.markavailability();

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
            assertTrue(response.contains("\"specialization\":\"Cardiology\""), response);
        }

        @Test
        @DisplayName("masterspecialization should report a lookup failure")
        void masterspecialization_shouldReportLookupFailure() {
            when(specialistService.getspecialization())
                    .thenThrow(new IllegalStateException("specialization lookup failed"));

            assertEquals(OutputResponse.GENERIC_FAILURE, statusCodeOf(specialistController.markavailability()));
        }
    }

    @Nested
    @DisplayName("getSpecialist")
    class GetSpecialistTests {

        @Test
        @DisplayName("getSpecialist should answer the specialists resolved for the request")
        void getSpecialist_shouldAnswerResolvedSpecialists() throws Exception {
            when(specialistService.getspecialistUser(PROVIDER_SERVICE_MAP_ID, SPECIALIZATION_ID, USER_ID))
                    .thenReturn(new ArrayList<>(List.of(storedSpecialist())));

            String response = specialistController.getSpecialist(specialistRequest());

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
            assertTrue(response.contains("\"userName\":\"dr.rao\""), response);
        }

        @Test
        @DisplayName("getSpecialist should report an unmapped user as a TM error")
        void getSpecialist_shouldReportUnmappedUser() throws Exception {
            when(specialistService.getspecialistUser(PROVIDER_SERVICE_MAP_ID, SPECIALIZATION_ID, USER_ID))
                    .thenThrow(new TMException("User not mapped to a parking place"));

            String response = specialistController.getSpecialist(specialistRequest());

            assertEquals(OutputResponse.TM_FAILURE, statusCodeOf(response));
            assertTrue(response.contains("User not mapped to a parking place"), response);
        }
    }

    @Nested
    @DisplayName("info")
    class InfoTests {

        @Test
        @DisplayName("info should answer the user the service resolves")
        void info_shouldAnswerResolvedUser() {
            MUser user = new MUser();
            user.setUserID(USER_ID);
            user.setFirstName("Anita");
            user.setSpecialization("Cardiology");
            when(specialistService.getinfo(USER_ID)).thenReturn(user);

            String response = specialistController.info(USER_ID);

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
            assertTrue(response.contains("\"firstName\":\"Anita\""), response);
        }

        @Test
        @DisplayName("info should report a null user from the service as a failure")
        void info_shouldReportNullUserAsFailure() {
            when(specialistService.getinfo(USER_ID)).thenReturn(null);

            assertEquals(OutputResponse.CODE_EXCEPTION, statusCodeOf(specialistController.info(USER_ID)));
        }
    }

    @Nested
    @DisplayName("getSpecialistAll")
    class GetSpecialistAllTests {

        @Test
        @DisplayName("getSpecialistAll should answer every specialist of the provider")
        void getSpecialistAll_shouldAnswerEveryProviderSpecialist() {
            when(specialistService.getAllSpecialist(PROVIDER_SERVICE_MAP_ID))
                    .thenReturn(new ArrayList<>(List.of(storedSpecialist())));

            String response = specialistController.getSpecialistAll(specialistRequest());

            assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
            assertTrue(response.contains("\"specialization\":\"Cardiology\""), response);
        }

        @Test
        @DisplayName("getSpecialistAll should report a lookup failure")
        void getSpecialistAll_shouldReportLookupFailure() {
            when(specialistService.getAllSpecialist(PROVIDER_SERVICE_MAP_ID))
                    .thenThrow(new IllegalStateException("provider lookup failed"));

            assertEquals(OutputResponse.GENERIC_FAILURE,
                    statusCodeOf(specialistController.getSpecialistAll(specialistRequest())));
        }
    }
}
