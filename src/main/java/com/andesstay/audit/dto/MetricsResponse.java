package com.andesstay.audit.dto;

import java.time.Instant;
import java.util.List;

public record MetricsResponse(Instant asOf, double occupationPercent, long reservationsToday, long checkInsToday,
		long incidentsOpen, long incidentsNeedReview, double completenessPercent, double availabilityPercent,
		long paymentsConfirmed, long cancellations, List<RiskItem> risks) {
}
