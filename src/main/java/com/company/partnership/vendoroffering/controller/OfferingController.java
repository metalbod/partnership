package com.company.partnership.vendoroffering.controller;

import com.company.partnership.vendoroffering.dto.OfferingRequest;
import com.company.partnership.vendoroffering.dto.OfferingResponse;
import com.company.partnership.vendoroffering.service.OfferingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/offerings")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferingResponse create(@Valid @RequestBody OfferingRequest req) {
        return OfferingResponse.from(offeringService.create(req));
    }

    @GetMapping
    public List<OfferingResponse> listByVendor(@RequestParam UUID vendorId) {
        return offeringService.findByVendor(vendorId).stream().map(OfferingResponse::from).toList();
    }

    @GetMapping("/{id}")
    public OfferingResponse get(@PathVariable UUID id) {
        return OfferingResponse.from(offeringService.findById(id));
    }
}
