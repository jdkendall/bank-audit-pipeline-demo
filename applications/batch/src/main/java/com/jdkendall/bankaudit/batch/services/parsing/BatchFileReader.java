package com.jdkendall.bankaudit.batch.services.parsing;

import com.jdkendall.bankaudit.batch.domain.ParsedLine;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

public class BatchFileReader extends FlatFileItemReader<ParsedLine> {
    public BatchFileReader() {
        super();
        DefaultLineMapper<ParsedLine> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(new DelimitedLineTokenizer("|"));
        lineMapper.setFieldSetMapper(fs -> new ParsedLine(
                fs.readString(0),
                fs.readString(1),
                fs.readString(2),
                fs.readString(3),
                fs.readString(4),
                fs.readString(5),
                fs.readString(6)
        ));
        setLineMapper(lineMapper);
    }

    @Override
    protected ParsedLine doRead() throws Exception {
        return super.doRead();
    }

    @Override
    public ParsedLine read() throws Exception {
        return super.read();
    }
}
