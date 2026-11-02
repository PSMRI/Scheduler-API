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
package com.iemr.tm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Application bootstrap Test Suite")
class RoleMasterApplicationTest {

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Test
    @DisplayName("main should hand the application class to Spring Boot")
    void main_shouldHandApplicationClassToSpringBoot() {
        String[] args = { "--spring.profiles.active=test" };

        try (var springApplication = mockStatic(SpringApplication.class)) {
            RoleMasterApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(RoleMasterApplication.class, args));
        }
    }

    @Test
    @DisplayName("redisTemplate should bind the supplied connection factory")
    void redisTemplate_shouldBindSuppliedConnectionFactory() {
        RedisTemplate<String, Object> template = new RoleMasterApplication().redisTemplate(connectionFactory);

        assertNotNull(template);
        assertSame(connectionFactory, template.getConnectionFactory());
    }

    @Test
    @DisplayName("redisTemplate should serialise keys as plain strings and values as JSON")
    void redisTemplate_shouldUseStringKeysAndJsonValues() {
        RedisTemplate<String, Object> template = new RoleMasterApplication().redisTemplate(connectionFactory);

        assertTrue(template.getKeySerializer() instanceof StringRedisSerializer);
        assertTrue(template.getValueSerializer() instanceof Jackson2JsonRedisSerializer);
    }

    @Test
    @DisplayName("the servlet initializer should register the application as the WAR source")
    void servletInitializer_shouldRegisterApplicationAsWarSource() {
        SpringApplicationBuilder builder = org.mockito.Mockito.mock(SpringApplicationBuilder.class);
        when(builder.sources(any(Class[].class))).thenReturn(builder);

        assertSame(builder, new ServletInitializer().configure(builder));
        verify(builder).sources(eq(RoleMasterApplication.class));
    }
}
