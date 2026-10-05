package com.company.partnership.transactionprofitshare.controller;

import com.company.partnership.transactionprofitshare.dto.ProfitShareReportResponse;
import com.company.partnership.transactionprofitshare.service.ProfitShareCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public ResponseEntity<?> run(@RequestParam LocalDate periodStart, @RequestParam LocalDate periodEnd) {
        ProfitShareReportResponse report = calculationService.runPeriodicCalculation(periodStart, periodEnd);
        if (report == null) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                    "status", 422, "error", "Unprocessable Entity",
                    "message", "No unreported transactions fall between " + periodStart + " and " + periodEnd + "."));
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports")
    public List<ProfitShareReportResponse> reports() {
        return calculationService.findReports();
    }

    @GetMapping("/reports/{id}")
    public ProfitShareReportResponse report(@PathVariable UUID id) {
        return calculationService.findReport(id);
    }
}
