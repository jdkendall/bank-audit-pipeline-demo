package com.yourcompany.batch;

import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.inject.Named;
import java.util.List;

@Named
public class MyItemWriter extends AbstractItemWriter {

    @Override
    public void writeItems(List<Object> items) {
        for (Object item : items) {
            System.out.println("Writing item: " + item); // Write the item, e.g., to the console
        }
    }
}

