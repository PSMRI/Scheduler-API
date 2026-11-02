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

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TokenDenylist Test Suite")
class TokenDenylistTest {

    private static final String JTI = "b0f1c2d3-4e5f-6789-abcd-ef0123456789";
    private static final String KEY = "denied_" + JTI;
    private static final Long ONE_HOUR_MS = 3_600_000L;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private TokenDenylist tokenDenylist;

    @BeforeEach
    @DisplayName("Wire the value operations onto the Redis template before each test")
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("addTokenToDenylist")
    class AddTokenTests {

        @Test
        @DisplayName("addTokenToDenylist should store the prefixed key and set its expiry")
        void addTokenToDenylist_shouldStorePrefixedKeyWithExpiry() {
            tokenDenylist.addTokenToDenylist(JTI, ONE_HOUR_MS);

            verify(valueOperations).set(KEY, true);
            verify(redisTemplate).expire(KEY, ONE_HOUR_MS, TimeUnit.MILLISECONDS);
        }

        @Test
        @DisplayName("addTokenToDenylist should ignore a null jti")
        void addTokenToDenylist_shouldIgnoreNullJti() {
            tokenDenylist.addTokenToDenylist(null, ONE_HOUR_MS);

            verifyNoInteractions(valueOperations);
            verify(redisTemplate, never()).expire(KEY, ONE_HOUR_MS, TimeUnit.MILLISECONDS);
        }

        @Test
        @DisplayName("addTokenToDenylist should ignore a null expiry")
        void addTokenToDenylist_shouldIgnoreNullExpiry() {
            tokenDenylist.addTokenToDenylist(JTI, null);

            verifyNoInteractions(valueOperations);
        }

        @Test
        @DisplayName("addTokenToDenylist should ignore a non-positive expiry")
        void addTokenToDenylist_shouldIgnoreNonPositiveExpiry() {
            tokenDenylist.addTokenToDenylist(JTI, 0L);
            tokenDenylist.addTokenToDenylist(JTI, -1L);

            verifyNoInteractions(valueOperations);
        }

        @Test
        @DisplayName("addTokenToDenylist should surface a Redis failure as a runtime exception")
        void addTokenToDenylist_shouldSurfaceRedisFailure() {
            doThrow(new RedisConnectionFailureException("redis down"))
                    .when(valueOperations).set(KEY, true);

            RuntimeException thrown = assertThrows(RuntimeException.class,
                    () -> tokenDenylist.addTokenToDenylist(JTI, ONE_HOUR_MS));

            assertEquals("Failed to add token to denylist", thrown.getMessage());
        }
    }

    @Nested
    @DisplayName("isTokenDenylisted")
    class IsTokenDenylistedTests {

        @Test
        @DisplayName("isTokenDenylisted should report true when the key holds a value")
        void isTokenDenylisted_shouldReportTrueWhenKeyExists() {
            when(valueOperations.get(KEY)).thenReturn(true);

            assertTrue(tokenDenylist.isTokenDenylisted(JTI));
        }

        @Test
        @DisplayName("isTokenDenylisted should report false when the key is absent")
        void isTokenDenylisted_shouldReportFalseWhenKeyAbsent() {
            when(valueOperations.get(KEY)).thenReturn(null);

            assertFalse(tokenDenylist.isTokenDenylisted(JTI));
        }

        @Test
        @DisplayName("isTokenDenylisted should report false for a null jti without touching Redis")
        void isTokenDenylisted_shouldReportFalseForNullJti() {
            assertFalse(tokenDenylist.isTokenDenylisted(null));

            verifyNoInteractions(valueOperations);
        }

        @Test
        @DisplayName("isTokenDenylisted should fail open when Redis is unreachable")
        void isTokenDenylisted_shouldFailOpenWhenRedisIsDown() {
            when(valueOperations.get(KEY)).thenThrow(new RedisConnectionFailureException("redis down"));

            assertFalse(tokenDenylist.isTokenDenylisted(JTI),
                    "a Redis outage must not block every request");
        }
    }
}
