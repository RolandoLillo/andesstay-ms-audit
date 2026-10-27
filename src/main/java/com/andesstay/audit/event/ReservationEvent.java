package com.andesstay.audit.event;

import java.time.Instant;

public record ReservationEvent(
		String eventId,
		Instant occurredAt,
		String actor,
		String action,
		String resourceType,
		Long resourceId,
		String previousStatus,
		String newStatus,
		String unitName,
		Integer guests
) {
}
