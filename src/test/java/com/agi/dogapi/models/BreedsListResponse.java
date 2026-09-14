package com.agi.dogapi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = false)
public class BreedsListResponse {
    public Map<String, List<String>> message;
    public String status;
}
