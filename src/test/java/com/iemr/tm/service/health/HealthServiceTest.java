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
package com.iemr.tm.service.health;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HealthService Test Suite")
class HealthServiceTest {

    private static final String STATUS = "status";
    private static final String SEVERITY = "severity";

    @Mock
    private DataSource dataSource;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private HealthService healthService;

    @BeforeEach
    @DisplayName("Wire a healthy datasource and Redis template before each test")
    void setUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        // Neither lock waits nor slow queries are reported by default.
        when(resultSet.getInt(1)).thenReturn(0);
        answerPing("PONG");
        healthService = new HealthService(dataSource, redisTemplate);
    }

    @AfterEach
    @DisplayName("Release the service's executors after each test")
    void tearDown() {
        healthService.shutdown();
    }

    @SuppressWarnings("unchecked")
    private void answerPing(String reply) {
        when(redisTemplate.execute(any(RedisCallback.class))).thenAnswer(invocation -> {
            RedisConnection redisConnection = mock(RedisConnection.class);
            when(redisConnection.ping()).thenReturn(reply);
            return ((RedisCallback<String>) invocation.getArgument(0)).doInRedis(redisConnection);
        });
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> componentOf(Map<String, Object> health, String component) {
        Map<String, Map<String, Object>> components =
                (Map<String, Map<String, Object>>) health.get("components");
        return components.get(component);
    }

    @Nested
    @DisplayName("checkHealth")
    class CheckHealthTests {

        @Test
        @DisplayName("checkHealth should report UP when MySQL and Redis both answer")
        void checkHealth_shouldReportUpWhenEverythingAnswers() {
            Map<String, Object> health = healthService.checkHealth();

            assertEquals("UP", health.get(STATUS));
            assertEquals("UP", componentOf(health, "mysql").get(STATUS));
            assertEquals("UP", componentOf(health, "redis").get(STATUS));
            assertEquals("OK", componentOf(health, "mysql").get(SEVERITY));
            assertNotNull(health.get("timestamp"));
            assertNotNull(componentOf(health, "redis").get("responseTimeMs"));
        }

        @Test
        @DisplayName("checkHealth should report DOWN when the database connection fails")
        void checkHealth_shouldReportDownWhenDatabaseFails() throws Exception {
            when(dataSource.getConnection()).thenThrow(new SQLException("connection refused"));

            Map<String, Object> health = healthService.checkHealth();

            assertEquals("DOWN", health.get(STATUS));
            Map<String, Object> mysql = componentOf(health, "mysql");
            assertEquals("DOWN", mysql.get(STATUS));
            assertEquals("CRITICAL", mysql.get(SEVERITY));
            assertEquals("MySQL connection failed", mysql.get("error"));
        }

        @Test
        @DisplayName("checkHealth should report DOWN when the health query yields no row")
        void checkHealth_shouldReportDownWhenQueryYieldsNoRow() throws Exception {
            when(resultSet.next()).thenReturn(false);

            Map<String, Object> health = healthService.checkHealth();

            assertEquals("DOWN", health.get(STATUS));
            assertEquals("No result from health check query", componentOf(health, "mysql").get("error"));
        }

        @Test
        @DisplayName("checkHealth should report DOWN when Redis answers something other than PONG")
        void checkHealth_shouldReportDownWhenRedisDoesNotPong() {
            answerPing("NOPE");

            Map<String, Object> health = healthService.checkHealth();

            assertEquals("DOWN", health.get(STATUS));
            Map<String, Object> redis = componentOf(health, "redis");
            assertEquals("DOWN", redis.get(STATUS));
            assertEquals("Redis PING failed", redis.get("error"));
        }

        @Test
        @DisplayName("checkHealth should report DOWN when the Redis connection fails")
        void checkHealth_shouldReportDownWhenRedisConnectionFails() {
            when(redisTemplate.execute(any(RedisCallback.class)))
                    .thenThrow(new RedisConnectionFailureException("redis down"));

            Map<String, Object> health = healthService.checkHealth();

            assertEquals("DOWN", health.get(STATUS));
            assertEquals("Redis connection failed", componentOf(health, "redis").get("error"));
        }

        @Test
        @DisplayName("checkHealth should skip Redis and stay UP when no Redis template is configured")
        void checkHealth_shouldSkipRedisWhenNotConfigured() {
            HealthService withoutRedis = new HealthService(dataSource, null);
            try {
                Map<String, Object> health = withoutRedis.checkHealth();

                assertEquals("UP", health.get(STATUS));
                Map<String, Object> redis = componentOf(health, "redis");
                assertEquals("UP", redis.get(STATUS));
                assertEquals("Redis not configured — skipped", redis.get("message"));
            } finally {
                withoutRedis.shutdown();
            }
        }

        @Test
        @DisplayName("checkHealth should mark both components DOWN once the service has shut down")
        void checkHealth_shouldMarkComponentsDownAfterShutdown() {
            healthService.shutdown();

            Map<String, Object> health = healthService.checkHealth();

            assertEquals("DOWN", health.get(STATUS));
            assertEquals("MySQL health check did not complete in time",
                    componentOf(health, "mysql").get("error"));
            assertEquals("Redis health check did not complete in time",
                    componentOf(health, "redis").get("error"));
        }
    }

    @Nested
    @DisplayName("Advanced MySQL diagnostics")
    class AdvancedDiagnosticsTests {

        @Test
        @DisplayName("checkHealth should report DEGRADED when the database reports waiting locks")
        void checkHealth_shouldReportDegradedOnLockWaits() throws Exception {
            when(resultSet.getInt(1)).thenReturn(2);

            Map<String, Object> health = healthService.checkHealth();

            assertEquals("DEGRADED", health.get(STATUS));
            Map<String, Object> mysql = componentOf(health, "mysql");
            assertEquals("DEGRADED", mysql.get(STATUS));
            assertEquals("WARNING", mysql.get(SEVERITY));
            assertFalse(mysql.containsKey("error"), "a degraded component reports no hard error");
        }

        @Test
        @DisplayName("checkHealth should report DEGRADED when the diagnostics cannot borrow a connection")
        void checkHealth_shouldReportDegradedWhenDiagnosticsCannotConnect() throws Exception {
            // The basic probe succeeds; the follow-up diagnostic pass finds the pool empty.
            when(dataSource.getConnection())
                    .thenReturn(connection)
                    .thenThrow(new SQLException("pool exhausted"));

            Map<String, Object> health = healthService.checkHealth();

            assertEquals("DEGRADED", health.get(STATUS));
        }

        @Test
        @DisplayName("checkHealth should stay UP when a diagnostic probe itself errors but the database answers")
        void checkHealth_shouldStayUpWhenADiagnosticProbeErrors() throws Exception {
            doThrow(new SQLException("diagnostics unavailable")).when(preparedStatement).setQueryTimeout(2);

            assertEquals("UP", healthService.checkHealth().get(STATUS),
                    "a probe that cannot run must not be read as a database problem");
        }

        @Test
        @DisplayName("checkHealth should report DEGRADED when the connection pool is close to exhaustion")
        void checkHealth_shouldReportDegradedOnPoolExhaustion() throws Exception {
            HikariDataSource hikariDataSource = mock(HikariDataSource.class);
            HikariPoolMXBean poolMXBean = mock(HikariPoolMXBean.class);
            when(hikariDataSource.getConnection()).thenReturn(connection);
            when(hikariDataSource.getHikariPoolMXBean()).thenReturn(poolMXBean);
            when(hikariDataSource.getMaximumPoolSize()).thenReturn(10);
            when(poolMXBean.getActiveConnections()).thenReturn(9);

            HealthService pooledService = new HealthService(hikariDataSource, redisTemplate);
            try {
                assertEquals("DEGRADED", pooledService.checkHealth().get(STATUS));
            } finally {
                pooledService.shutdown();
            }
        }

        @Test
        @DisplayName("checkHealth should stay UP when the pool is comfortably below its ceiling")
        void checkHealth_shouldStayUpWhenPoolHasHeadroom() throws Exception {
            HikariDataSource hikariDataSource = mock(HikariDataSource.class);
            HikariPoolMXBean poolMXBean = mock(HikariPoolMXBean.class);
            when(hikariDataSource.getConnection()).thenReturn(connection);
            when(hikariDataSource.getHikariPoolMXBean()).thenReturn(poolMXBean);
            when(hikariDataSource.getMaximumPoolSize()).thenReturn(10);
            when(poolMXBean.getActiveConnections()).thenReturn(2);

            HealthService pooledService = new HealthService(hikariDataSource, redisTemplate);
            try {
                assertEquals("UP", pooledService.checkHealth().get(STATUS));
            } finally {
                pooledService.shutdown();
            }
        }

        @Test
        @DisplayName("checkHealth should reuse the cached diagnostic verdict inside the throttle window")
        void checkHealth_shouldReuseCachedDiagnosticVerdict() throws Exception {
            when(resultSet.getInt(1)).thenReturn(2);
            assertEquals("DEGRADED", healthService.checkHealth().get(STATUS));

            // The database now looks healthy, but the throttle window has not elapsed.
            when(resultSet.getInt(1)).thenReturn(0);

            assertEquals("DEGRADED", healthService.checkHealth().get(STATUS),
                    "the cached diagnostic verdict must survive the throttle window");
        }

        @Test
        @DisplayName("checkHealth should report DEGRADED when more than three slow queries are running")
        void checkHealth_shouldReportDegradedOnSlowQueries() throws Exception {
            // The lock-wait probe stays clean while the slow-query probe reports four queries.
            doAnswer(invocation -> {
                when(resultSet.getInt(1)).thenReturn(4);
                return null;
            }).when(preparedStatement).setInt(anyInt(), anyInt());

            assertEquals("DEGRADED", healthService.checkHealth().get(STATUS));
        }
    }

    @Nested
    @DisplayName("shutdown")
    class ShutdownTests {

        @Test
        @DisplayName("shutdown should be safe to call more than once")
        void shutdown_shouldBeIdempotent() {
            healthService.shutdown();

            assertTrue(healthService.checkHealth().containsKey("components"),
                    "a shut-down service still answers a structured health payload");
            healthService.shutdown();
        }
    }

    @Nested
    @DisplayName("Pool metrics read over JMX")
    class JmxPoolMetricsTests {

        private static final String POOL_OBJECT_NAME = "com.zaxxer.hikari:type=Pool (scheduler-test)";

        private ObjectName objectName;

        @AfterEach
        @DisplayName("Unregister the fake pool MBean after each test")
        void unregisterPoolMBean() throws Exception {
            if (objectName != null) {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
                objectName = null;
            }
        }

        /** Publishes a HikariCP-shaped pool MBean so the JMX fallback finds something to read. */
        private void publishPool(Object activeConnections, Object maximumPoolSize) throws Exception {
            objectName = new ObjectName(POOL_OBJECT_NAME);
            ManagementFactory.getPlatformMBeanServer()
                    .registerMBean(new FakePool(activeConnections, maximumPoolSize), objectName);
        }

        /** A HikariDataSource that publishes no MXBean, forcing the JMX fallback. */
        private HikariDataSource dataSourceWithoutMxBean() throws Exception {
            HikariDataSource hikariDataSource = mock(HikariDataSource.class);
            when(hikariDataSource.getConnection()).thenReturn(connection);
            when(hikariDataSource.getHikariPoolMXBean()).thenReturn(null);
            return hikariDataSource;
        }

        @Test
        @DisplayName("checkHealth should report DEGRADED when the JMX pool is close to exhaustion")
        void checkHealth_shouldReportDegradedFromJmxMetrics() throws Exception {
            publishPool(9, 10);

            HealthService service = new HealthService(dataSourceWithoutMxBean(), redisTemplate);
            try {
                assertEquals("DEGRADED", service.checkHealth().get(STATUS));
            } finally {
                service.shutdown();
            }
        }

        @Test
        @DisplayName("checkHealth should stay UP when the JMX pool has headroom")
        void checkHealth_shouldStayUpWhenJmxPoolHasHeadroom() throws Exception {
            publishPool(2, 10);

            HealthService service = new HealthService(dataSourceWithoutMxBean(), redisTemplate);
            try {
                assertEquals("UP", service.checkHealth().get(STATUS));
            } finally {
                service.shutdown();
            }
        }

        @Test
        @DisplayName("checkHealth should stay UP when the JMX pool reports unreadable metrics")
        void checkHealth_shouldStayUpWhenJmxMetricsAreUnreadable() throws Exception {
            publishPool("not a number", 10);

            HealthService service = new HealthService(dataSourceWithoutMxBean(), redisTemplate);
            try {
                assertEquals("UP", service.checkHealth().get(STATUS),
                        "metrics that cannot be read must not be reported as a pool problem");
            } finally {
                service.shutdown();
            }
        }

        @Test
        @DisplayName("checkHealth should stay UP when no pool is published over JMX at all")
        void checkHealth_shouldStayUpWhenNoPoolIsPublished() throws Exception {
            HealthService service = new HealthService(dataSourceWithoutMxBean(), redisTemplate);
            try {
                assertEquals("UP", service.checkHealth().get(STATUS));
            } finally {
                service.shutdown();
            }
        }
    }

    /** A minimal stand-in for the HikariCP pool MBean the JMX fallback looks for. */
    public static final class FakePool implements DynamicMBean {

        private final Object activeConnections;
        private final Object maximumPoolSize;

        FakePool(Object activeConnections, Object maximumPoolSize) {
            this.activeConnections = activeConnections;
            this.maximumPoolSize = maximumPoolSize;
        }

        @Override
        public Object getAttribute(String attribute) {
            return "ActiveConnections".equals(attribute) ? activeConnections : maximumPoolSize;
        }

        @Override
        public void setAttribute(Attribute attribute) {
            throw new UnsupportedOperationException("the fake pool is read-only");
        }

        @Override
        public AttributeList getAttributes(String[] attributes) {
            return new AttributeList();
        }

        @Override
        public AttributeList setAttributes(AttributeList attributes) {
            return new AttributeList();
        }

        @Override
        public Object invoke(String actionName, Object[] params, String[] signature) {
            throw new UnsupportedOperationException("the fake pool exposes no operations");
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            return new MBeanInfo(FakePool.class.getName(), "fake HikariCP pool",
                    new MBeanAttributeInfo[] {
                            new MBeanAttributeInfo("ActiveConnections", "java.lang.Integer", "", true, false, false),
                            new MBeanAttributeInfo("MaximumPoolSize", "java.lang.Integer", "", true, false, false) },
                    null, null, null);
        }
    }
}
