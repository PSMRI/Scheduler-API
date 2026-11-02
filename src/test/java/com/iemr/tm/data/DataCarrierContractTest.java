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
package com.iemr.tm.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.iemr.tm.data.schedule.Slot;
import com.iemr.tm.data.schedule.SpecialistAvailability;
import com.iemr.tm.data.schedule.SpecialistAvailabilityDetail;
import com.iemr.tm.data.schedule.SpecialistInput;
import com.iemr.tm.data.schedule.SpecialistInput2;
import com.iemr.tm.data.specialist.MUser;
import com.iemr.tm.data.specialist.Specialist;
import com.iemr.tm.data.specialist.Specialization;
import com.iemr.tm.data.user.Users;
import com.iemr.tm.data.van.MVan;

@DisplayName("Data carrier contract Test Suite")
class DataCarrierContractTest {

    @ParameterizedTest(name = "{0} honours the accessor, equality and string contract")
    @DisplayName("every data carrier should honour its accessor, equality and string contract")
    @ValueSource(classes = {
            Slot.class,
            SpecialistAvailability.class,
            SpecialistAvailabilityDetail.class,
            SpecialistInput.class,
            SpecialistInput2.class,
            MUser.class,
            Specialist.class,
            Specialization.class,
            Users.class,
            MVan.class })
    void dataCarrier_shouldHonourBeanContract(Class<?> type) throws Exception {
        BeanContract.verify(type);
    }
}
