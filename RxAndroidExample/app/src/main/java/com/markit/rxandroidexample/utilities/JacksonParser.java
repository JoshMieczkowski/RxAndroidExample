package com.markit.rxandroidexample.utilities;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by josh.mieczkowski on 8/26/2015.
 */
public class JacksonParser {
    private static JacksonParser ourInstance = new JacksonParser();
    private ObjectMapper objectMapper;

    public static JacksonParser getInstance() {
        return ourInstance;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private JacksonParser() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
