package com.github.hikvision.api.services;

import lombok.Data;

@Data
public class HikVisionErrorResponse {
    public int statusCode;
    public String statusString;
    public String subStatusCode;
    public int errorCode;
    public String errorMsg;
}
