package com.company.partnership.partnersubscription.controller;

import com.company.partnership.partnersubscription.dto.PartnerRequest;
import com.company.partnership.partnersubscription.dto.PartnerResponse;
import com.company.partnership.partnersubscription.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerResponse create(@Valid @RequestBody PartnerRequest req) {
        return PartnerResponse.from(partnerService.create(req));
    }

    @GetMapping
    public List<PartnerResponse> list() {
        return partnerService.findAll().stream().map(PartnerResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PartnerResponse get(@PathVariable UUID id) {
        return PartnerResponse.from(partnerService.findById(id));
    }
}
