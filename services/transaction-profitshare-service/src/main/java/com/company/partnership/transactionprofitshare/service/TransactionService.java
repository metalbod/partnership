package com.company.partnership.transactionprofitshare.service;

import com.company.partnership.transactionprofitshare.client.CatalogClient;
import com.company.partnership.transactionprofitshare.client.CatalogClient.BundleInfo;
import com.company.partnership.transactionprofitshare.client.CatalogClient.OfferingInfo;
import com.company.partnership.transactionprofitshare.client.CatalogClient.SubscriptionTerms;
import com.company.partnership.transactionprofitshare.domain.OfferingLine;
import com.company.partnership.transactionprofitshare.domain.Transaction;
import com.company.partnership.transactionprofitshare.dto.TransactionRequest;
import com.company.partnership.transactionprofitshare.exception.NotFoundException;
import com.company.partnership.transactionprofitshare.exception.UnprocessableTransactionException;
import com.company.partnership.transactionprofitshare.pricing.PricingCalculator;
import com.company.partnership.transactionprofitshare.pricing.PricingCalculator.Breakdown;
import com.company.partnership.transactionprofitshare.pricing.PricingCalculator.Kind;
import com.company.partnership.transactionprofitshare.pricing.PricingCalculator.OfferingTerms;
import com.company.partnership.transactionprofitshare.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

// FR-RPT-01, FR-RPT-02: capture a customer's whole-bundle purchase through a partner's programme,
// snapshotting the offerings in the bundle and the profit-share breakdown at that moment.
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    /** Consumers keep buying the old bundle version while a subscription awaits re-consent (BRD 4.2). */
    private static final Set<String> SELLABLE = Set.of("ACTIVE", "PENDING_RECONSENT");

    private final TransactionRepository transactionRepository;
    private final CatalogClient catalog;

    public Transaction record(TransactionRequest req) {
        SubscriptionTerms sub = catalog.getSubscription(req.subscriptionId());
        if (!SELLABLE.contains(sub.status())) {
            throw new UnprocessableTransactionException("This partner programme is " + sub.status() + " and can't take new customers.");
        }
        if (sub.subscriptionPrice() == null || sub.subscriptionPrice().signum() <= 0) {
            throw new UnprocessableTransactionException("This partner programme has no bundle cost set yet.");
        }
        BundleInfo bundle = catalog.getBundle(sub.bundleId());
        List<OfferingTerms> terms = bundle.offeringIds().stream().map(id -> {
            OfferingInfo o = catalog.getOffering(id);
            return new OfferingTerms(o.id(), o.name(), o.vendorId(), Kind.valueOf(o.priceType()), o.priceValue());
        }).toList();

        Breakdown breakdown = PricingCalculator.compute(
                sub.subscriptionPrice(), Kind.valueOf(sub.partnerShareType()), sub.partnerShareValue(), terms);

        Transaction tx = new Transaction();
        tx.setCustomerName(req.customerName().trim());
        tx.setCustomerPhone(req.customerPhone().trim());
        tx.setCustomerEmail(req.customerEmail().trim());
        tx.setSubscriptionId(sub.id());
        tx.setPartnerId(sub.partnerId());
        tx.setBundleId(bundle.id());
        tx.setBundleName(bundle.name());
        tx.setBundleVersion(bundle.version());
        tx.setEcoSystemId(bundle.ecoSystemId());
        tx.setAmount(breakdown.price());
        breakdown.vendorShares().forEach(s -> tx.getOfferingLines().add(new OfferingLine(
                s.terms().offeringId(), s.terms().offeringName(), s.terms().vendorId(),
                OfferingLine.PriceType.valueOf(s.terms().kind().name()), s.terms().value(), s.amount())));
        tx.setPartnerAmount(breakdown.partnerAmount());
        tx.setCompanyAmount(breakdown.companyAmount());
        tx.setPremium(req.premium());
        tx.setSumInsured(req.sumInsured());
        tx.setPolicyNumber(req.policyNumber());
        return transactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public Transaction findById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Transaction> findAll() {
        return transactionRepository.findAllByOrderByTransactionTimestampDesc();
    }
}
