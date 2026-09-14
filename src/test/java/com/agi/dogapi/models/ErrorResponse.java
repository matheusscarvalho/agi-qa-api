package com.agi.dogapi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public class ErrorResponse {
    public String message;
    public String status;
    public Integer code;
}
