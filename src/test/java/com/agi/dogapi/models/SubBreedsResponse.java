package com.agi.dogapi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = false)
public class SubBreedsResponse {
    public List<String> message;
    public String status;
}
