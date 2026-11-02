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
package com.iemr.tm.service.van;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iemr.tm.data.van.MVan;
import com.iemr.tm.repo.van.VanRepo;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VanServiceImpl Test Suite")
class VanServiceImplTest {

    private static final Integer VAN_ID = 71;

    @Mock
    private VanRepo vanRepo;

    @InjectMocks
    private VanServiceImpl vanService;

    @Test
    @DisplayName("getvan should return the van the repository holds for the id")
    void getvan_shouldReturnTheStoredVan() {
        MVan van = new MVan();
        van.setVanID(VAN_ID);
        van.setVanName("TM Van 12");
        when(vanRepo.findByVanID(VAN_ID)).thenReturn(van);

        assertSame(van, vanService.getvan(VAN_ID));
    }

    @Test
    @DisplayName("getvan should return null for an id the repository does not know")
    void getvan_shouldReturnNullForUnknownId() {
        when(vanRepo.findByVanID(VAN_ID)).thenReturn(null);

        assertNull(vanService.getvan(VAN_ID));
    }
}
