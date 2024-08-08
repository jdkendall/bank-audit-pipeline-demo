package com.jdkendall.bankaudit.batch.services.parsing;

import com.jdkendall.bankaudit.batch.domain.ParsedLine;
import com.jdkendall.bankaudit.domain.AuditRequest;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class BatchFileProcessorTest {
    @Test
    void test() {
        // Given
        ParsedLine input = new ParsedLine(
                "b53f22a3-40d1-4bb9-9678-f258b20b4193",
                "1234567890",
                "7788778877",
                "5858585858",
                "3323313131",
                "2912",
                "2024-02-22T03:16:21Z");

        // When
        BatchFileProcessor batchFileProcessor = new BatchFileProcessor();
        AuditRequest result = batchFileProcessor.process(input);

        // Then
        assertNotNull(result);
        assertEquals("b53f22a3-40d1-4bb9-9678-f258b20b4193", result.uuid().toString());
        assertEquals(2912, result.total());
        assertEquals("2024-02-22T03:16:21", result.timestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        assertEquals("1234567890", result.sourceAccount().id());
        assertEquals("7788778877", result.sourceAccount().routingNumber());
        assertEquals("5858585858", result.targetAccount().id());
        assertEquals("3323313131", result.targetAccount().routingNumber());
    }
}
