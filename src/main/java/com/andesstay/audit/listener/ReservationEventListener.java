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
		java.util.Map<String, Object> eventDetail = event.detail();
		String previousStatus = null;
		String newStatus = null;
		String unitName = null;
		Object guests = null;
		if (eventDetail != null) {
			Object prev = eventDetail.get("previousStatus");
			if (prev != null) {
				previousStatus = String.valueOf(prev);
			}
			Object neu = eventDetail.get("newStatus");
			if (neu != null) {
				newStatus = String.valueOf(neu);
			}
			Object unit = eventDetail.get("unitName");
			if (unit != null) {
				unitName = String.valueOf(unit);
			}
			guests = eventDetail.get("guests");
		}
		if (previousStatus != null && !previousStatus.isBlank() && newStatus != null && !newStatus.isBlank()) {
			detail.append(previousStatus).append(" -> ").append(newStatus);
		} else if (newStatus != null && !newStatus.isBlank()) {
			detail.append("newStatus=").append(newStatus);
		} else {
			detail.append("action=").append(event.action());
		}
		if (unitName != null && !unitName.isBlank()) {
			if (detail.length() > 0) {
				detail.append("; ");
			}
			detail.append("unitName=").append(unitName);
		}
		if (guests != null) {
			if (detail.length() > 0) {
				detail.append("; ");
			}
			detail.append("guests=").append(guests);
		}
		return detail.toString();
	}
}
