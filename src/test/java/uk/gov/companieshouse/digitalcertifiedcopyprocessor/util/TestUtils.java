package uk.gov.companieshouse.digitalcertifiedcopyprocessor.util;

import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.springframework.core.env.Environment;

import java.util.Map;

public class TestUtils {

    /**
     * Configures the CH Java SDKs used in WireMock based integration tests to interact with WireMock
     * stubbed/mocked API endpoints.
     * @param environment the Spring {@link Environment} the tests are run in
     * @param variables {@link EnvironmentVariables} class rule permitting environment variable manipulation
     * @return the WireMock port value used for the tests
     */
    public static String givenSdkIsConfigured(final Environment environment, final EnvironmentVariables variables) {
        final String wireMockPort = environment.getProperty("wiremock.server.port");
        variables.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        variables.set("API_URL", "http://localhost:" + wireMockPort);
        variables.set("PAYMENTS_API_URL", "http://localhost:" + wireMockPort);
        return wireMockPort;
    }

    /**
     * Configures the CH Java SDKs used in manual tests to interact with Tilt
     * hosted API endpoints.
     * @param variables {@link EnvironmentVariables} class rule permitting environment variable manipulation
     * @see #givenSdkIsConfigured(EnvironmentVariables, Map)
     */
    public static void givenSdkIsConfiguredForTilt(final EnvironmentVariables variables) {
        variables.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        variables.set("API_URL", "http://api.chs.local:4001");
        variables.set("PAYMENTS_API_URL", "http://api.chs.local:4001");
    }

    /**
     * Configures the CH Java SDKs used in manual tests to interact with API endpoints.
     * @param variables {@link EnvironmentVariables} class rule permitting environment variable manipulation
     * @param variableValues map of key-value pairs to override settings which otherwise default to acceptable
     *                       values for testing with Tilt hosted API endpoints
     */
    public static void givenSdkIsConfigured(final EnvironmentVariables variables,
                                            final Map<String, String> variableValues) {
        variables.set("CHS_API_KEY", "MGQ1MGNlYmFkYzkxZTM2MzlkNGVmMzg4ZjgxMmEz");
        variables.set("API_URL", "http://api.chs.local:4001");
        variables.set("PAYMENTS_API_URL", "http://api.chs.local:4001");
        variableValues.forEach(variables::set);
    }

}
