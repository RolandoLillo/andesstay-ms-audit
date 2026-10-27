package com.andesstay.audit.event;

import java.time.Instant;
import java.util.Map;

public record ReservationEvent(
		String eventId,
		Instant occurredAt,
		String actor,
		String action,
		String resourceType,
		Long resourceId,
		Map<String, Object> detail
) {
}
