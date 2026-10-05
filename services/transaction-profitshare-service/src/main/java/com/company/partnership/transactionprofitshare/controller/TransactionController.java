package com.company.partnership.transactionprofitshare.controller;

import com.company.partnership.transactionprofitshare.dto.TransactionRequest;
import com.company.partnership.transactionprofitshare.dto.TransactionResponse;
import com.company.partnership.transactionprofitshare.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse record(@Valid @RequestBody TransactionRequest req) {
        return TransactionResponse.from(transactionService.record(req));
    }

    @GetMapping
    public List<TransactionResponse> list() {
        return transactionService.findAll().stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TransactionResponse get(@PathVariable UUID id) {
        return TransactionResponse.from(transactionService.findById(id));
    }
}
