package com.company.partnership.vendoroffering.controller;

import com.company.partnership.vendoroffering.domain.Vendor;
import com.company.partnership.vendoroffering.dto.VendorRequest;
import com.company.partnership.vendoroffering.dto.VendorResponse;
import com.company.partnership.vendoroffering.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorResponse create(@Valid @RequestBody VendorRequest req) {
        return VendorResponse.from(vendorService.create(req));
    }

    @GetMapping
    public List<VendorResponse> list() {
        return vendorService.findAll().stream().map(VendorResponse::from).toList();
    }

    @GetMapping("/{id}")
    public VendorResponse get(@PathVariable UUID id) {
        return VendorResponse.from(vendorService.findById(id));
    }

    @PutMapping("/{id}")
    public VendorResponse update(@PathVariable UUID id, @Valid @RequestBody VendorRequest req) {
        return VendorResponse.from(vendorService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        vendorService.deactivate(id);
    }
}
