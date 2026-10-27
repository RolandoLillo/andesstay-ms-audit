package com.andesstay.audit.controller;

import com.andesstay.audit.dto.AuditEventResponse;
import com.andesstay.audit.dto.MetricsResponse;
import com.andesstay.audit.dto.RiskItem;
import com.andesstay.audit.dto.TimelineResponse;
import com.andesstay.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
class AuditControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuditService auditService;

	@Test
	void devuelveElTimelineDeAuditoria() throws Exception {
		when(auditService.getTimeline(null, null, 2)).thenReturn(new TimelineResponse(List.of(
				new AuditEventResponse(1L, Instant.parse("2026-10-18T21:00:00Z"), "operador@andesstay.local",
						"RESERVATION_STATUS_CHANGED", "RESERVATION", "42", "CREADA -> CONFIRMADA", "INFO"))));

		mockMvc.perform(get("/api/audit/events").param("limit", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].id").value(1))
				.andExpect(jsonPath("$.items[0].occurredAt").value("2026-10-18T21:00:00Z"))
				.andExpect(jsonPath("$.items[0].actor").value("operador@andesstay.local"))
				.andExpect(jsonPath("$.items[0].action").value("RESERVATION_STATUS_CHANGED"))
				.andExpect(jsonPath("$.items[0].resourceType").value("RESERVATION"))
				.andExpect(jsonPath("$.items[0].resourceId").value("42"))
				.andExpect(jsonPath("$.items[0].detail").value("CREADA -> CONFIRMADA"))
				.andExpect(jsonPath("$.items[0].severity").value("INFO"));
	}

	@Test
	void devuelveLasMetricasDelPanel() throws Exception {
		when(auditService.getMetrics()).thenReturn(new MetricsResponse(
				Instant.parse("2026-10-18T21:30:00Z"), 84.0, 26, 9, 3, 1, 96.0, 16.0, 22, 2,
				List.of(new RiskItem("Pago pendiente", "22 casos", "INFO"),
						new RiskItem("Cancelaciones", "2 casos", "WARN"))));

		mockMvc.perform(get("/api/audit/metrics"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.asOf").value("2026-10-18T21:30:00Z"))
				.andExpect(jsonPath("$.occupationPercent").value(84.0))
				.andExpect(jsonPath("$.reservationsToday").value(26))
				.andExpect(jsonPath("$.checkInsToday").value(9))
				.andExpect(jsonPath("$.incidentsOpen").value(3))
				.andExpect(jsonPath("$.incidentsNeedReview").value(1))
				.andExpect(jsonPath("$.completenessPercent").value(96.0))
				.andExpect(jsonPath("$.availabilityPercent").value(16.0))
				.andExpect(jsonPath("$.paymentsConfirmed").value(22))
				.andExpect(jsonPath("$.cancellations").value(2))
				.andExpect(jsonPath("$.risks.length()").value(2))
				.andExpect(jsonPath("$.risks[0].label").value("Pago pendiente"))
				.andExpect(jsonPath("$.risks[0].status").value("22 casos"))
				.andExpect(jsonPath("$.risks[1].severity").value("WARN"));
	}
}
