package com.jdkendall.bankaudit.batch.services;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;

public class MarkCompleteTasklet implements Tasklet {
    private final Resource file;

    public MarkCompleteTasklet(Resource file) {
        this.file = file;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Path path = file.getFile().toPath();
        Files.move(path, path.resolveSibling(file.getFilename() + ".complete"));
        return RepeatStatus.FINISHED;
    }
}
