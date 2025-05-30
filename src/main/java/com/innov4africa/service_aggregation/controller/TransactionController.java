package com.innov4africa.service_aggregation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.innov4africa.service_aggregation.model.ServiceStatus;
import com.innov4africa.service_aggregation.model.Transaction;
import com.innov4africa.service_aggregation.model.TransactionResponse;
import com.innov4africa.service_aggregation.service.JwtUtil;
import com.innov4africa.service_aggregation.service.TransactionAggregationService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionAggregationService transactionService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/recent")
    public Mono<ResponseEntity<TransactionResponse>> getRecentTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        return validateAndExtractToken(authHeader)
            .flatMap(tokenInfo -> {
                String telephone = tokenInfo.get("telephone");
                String ipayToken = tokenInfo.get("ipayToken"); 
                String email = tokenInfo.get("email");

                return transactionService.getRecentTransactions(telephone, ipayToken, email)
                    .map(transactions -> ResponseEntity.ok(
                        new TransactionResponse(
                            "success",
                            "Recent transactions retrieved",
                            transactions.size(),
                            transactions,
                            List.of(new ServiceStatus("i-pay", true, "Success"))
                        )
                    ))
                    .onErrorResume(e -> Mono.just(
                        ResponseEntity.internalServerError()
                            .body(new TransactionResponse(
                                "error",
                                e.getMessage(),
                                0,
                                null,
                                List.of(new ServiceStatus("i-pay", false, "Error getting transactions"))
                            ))
                    ));
            });
    }

    @GetMapping("/list")
    public Mono<ResponseEntity<TransactionResponse>> getAllTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String filter) {

        return validateAndExtractToken(authHeader)
            .flatMap(tokenInfo -> {
                String telephone = tokenInfo.get("telephone");
                String ipayToken = tokenInfo.get("ipayToken");
                String email = tokenInfo.get("email");

                return transactionService.getAllTransactions(telephone, ipayToken, email, filter)
                    .map(transactions -> ResponseEntity.ok(
                        new TransactionResponse(
                            "success",
                            "Transactions retrieved",
                            transactions.size(),
                            transactions,
                            List.of(new ServiceStatus("i-pay", true, "Success"))
                        )
                    ))
                    .onErrorResume(e -> Mono.just(
                        ResponseEntity.internalServerError()
                            .body(new TransactionResponse(
                                "error",
                                e.getMessage(),
                                0,
                                null,
                                List.of(new ServiceStatus("i-pay", false, "Error getting transactions"))
                            ))
                    ));
            });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TransactionResponse>> getTransactionDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestParam String source) {

        return validateAndExtractToken(authHeader)
            .flatMap(tokenInfo -> 
                transactionService.getTransactionDetails(id, source)
                    .map(transaction -> ResponseEntity.ok(
                        new TransactionResponse(
                            "success",
                            "Transaction details retrieved",
                            1,
                            List.of(transaction),
                            List.of(new ServiceStatus(source, true, "Success"))
                        )
                    ))
                    .defaultIfEmpty(ResponseEntity.notFound().build())
                    .onErrorResume(e -> Mono.just(
                        ResponseEntity.internalServerError()
                            .body(new TransactionResponse(
                                "error",
                                e.getMessage(),
                                0,
                                null,
                                List.of(new ServiceStatus(source, false, "Error getting transaction details"))
                            ))
                    ))
            );
    }

    private Mono<java.util.Map<String, String>> validateAndExtractToken(String authHeader) {
        // 1. Verify Authorization header presence
        if (authHeader == null || authHeader.isBlank()) {
            return Mono.error(new IllegalArgumentException("Missing Authorization header"));
        }

        // 2. Check Bearer format
        if (!authHeader.startsWith("Bearer ")) {
            return Mono.error(new IllegalArgumentException("Invalid token format"));
        }

        String jwt = authHeader.substring(7);

        // 3. Validate JWT token
        if (!jwtUtil.validateToken(jwt)) {
            return Mono.error(new IllegalArgumentException("Invalid or expired token"));
        }

        // 4. Extract required claims
        String telephone = jwtUtil.extractTelephone(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        String email = jwtUtil.extractEmail(jwt);

        if (telephone == null || ipayToken == null) {
            return Mono.error(new IllegalArgumentException("Incomplete token claims"));
        }

        var tokenInfo = new java.util.HashMap<String, String>();
        tokenInfo.put("telephone", telephone);
        tokenInfo.put("ipayToken", ipayToken);
        tokenInfo.put("email", email);

        return Mono.just(tokenInfo);
    }
}
