package com.company.partnership.ecosystembundle.controller;

import com.company.partnership.ecosystembundle.dto.EcoSystemRequest;
import com.company.partnership.ecosystembundle.dto.EcoSystemResponse;
import com.company.partnership.ecosystembundle.service.EcoSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/eco-systems")
@RequiredArgsConstructor
public class EcoSystemController {

    private final EcoSystemService ecoSystemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EcoSystemResponse create(@Valid @RequestBody EcoSystemRequest req) {
        return EcoSystemResponse.from(ecoSystemService.create(req));
    }

    @GetMapping
    public List<EcoSystemResponse> list() {
        return ecoSystemService.findAll().stream().map(EcoSystemResponse::from).toList();
    }

    @GetMapping("/{id}")
    public EcoSystemResponse get(@PathVariable UUID id) {
        return EcoSystemResponse.from(ecoSystemService.findById(id));
    }

    @PostMapping("/{id}/offerings/{offeringId}")
    public EcoSystemResponse assignOffering(@PathVariable UUID id, @PathVariable UUID offeringId) {
        return EcoSystemResponse.from(ecoSystemService.assignOffering(id, offeringId));
    }
}
