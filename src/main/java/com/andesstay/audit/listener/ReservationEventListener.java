package com.andesstay.audit.listener;

import com.andesstay.audit.domain.AuditEvent;
import com.andesstay.audit.event.ReservationEvent;
import com.andesstay.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventListener {

	private final AuditEventRepository auditEventRepository;

	@KafkaListener(topics = "reservations.events", groupId = "audit-service-group")
	@Transactional
	public void handleReservationEvent(ReservationEvent event) {
		log.info("Recibido evento de reserva: eventId={}, action={}, resourceId={}, actor={}",
				event.eventId(), event.action(), event.resourceId(), event.actor());
		AuditEvent auditEvent = AuditEvent.builder()
				.occurredAt(event.occurredAt())
				.actor(event.actor())
				.action(event.action())
				.resourceType(event.resourceType())
				.resourceId(String.valueOf(event.resourceId()))
				.detail(buildDetail(event))
				.severity("INFO")
				.status("RESOLVED")
				.build();
		auditEventRepository.save(auditEvent);
		log.info("Registro de auditoría persistido para resourceId={} con action={}",
				event.resourceId(), event.action());
	}

	private String buildDetail(ReservationEvent event) {
		StringBuilder detail = new StringBuilder();
		if (event.previousStatus() != null && event.newStatus() != null) {
			detail.append(event.previousStatus()).append(" -> ").append(event.newStatus());
		} else if (event.newStatus() != null) {
			detail.append("newStatus=").append(event.newStatus());
		} else {
			detail.append("action=").append(event.action());
		}
		if (event.unitName() != null && !event.unitName().isBlank()) {
			if (detail.length() > 0) {
				detail.append("; ");
			}
			detail.append("unitName=").append(event.unitName());
		}
		if (event.guests() != null) {
			if (detail.length() > 0) {
				detail.append("; ");
			}
			detail.append("guests=").append(event.guests());
		}
		return detail.toString();
	}
}
