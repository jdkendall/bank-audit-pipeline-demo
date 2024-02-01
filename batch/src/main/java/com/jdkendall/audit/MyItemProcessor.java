package com.yourcompany.batch;

import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.inject.Named;

@Named
public class MyItemProcessor implements ItemProcessor {

    @Override
    public Object processItem(Object item) {
        String input = (String) item;
        return input.toUpperCase(); // Process the item, e.g., convert to uppercase
    }
}

