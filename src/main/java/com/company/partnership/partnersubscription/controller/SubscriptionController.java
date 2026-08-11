package com.company.partnership.partnersubscription.controller;

import com.company.partnership.partnersubscription.dto.SubscribeRequest;
import com.company.partnership.partnersubscription.dto.SubscriptionResponse;
import com.company.partnership.partnersubscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse subscribe(@Valid @RequestBody SubscribeRequest req) {
        return SubscriptionResponse.from(subscriptionService.subscribe(req));
    }

    @GetMapping("/{id}")
    public SubscriptionResponse get(@PathVariable UUID id) {
        return SubscriptionResponse.from(subscriptionService.findById(id));
    }

    @GetMapping
    public List<SubscriptionResponse> listByPartner(@RequestParam UUID partnerId) {
        return subscriptionService.findByPartner(partnerId).stream().map(SubscriptionResponse::from).toList();
    }

    /** Internal endpoint, normally driven by the BundleSuperseded integration event (see /api-contracts). */
    @PostMapping("/flag-pending-reconsent")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void flagPendingReconsent(@RequestBody Map<String, UUID> body) {
        subscriptionService.flagPendingReconsent(body.get("oldBundleId"), body.get("newBundleId"));
    }

    @PostMapping("/{id}/reconsent")
    public SubscriptionResponse reconsent(@PathVariable UUID id, @RequestParam int newBundleVersion) {
        return SubscriptionResponse.from(subscriptionService.reconsent(id, newBundleVersion));
    }
}
