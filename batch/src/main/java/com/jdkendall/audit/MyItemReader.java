package com.yourcompany.batch;

import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Named
public class MyItemReader extends AbstractItemReader {

    private List<String> items = Arrays.asList("Item1", "Item2", "Item3");
    private int index = 0;

    @Override
    public String readItem() {
        if (index < items.size()) {
            return items.get(index++);
        }
        return null; // Return null to indicate no more items to read
    }
}

