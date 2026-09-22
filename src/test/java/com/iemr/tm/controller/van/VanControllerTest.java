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
package com.iemr.tm.controller.van;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iemr.tm.data.van.MVan;
import com.iemr.tm.service.van.VanService;
import com.iemr.tm.utils.response.OutputResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VanController Test Suite")
class VanControllerTest {

    private static final Integer VAN_ID = 71;

    @Mock
    private VanService vanService;

    @InjectMocks
    private VanController vanController;

    private static int statusCodeOf(String response) {
        return Integer.parseInt(response.replaceAll(".*\"statusCode\":(-?\\d+).*", "$1"));
    }

    @Test
    @DisplayName("getvan should answer the van the service resolves")
    void getvan_shouldAnswerResolvedVan() {
        MVan van = new MVan();
        van.setVanID(VAN_ID);
        van.setVanName("TM Van 12");
        van.setVehicalNo("KA-01-AB-1234");
        when(vanService.getvan(VAN_ID)).thenReturn(van);

        String response = vanController.markavailability(VAN_ID);

        assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
        assertTrue(response.contains("\"vanName\":\"TM Van 12\""), response);
        assertTrue(response.contains("\"vehicalNo\":\"KA-01-AB-1234\""), response);
    }

    @Test
    @DisplayName("getvan should answer an empty van rather than fail for an unknown id")
    void getvan_shouldAnswerEmptyVanForUnknownId() {
        when(vanService.getvan(VAN_ID)).thenReturn(null);

        String response = vanController.markavailability(VAN_ID);

        assertEquals(OutputResponse.SUCCESS, statusCodeOf(response));
        assertFalse(response.contains("\"vanID\""), response);
    }

    @Test
    @DisplayName("getvan should report a lookup failure")
    void getvan_shouldReportLookupFailure() {
        when(vanService.getvan(VAN_ID)).thenThrow(new IllegalStateException("van lookup failed"));

        assertEquals(OutputResponse.GENERIC_FAILURE, statusCodeOf(vanController.markavailability(VAN_ID)));
    }
}
