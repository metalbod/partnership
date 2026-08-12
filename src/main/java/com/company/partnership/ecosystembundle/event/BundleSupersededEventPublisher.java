package com.company.partnership.ecosystembundle.event;

import com.company.partnership.ecosystembundle.config.BundleSupersededEventProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

/**
 * Publishes BundleSuperseded to EventBridge (see /api-contracts). Replaces the manual
 * POST /v1/subscriptions/flag-pending-reconsent call that partner-subscription-service
 * previously had to be driven by directly.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BundleSupersededEventPublisher {

    private final EventBridgeClient eventBridgeClient;
    private final BundleSupersededEventProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Publishes only after the enclosing transaction commits – if bundle creation rolls
     * back, no event should go out. Falls back to immediate publish if no transaction is
     * active (e.g. called from a test or a non-transactional context).
     */
    public void publishAfterCommit(BundleSupersededEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(event);
                }
            });
        } else {
            publish(event);
        }
    }

    private void publish(BundleSupersededEvent event) {
        try {
            String detail = objectMapper.writeValueAsString(event);
            PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                    .eventBusName(properties.eventBusName())
                    .source(properties.source())
                    .detailType(properties.detailType())
                    .detail(detail)
                    .build();

            PutEventsResponse response = eventBridgeClient.putEvents(
                    PutEventsRequest.builder().entries(entry).build());

            if (response.failedEntryCount() != null && response.failedEntryCount() > 0) {
                for (PutEventsResultEntry result : response.entries()) {
                    if (result.errorCode() != null) {
                        log.error("Failed to publish BundleSuperseded event {}: {} - {}",
                                event.eventId(), result.errorCode(), result.errorMessage());
                    }
                }
            } else {
                log.info("Published BundleSuperseded event {} (oldBundleId={}, newBundleId={})",
                        event.eventId(), event.oldBundleId(), event.newBundleId());
            }
        } catch (Exception e) {
            // Non-fatal by design: the manual POST /v1/subscriptions/flag-pending-reconsent
            // endpoint remains available as a fallback if the event bus is unreachable.
            log.error("Failed to publish BundleSuperseded event {} for bundle {}",
                    event.eventId(), event.oldBundleId(), e);
        }
    }
}
