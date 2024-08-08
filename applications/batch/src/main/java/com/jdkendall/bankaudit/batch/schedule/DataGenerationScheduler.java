package com.jdkendall.bankaudit.batch.schedule;

import com.jdkendall.bankaudit.batch.services.mft.FileDataStore;
import com.jdkendall.bankaudit.domain.Account;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class DataGenerationScheduler {

    private final FileDataStore fileDataStore;
    private final DataSource accountDataSource;

    DataGenerationScheduler(FileDataStore fileDataStore, DataSource accountDataSource) {
        this.fileDataStore = fileDataStore;
        this.accountDataSource = accountDataSource;
    }

    record Data(UUID uuid, Account source, Account destination, long total, LocalDateTime timestamp) {
        public String toDelimited() {
            return "%s|%s|%s|%s|%s|%d|%s".formatted(uuid, source.id(), source.routingNumber(), destination.id(), destination.routingNumber(), total, timestamp);
        }
    }

    @WithSpan
    @Scheduled(fixedRateString = "${generator.rate:300000}")
    void generateData() {
        // Generate a random number of Data records (between 5 and 500)
        int numRecords = (int) (Math.random() * 495) + 5;

        List<Account> accounts = new ArrayList<>();
        try (Connection conn = accountDataSource.getConnection();
             var stmt = conn.prepareStatement("select account_num, routing_num from accounts");
             var result = stmt.executeQuery()) {
            while(result.next()) {
                accounts.add(new Account(result.getString(1), result.getString(2)));
            }
        } catch (SQLException ex) {
            LOG.error("Failed to connect to account database", ex);
            throw new RuntimeException(ex);
        }

        // Generate a random time within the last 3 days by subtracting a random number of seconds
        List<Data> records = IntStream.range(0, numRecords)
                .mapToObj(i ->
                        new Data(UUID.randomUUID(), pickRandom(accounts), pickRandom(accounts), generateRandomTotal(1000), generateRandomTime(Duration.ofDays(3)))
                )
                .sorted(Comparator.comparing(Data::timestamp))
                .toList();

        // Assign a unique filename based on current timestamp
        String filename = ("dogebank-inbound-%s.batch".formatted(LocalDateTime.now())).replace(":", ".");
        String body = records.stream().map(Data::toDelimited).collect(Collectors.joining("\n"));
        LOG.info("Writing {} records to {}", numRecords, filename);
        fileDataStore.save(filename, body);
    }

    private static long generateRandomTotal(int max) {
        return (long) (Math.random() * max);
    }

    private static LocalDateTime generateRandomTime(Duration maxPastOffset) {
        long randomOffset = (long) (Math.random() * maxPastOffset.toMillis());
        return LocalDateTime.now().minus(randomOffset, ChronoUnit.MILLIS);
    }

    private static <T> T pickRandom(List<T> list) {
        return list.get((int) (Math.random() * list.size()));
    }

    private static final Logger LOG = LoggerFactory.getLogger(DataGenerationScheduler.class);
}
