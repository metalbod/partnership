package com.company.partnership.transactionprofitshare.client;

import com.company.partnership.transactionprofitshare.config.UpstreamProperties;
import com.company.partnership.transactionprofitshare.exception.NotFoundException;
import com.company.partnership.transactionprofitshare.exception.UpstreamServiceException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class RemoteCatalogClient implements CatalogClient {

    private final RestClient subscriptions;
    private final RestClient bundles;
    private final RestClient offerings;

    public RemoteCatalogClient(UpstreamProperties props) {
        this.subscriptions = client(props.partnerSubscriptionUrl());
        this.bundles = client(props.ecosystemBundleUrl());
        this.offerings = client(props.vendorOfferingUrl());
    }

    private static RestClient client(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public SubscriptionTerms getSubscription(UUID id) {
        return fetch(subscriptions, "/v1/subscriptions/" + id, SubscriptionTerms.class, "Subscription " + id);
    }

    @Override
    public BundleInfo getBundle(UUID id) {
        return fetch(bundles, "/v1/bundles/" + id, BundleInfo.class, "Bundle " + id);
    }

    @Override
    public OfferingInfo getOffering(UUID id) {
        return fetch(offerings, "/v1/offerings/" + id, OfferingInfo.class, "Offering " + id);
    }

    private static <T> T fetch(RestClient client, String path, Class<T> type, String what) {
        try {
            T body = client.get().uri(path).retrieve().body(type);
            if (body == null) throw new UpstreamServiceException(what + " returned an empty response");
            return body;
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException(what + " not found");
        } catch (RestClientException e) {
            throw new UpstreamServiceException("Couldn't read " + what + ": " + e.getMessage());
        }
    }
}
