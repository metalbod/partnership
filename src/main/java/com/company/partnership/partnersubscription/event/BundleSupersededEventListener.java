package com.company.partnership.partnersubscription.event;

import com.company.partnership.partnersubscription.service.SubscriptionService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Consumes BundleSuperseded off the SQS queue an EventBridge rule routes it to (see
 * infra/local for the local rule/queue bootstrap). This is what
 * POST /v1/subscriptions/flag-pending-reconsent used to require a synchronous caller
 * for – that endpoint now only exists as a manual/testing fallback.
 *
 * Disabled under "test": the Spring context test runs against H2 with no SQS/LocalStack
 * available, and registering an @SqsListener bean would make the listener container try
 * (and fail) to resolve the queue on startup.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class BundleSupersededEventListener {

    private final SubscriptionService subscriptionService;

    @SqsListener("${app.events.bundle-superseded.queue-name}")
    public void onBundleSuperseded(BundleSupersededEvent event) {
        log.info("Received BundleSuperseded event {} (oldBundleId={}, newBundleId={})",
                event.eventId(), event.oldBundleId(), event.newBundleId());
        subscriptionService.flagPendingReconsent(event.oldBundleId(), event.newBundleId());
    }
}
