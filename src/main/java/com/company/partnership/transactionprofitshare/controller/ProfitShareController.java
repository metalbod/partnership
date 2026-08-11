package com.company.partnership.transactionprofitshare.controller;

import com.company.partnership.transactionprofitshare.domain.ProfitShareReport;
import com.company.partnership.transactionprofitshare.service.ProfitShareCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * MVP exposes the periodic calculation as an on-demand endpoint for local testing;
 * production trigger is the scheduled AWS Lambda job (TDD Section 4.4), not this
 * HTTP endpoint directly.
 */
@RestController
@RequestMapping("/v1/profit-share")
@RequiredArgsConstructor
public class ProfitShareController {

    private final ProfitShareCalculationService calculationService;

    @PostMapping("/run")
    public ProfitShareReport run(@RequestParam LocalDate periodStart, @RequestParam LocalDate periodEnd) {
        return calculationService.runPeriodicCalculation(periodStart, periodEnd);
    }
}
