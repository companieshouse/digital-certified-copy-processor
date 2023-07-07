package uk.gov.companieshouse.digitalcertifiedcopyprocessor.consumer;

import uk.gov.companieshouse.itemorderedcertifiedcopy.ItemOrderedCertifiedCopy;

import java.util.Map;

public class ConsumerTestConstants {
    private ConsumerTestConstants() {}

    public static final ItemOrderedCertifiedCopy ITEM_ORDERED_CERTIFIED_COPY = ItemOrderedCertifiedCopy.newBuilder()
            .setOrderNumber("ORD-422426-522214")
            .setItemId("CCD-182317-112340")
            .setGroupItem("/item-groups/IG-004747-123485/items/CCD-341238-223457")
            .setCompanyName("Very Profitable Company Limited")
            .setCompanyNumber("00006400")
            .setFilingHistoryDescription("appoint-person-director-company-with-name-date")
            .setFilingHistoryId("OTKyMYM3EgbyOPTweXFr41C4")
            .setFilingHistoryType("AP01")
            .setFilingHistoryDescriptionValues(Map.of(
                    "appointment_date", "2022-01-01",
                    "officer_name", "Mr Clean"))
            .build();
}
