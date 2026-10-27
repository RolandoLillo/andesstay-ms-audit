package com.andesstay.audit.controller;

import com.andesstay.audit.dto.MetricsResponse;
import com.andesstay.audit.dto.TimelineResponse;
import com.andesstay.audit.service.AuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

	private final AuditService auditService;

	public AuditController(AuditService auditService) {
		this.auditService = auditService;
	}

	@GetMapping("/health")
	public ResponseEntity<Map<String, String>> health() {
		return ResponseEntity.ok(Map.of("status", "UP", "service", "andesstay-ms-audit"));
	}

	@GetMapping("/events")
	public ResponseEntity<TimelineResponse> getEvents(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
			@RequestParam(required = false) Integer limit) {
		return ResponseEntity.ok(auditService.getTimeline(from, to, limit));
	}

	@GetMapping("/metrics")
	public ResponseEntity<MetricsResponse> getMetrics() {
		return ResponseEntity.ok(auditService.getMetrics());
	}
}
