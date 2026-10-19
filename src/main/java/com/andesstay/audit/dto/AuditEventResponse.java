package com.andesstay.audit.dto;

import com.andesstay.audit.domain.AuditEvent;

import java.time.Instant;

public record AuditEventResponse(Long id, Instant occurredAt, String actor, String action, String resourceType,
		String resourceId, String detail, String severity) {

	public static AuditEventResponse from(AuditEvent e) {
		return new AuditEventResponse(e.getId(), e.getOccurredAt(), e.getActor(), e.getAction(), e.getResourceType(),
				e.getResourceId(), e.getDetail(), e.getSeverity());
	}
}
