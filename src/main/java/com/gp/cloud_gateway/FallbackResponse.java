package com.gp.cloud_gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FallbackResponse {
    private int status;
    private String service;
    private String message;
    private String traceId;
    private String timestamp;
}
