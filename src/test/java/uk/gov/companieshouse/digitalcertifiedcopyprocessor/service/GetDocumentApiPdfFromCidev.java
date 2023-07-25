package uk.gov.companieshouse.digitalcertifiedcopyprocessor.service;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsGetSessionTokenCredentialsProvider;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.KafkaConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.config.TestConfig;
import uk.gov.companieshouse.digitalcertifiedcopyprocessor.kafka.SignDigitalDocumentFactory;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;
import uk.gov.companieshouse.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static java.lang.System.getenv;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static software.amazon.awssdk.core.SdkSystemSetting.AWS_ACCESS_KEY_ID;
import static software.amazon.awssdk.core.SdkSystemSetting.AWS_SECRET_ACCESS_KEY;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.DOCUMENT_METADATA;
import static uk.gov.companieshouse.digitalcertifiedcopyprocessor.util.Constants.EXPECTED_PRIVATE_DOCUMENT_URI;
import static wiremock.org.apache.commons.io.FileUtils.copyInputStreamToFile;

/**
 *  "Test" class re-purposed to execute {@link DocumentService#getPrivateUri(String)}
 *  to get a private URl for a document in Cidev, and then verify that the URI can
 *  be used to download the document. This is NOT to be run as part of an automated
 *  test suite. It is for manual testing only.
 */
@SpringBootTest
@SpringJUnitConfig(classes={
        GetDocumentApiPdfFromCidev.Config.class,
        TestConfig.class,
        KafkaConfig.class,
        SignDigitalDocumentFactory.class})
@Tag("manual")
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class GetDocumentApiPdfFromCidev {

    private static final String DOWNLOADED_DOCUMENT_PATH = "./test/downloaded.pdf";


    private static final String AWS_REGION_VALUE = "eu-west-2";

    @Autowired
    private DocumentService serviceUnderTest;

    @Autowired
    private Logger logger;

    @Autowired
    private S3Client s3Client;

    @MockBean
    private KafkaConsumer<String, ItemOrderedCertifiedCopy> testConsumer;
    @MockBean
    private KafkaProducer<String, ItemOrderedCertifiedCopy> testProducer;
    @MockBean
    private ProducerFactory<String, ItemOrderedCertifiedCopy> producerFactory;
    @MockBean
    private ConsumerFactory<String, ItemOrderedCertifiedCopy> consumerFactory;

    @Configuration
    @ComponentScan(basePackageClasses = GetDocumentApiPdfFromCidev.class)
    static class Config {

        @Bean
        public S3Client s3Client() {
            final var stsClient = StsClient.builder()
                    .region(Region.of(AWS_REGION_VALUE))
                    .build();
            final var stsGetSessionTokenCredentialsProvider =
                    StsGetSessionTokenCredentialsProvider.builder().
                            stsClient(stsClient)
                            .build();
            return S3Client.builder().
                    region(Region.of(AWS_REGION_VALUE)).
                    credentialsProvider(stsGetSessionTokenCredentialsProvider)
                    .build();
        }

    }

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    /**
     * Required environment variables:
     * <ul>
     *     <li><code>CHS_API_KEY</code></li>
     *     <li><code>AWS_ACCESS_KEY_ID</code></li>
     *     <li><code>AWS_SECRET_ACCESS_KEY</code></li>
     * </ul>
     */
    @Test
    @DisplayName("get document PDF from cidev")
    void getDocumentPdfFromCidev() throws IOException {

        // Given
        givenSdkIsConfiguredForTilt(environmentVariables);
        givenS3BucketAccessIsConfigured();

        // When
        final URI privateUri =
                serviceUnderTest.getPrivateUri(DOCUMENT_METADATA);

        copyInputStreamToFile(getInputStream(privateUri), new File(DOWNLOADED_DOCUMENT_PATH));

        logger.info("Document PDF downloaded to " + DOWNLOADED_DOCUMENT_PATH);
    }

    /**
     * Required environment variables:
     * <ul>
     *     <li><code>CHS_API_KEY<</code></li>
     * </ul>
     */
    @Test
    @DisplayName("get private URI from cidev")
    void getPrivateUriFromCidev() {

        // Given
        givenSdkIsConfiguredForTilt(environmentVariables);

        // When
        final URI privateUri =
                serviceUnderTest.getPrivateUri(DOCUMENT_METADATA);

        logger.info("Get private URI returned = " + privateUri);

        // Then
        assertThat(privateUri, is(EXPECTED_PRIVATE_DOCUMENT_URI));
    }

    /**
     * Required environment variables:
     * <ul>
     *     <li><code>CHS_API_KEY</code></li>
     * </ul>
     */
    @Test
    @DisplayName("get public URI from cidev")
    void getPublicUriFromCidev() {

        // Given
        givenSdkIsConfiguredForTilt(environmentVariables);

        // When
        final URI publicUri =
                serviceUnderTest.getPublicUri(DOCUMENT_METADATA);

        logger.info("Get public URI returned = " + publicUri);

        // Then
        assertThat(publicUri.getPath(), is(EXPECTED_PRIVATE_DOCUMENT_URI.getPath()));
    }


    private static void givenS3BucketAccessIsConfigured() {
        assertThat(getenv(AWS_ACCESS_KEY_ID.environmentVariable()), not(is(emptyOrNullString())));
        assertThat(getenv(AWS_SECRET_ACCESS_KEY.environmentVariable()), not(is(emptyOrNullString())));
    }

    private ResponseInputStream<GetObjectResponse> getInputStream(final URI privateUri) {
        final var bucketName =  privateUri.getHost();
        final var key = privateUri.getPath().substring(1); // remove leading /
        final var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    /**
     * Configures the CH Java SDKs used in manual tests to interact with Tilt
     * or other development hosted API endpoints.
     * @param variables {@link EnvironmentVariables} class rule permitting environment variable manipulation
     */
    public static void givenSdkIsConfiguredForTilt(final EnvironmentVariables variables) {
        assertThat(getenv("CHS_API_KEY"), not(is(emptyOrNullString())));
        variables.set("API_URL", "http://api.chs.local:4001");
        variables.set("PAYMENTS_API_URL", "http://api.chs.local:4001");
        variables.set("DOCUMENT_API_LOCAL_URL", "http://document-api-cidev.aws.chdev.org");
    }

}


