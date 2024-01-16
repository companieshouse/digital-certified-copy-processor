package uk.gov.companieshouse.digitalcertifiedcopyprocessor.util;

import uk.gov.companieshouse.documentsigning.CoverSheetDataRecord;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final String DOCUMENT_METADATA = "/document/-fsWaC-ED30jRNACt2dqNYc-lH2uODjjLhliYjryjV0";
    public static final String DOCUMENT_METADATA_WITH_HOSTNAME =
            "http://document-api-cidev.aws.chdev.org/document/-fsWaC-ED30jRNACt2dqNYc-lH2uODjjLhliYjryjV0";

    public static final String PUBLIC_DOCUMENT_URI =
        "https://document-api-images-cidev.s3.eu-west-2.amazonaws.com/" +
        "docs/-fsWaC-ED30jRNACt2dqNYc-lH2uODjjLhliYjryjV0/application-pdf" +
        "?X-Amz-Algorithm=AWS4-HMAC-SHA256" +
        "&X-Amz-Credential=AWS_Access_Key_ID%2F20230523%2Feu-west-2%2Fs3%2Faws4_request" +
        "&X-Amz-Date=20230523T071124Z" +
        "&X-Amz-Expires=60&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEKf%2F%2F%2F%2F%2F%2F%2F%2F%2F%2" +
        "FwEaCWV1LXdlc3QtMiJIMEYCIQCkH0CgdgxtZUHzWSGqGZfvhArMBVhjUipfyzC7HqxybwIhAO3%2FACQh8hYR" +
        "P9tBbyeKNThXR5x4t%2B4kWBWFM9o4pYhjKsUFCND%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEQABoMMTY5OTQy" +
        "MDIwNTIxIgy%2FRjW4UMixpIgXLyoqmQUlJi%2FBNQsLmcvzHsmKLGAkTSa%2B07rB3JADObNlS1F8ajKNef%2" +
        "BHlGPmGtWr4TDVo%2BmfASgChgdwaZfZQ5%2BedFmO1O5mZIaaTXKpnpnggPEVv8EzFF4%2BuASmFhhD5GVEXi" +
        "MHdMqXaYzeKpry2wjJFvvcUhLpwOq9MdGEOd9rJOWp3EwX35u6geuFlR36yHPqXZD8gEmOltWJXz64G8otbmxo" +
        "imy9ZMjUe%2FT8KbAEg7WUfpHo23022myZjeUJYfk2X32mwQ%2F8FsUoU2pMQpTUHO0QmBlscMKzqlJMHalBzE" +
        "ut%2Fuiw0jl%2BbbXmRcz6piW4DTHuK%2BQsRUNGAjItwaFxVjNemJ14m1yDSNEaRE1JdqWutAsdKQ87XgV%2F" +
        "s9Td8KeFLJH%2FaC86ZXsgEjB2esnjp0q56ATBCs7yp9Im1966%2BoBOZaxqNAbtLwr%2F11YKn5V01R9uQpPi" +
        "ieEKp7nipRbxajBB6bJcTen%2By6dvxGm%2FladwvUIH7BRWYPDCx1DA9s1bUEKpLg%2F2FSVEmhq6yjZV%2Bu" +
        "eNkOBwqKwcLYKWgB3nfVYEu1ORzx6IMbVksnMJ%2BAlHrfj9HJF3XYDxca8yw%2F%2BIvi4oSK3j2Bc2bRyHim" +
        "q9UCdkiQFFnyPzplZ8OVkZ0%2FfOnv0LkPYhkdiMUoPWIqPcOyjmSNjF%2FbqOs2O7cN8xUUy4Nv%2FhzUYaYZ" +
        "svV6cy3Gk7DEq5Uj8eT5fEd3zlwLg0YKaG%2BEs2pHdoEIrDx6SgRDmZF5%2FlKwIEG8HrvkFLBvXxEjSnr%2F" +
        "u0naENzYXIg9fgtc%2BtctfbNtD1jloLe0RelNMGWhP9huPN1kotv3AnJQ%2B4TCxhIbDQeJBox%2BpR7sSfso" +
        "UfCAAgZr8ZzBlkhqAxYbAOCz3jhCPlLRdaGzD8v7GjBjqwAev5qyBqHCbUewXjxQs0JEOegwMO4gQ7IJt7OU0m" +
        "l1%2F3gogILJ2hpX6SvPCIT5ECm0NR9gaO2Ej%2B1RSflO%2FwBMpo2p48rwUVnUZ2LwNnvGxf79BwbyMJ%2Bd" +
        "cgaHQks4z5suSYJ4fabfQnYV32CwzEc%2FdNxaPKM7TFzaqaU%2FiVB91yVncr9sMp%2B8zKVMSyFBB32bQPYL" +
        "7qZT33foMHOrQP3BCQjovrlGe67hOKURrh5lkf" +
        "&X-Amz-SignedHeaders=host" +
        "&response-content-disposition=inline%3Bfilename%3D%22companies_house_document.pdf%22" +
        "&X-Amz-Signature=e18b2345566e0340bfc6ca7633939039df9fb71c1a44265701bb7102e42094c0";

    public static final URI EXPECTED_PUBLIC_DOCUMENT_URI;
    public static final URI EXPECTED_PRIVATE_DOCUMENT_URI;
    public static final URI PRIVATE_DOCUMENT_URI;
    static {
        try {
            EXPECTED_PUBLIC_DOCUMENT_URI = new URI(PUBLIC_DOCUMENT_URI);
            EXPECTED_PRIVATE_DOCUMENT_URI = new URI(
                    "s3://document-api-images-cidev/docs/-fsWaC-ED30jRNACt2dqNYc-lH2uODjjLhliYjryjV0/application-pdf");
            PRIVATE_DOCUMENT_URI = EXPECTED_PRIVATE_DOCUMENT_URI;
        } catch (URISyntaxException e) {
            // This will not happen.
            throw new RuntimeException(e);
        }
    }

    private static final CoverSheetDataRecord COVER_SHEET_DATA = CoverSheetDataRecord.newBuilder()
            .setCompanyName("Test Company")
            .setCompanyNumber("00000000")
            .setDescription("A test filing history document")
            .setType("AM01")
            .build();

    public static final SignDigitalDocument DOCUMENT = SignDigitalDocument.newBuilder()
            .setOrderNumber("ORD-152416-079544")
            .setPrivateS3Location("s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf")
            .setDocumentType("363s")
            .setGroupItem("/item-groups/IG-000216-873460/items/")
            .setItemId("CCD-768116-517930")
            .setCoverSheetData(COVER_SHEET_DATA)
            .setFilingHistoryDescriptionValues(new HashMap<>())
            .build();

    public static final ItemOrderedCertifiedCopy CERTIFIED_COPY = ItemOrderedCertifiedCopy.newBuilder()
            .setOrderNumber("ORD-152416-079544")
            .setItemId("CCD-768116-517930")
            .setGroupItem("/item-groups/IG-000216-873460/items/CCD-768116-517930")
            .setCompanyName("Test Company Limited")
            .setCompanyNumber("00006400")
            .setFilingHistoryDescription("appoint-person-director-company-with-name-date")
            .setFilingHistoryId("OTYyMTM3NjgxOGFkaXF6a2N4")
            .setFilingHistoryType("AP01")
            .setFilingHistoryDescriptionValues(Map.of(
                    "appointment_date", "2023-05-01",
                    "officer_name", "Mr Tom Sunburn"))
            .build();

    public static final ItemOrderedCertifiedCopy CERTIFIED_COPY_2 = ItemOrderedCertifiedCopy.newBuilder()
            .setOrderNumber("ORD-152416-079544")
            .setItemId("CCD-768116-517930")
            .setGroupItem("/item-groups/IG-000216-873460/items/CCD-768116-517930")
            .setCompanyName("Test Company Limited")
            .setCompanyNumber("00006400")
            .setFilingHistoryDescription("appoint-person-director-company-with-name-date")
            .setFilingHistoryId("MDA3MjAxMzc0MmFkaXF6a2N4")
            .setFilingHistoryType("AP01")
            .setFilingHistoryDescriptionValues(Map.of(
                    "appointment_date", "2023-05-01",
                    "officer_name", "Mr Tom Sunburn"))
            .build();


}
