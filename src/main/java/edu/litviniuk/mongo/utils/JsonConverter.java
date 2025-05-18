package edu.litviniuk.mongo.utils;

/*
  @author darin
  @project mongo
  @class JsonConverter
  @version 1.0.0
  @since 18.05.2025 - 14.01
*/

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter {
    public static String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }
}
