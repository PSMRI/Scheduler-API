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
package com.iemr.tm.utils.exception;

import java.io.IOException;
import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.SQLGrammarException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CustomExceptionResponse Test Suite")
class CustomExceptionResponseTest {

    private CustomExceptionResponse response;

    @BeforeEach
    @DisplayName("Create a fresh response before each test")
    void setUp() {
        response = new CustomExceptionResponse();
    }

    /** The mapping keys off the cause, so every failure is wrapped before it is reported. */
    private static Exception wrapping(Throwable cause) {
        return new Exception("scheduling request failed", cause);
    }

    private static SQLException sqlException() {
        return new SQLException("deadlock found when trying to get lock");
    }

    @Nested
    @DisplayName("Default state")
    class DefaultStateTests {

        @Test
        @DisplayName("a new response should default to a generic failure")
        void newResponse_shouldDefaultToGenericFailure() {
            assertEquals(CustomExceptionResponse.GENERIC_FAILURE, response.getStatusCode());
            assertEquals("Failed with generic error", response.getErrorMessage());
            assertEquals("FAILURE", response.getStatus());
            assertFalse(response.isSuccess());
        }

        @Test
        @DisplayName("getData should return null when no data has been set")
        void getData_shouldReturnNullWhenNoDataSet() {
            assertNull(response.getData());
        }
    }

    @Nested
    @DisplayName("setResponse")
    class SetResponseTests {

        @Test
        @DisplayName("setResponse should mark the response successful")
        void setResponse_shouldMarkResponseSuccessful() {
            response.setResponse("done");

            assertEquals(CustomExceptionResponse.SUCCESS, response.getStatusCode());
            assertEquals("Success", response.getErrorMessage());
            assertTrue(response.isSuccess());
        }

        @Test
        @DisplayName("setResponse should keep a JSON object payload as an object")
        void setResponse_shouldKeepJsonObjectPayload() {
            response.setResponse("{\"specialistAvailabilityID\":12345}");

            assertTrue(response.getData().startsWith("{"));
            assertTrue(response.getData().contains("specialistAvailabilityID"));
        }

        @Test
        @DisplayName("setResponse should keep a JSON array payload as an array")
        void setResponse_shouldKeepJsonArrayPayload() {
            response.setResponse("[1,2,3]");

            assertTrue(response.getData().startsWith("["));
        }

        @Test
        @DisplayName("setResponse should wrap a plain string payload under a response key")
        void setResponse_shouldWrapPlainStringPayload() {
            response.setResponse("plain text");

            assertTrue(response.getData().contains("response"));
            assertTrue(response.getData().contains("plain text"));
        }
    }

    @Nested
    @DisplayName("setError from a wrapped cause")
    class SetErrorFromCauseTests {

        @Test
        @DisplayName("setError should map a login failure to the user-id failure code")
        void setError_shouldMapLoginFailure() {
            response.setError(wrapping(new IEMRException("Invalid session key")));

            assertEquals(CustomExceptionResponse.USERID_FAILURE, response.getStatusCode());
            assertEquals("User login failed", response.getStatus());
        }

        @Test
        @DisplayName("setError should map every database failure to the DB exception code")
        void setError_shouldMapEveryDatabaseFailure() {
            Throwable[] databaseFailures = {
                    sqlException(),
                    new SQLGrammarException("bad column", sqlException()),
                    new DataException("value out of range", sqlException()),
                    new ConstraintViolationException("duplicate key", sqlException(), "uq_specialist_day"),
                    new GenericJDBCException("driver failure", sqlException()),
                    new JDBCConnectionException("connection lost", sqlException()),
                    new LockAcquisitionException("lock wait timeout", sqlException()),
                    new InvalidDataAccessResourceUsageException("bad statement") };

            for (Throwable failure : databaseFailures) {
                CustomExceptionResponse mapped = new CustomExceptionResponse();

                mapped.setError(wrapping(failure));

                assertEquals(CustomExceptionResponse.DB_EXCEPTION, mapped.getStatusCode(),
                        failure.getClass().getSimpleName() + " must be reported as a database failure");
                assertEquals(CustomExceptionResponse.DB_EXCEPTION_SC, mapped.getStatus());
            }
        }

        @Test
        @DisplayName("setError should map a transport failure to the environment exception code")
        void setError_shouldMapTransportFailure() {
            response.setError(wrapping(new IOException("connection reset")));

            assertEquals(CustomExceptionResponse.ENVIRONMENT_EXCEPTION, response.getStatusCode());
            assertTrue(response.getStatus().startsWith("Failed with connection issues"));
        }

        @Test
        @DisplayName("setError should map a null-pointer failure to the environment exception code")
        void setError_shouldMapNullPointerFailure() {
            response.setError(wrapping(new NullPointerException("slot detail was null")));

            assertEquals(CustomExceptionResponse.ENVIRONMENT_EXCEPTION, response.getStatusCode());
        }

        @Test
        @DisplayName("setError should fall back to a generic failure for an unmapped cause")
        void setError_shouldFallBackToGenericFailure() {
            response.setError(wrapping(new IllegalStateException("unexpected state")));

            assertEquals(CustomExceptionResponse.GENERIC_FAILURE, response.getStatusCode());
            assertEquals("scheduling request failed", response.getErrorMessage());
        }
    }

    @Nested
    @DisplayName("setError with an explicit code")
    class SetErrorWithCodeTests {

        @Test
        @DisplayName("setError should apply the supplied code, message and status")
        void setError_shouldApplySuppliedCodeMessageAndStatus() {
            response.setError(CustomExceptionResponse.NOT_FOUND, "no such specialist",
                    CustomExceptionResponse.NOT_FOUND_SC);

            assertEquals(CustomExceptionResponse.NOT_FOUND, response.getStatusCode());
            assertEquals("no such specialist", response.getErrorMessage());
            assertEquals(CustomExceptionResponse.NOT_FOUND_SC, response.getStatus());
        }

        @Test
        @DisplayName("setError should reuse the message as the status when only a code is supplied")
        void setError_shouldReuseMessageAsStatus() {
            response.setError(CustomExceptionResponse.BAD_REQUEST, "no such specialist");

            assertEquals("no such specialist", response.getStatus());
        }
    }

    @Nested
    @DisplayName("Serialisation")
    class SerialisationTests {

        @Test
        @DisplayName("toString should serialise the exposed fields and render longs as strings")
        void toString_shouldSerialiseExposedFields() {
            response.setResponse("done");

            String json = response.toString();

            assertTrue(json.contains("\"statusCode\":200"), json);
            assertTrue(json.contains("\"status\":\"Success\""), json);
        }

        @Test
        @DisplayName("toStringWithSerialization should keep the null data field in the payload")
        void toStringWithSerialization_shouldKeepNullData() {
            assertTrue(new CustomExceptionResponse().toStringWithSerialization().contains("\"data\":null"));
        }
    }
}
