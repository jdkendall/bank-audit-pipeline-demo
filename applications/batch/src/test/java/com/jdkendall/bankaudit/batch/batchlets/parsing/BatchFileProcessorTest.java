package com.jdkendall.bankaudit.batch.batchlets.parsing;

import com.jdkendall.bankaudit.batch.domain.AuditLine;
import com.jdkendall.bankaudit.batch.domain.ParsedLine;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class BatchFileProcessorTest {
    @Test
    void test() throws Exception {
        // Given
        ParsedLine input = new ParsedLine("b53f22a3-40d1-4bb9-9678-f258b20b4193","2024-02-22T03:16:21Z", "2912");

        // When
        BatchFileProcessor batchFileProcessor = new BatchFileProcessor();
        AuditLine result = (AuditLine) batchFileProcessor.processItem(input);

        // Then
        assertNotNull(result);
        assertEquals("b53f22a3-40d1-4bb9-9678-f258b20b4193", result.uuid().toString());
        assertEquals(2912, result.total());
        assertEquals("2024-02-22T03:16:21", result.timestamp().format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
