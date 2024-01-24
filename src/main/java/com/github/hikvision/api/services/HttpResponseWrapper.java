package com.github.hikvision.api.services;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpResponseWrapper {
    private final String responseBody;
    private final int responseCode;
}
