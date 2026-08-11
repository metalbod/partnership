package com.company.partnership.ecosystembundle.controller;

import com.company.partnership.ecosystembundle.dto.BundleRequest;
import com.company.partnership.ecosystembundle.dto.BundleResponse;
import com.company.partnership.ecosystembundle.service.BundleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/bundles")
@RequiredArgsConstructor
public class BundleController {

    private final BundleService bundleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BundleResponse create(@Valid @RequestBody BundleRequest req) {
        return BundleResponse.from(bundleService.create(req));
    }

    @PostMapping("/{id}/publish")
    public BundleResponse publish(@PathVariable UUID id) {
        return BundleResponse.from(bundleService.publish(id));
    }

    /** FR-BUN-03/04: creates a new version instead of mutating a published bundle. */
    @PostMapping("/{id}/new-version")
    public BundleResponse newVersion(@PathVariable UUID id, @Valid @RequestBody BundleRequest req) {
        return BundleResponse.from(bundleService.createNewVersion(id, req));
    }

    @GetMapping("/{id}")
    public BundleResponse get(@PathVariable UUID id) {
        return BundleResponse.from(bundleService.findById(id));
    }

    @GetMapping
    public List<BundleResponse> listByEcoSystem(@RequestParam UUID ecoSystemId) {
        return bundleService.findByEcoSystem(ecoSystemId).stream().map(BundleResponse::from).toList();
    }
}
