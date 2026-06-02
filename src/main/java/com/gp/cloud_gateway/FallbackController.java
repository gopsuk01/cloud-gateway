package com.gp.cloud_gateway;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/fallback")
@Slf4j
@RequiredArgsConstructor
public class FallbackController {

    // ✅ Inject Tracer — reads traceId from current reactive context
    private final Tracer tracer;

    @RequestMapping("/order")
    public ResponseEntity<FallbackResponse> orderFallback(ServerWebExchange exchange) {

        // ✅ Read traceId from Tracer bean — works on forwarded requests
        String traceId = tracer.currentSpan() != null
                ? tracer.currentSpan().context().traceId()
                : "unknown";

        // ✅ Read cause from gateway exchange attribute
        Throwable cause = exchange.getAttribute(
                ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);

        String causeMessage = cause != null ? cause.getMessage() : "Service unreachable";

        log.error("⚠️ [cloud-gateway] ORDER-SERVICE is DOWN!");
        log.error("⚠️ TraceId: {} | Cause: {}", traceId, causeMessage);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(FallbackResponse.builder()
                        .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .service("ORDER-SERVICE")
                        .message("Order service is currently down. Please try again later.")
                        .traceId(traceId)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }

    @RequestMapping("/payment")
    public ResponseEntity<FallbackResponse> paymentFallback(ServerWebExchange exchange) {

        // ✅ Read traceId from Tracer bean
        String traceId = tracer.currentSpan() != null
                ? tracer.currentSpan().context().traceId()
                : "unknown";

        Throwable cause = exchange.getAttribute(
                ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);

        String causeMessage = cause != null ? cause.getMessage() : "Service unreachable";

        log.error("⚠️ [cloud-gateway] PAYMENT-SERVICE is DOWN!");
        log.error("⚠️ TraceId: {} | Cause: {}", traceId, causeMessage);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(FallbackResponse.builder()
                        .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .service("PAYMENT-SERVICE")
                        .message("Payment service is currently down. Please try again later.")
                        .traceId(traceId)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }
}