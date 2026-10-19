package com.andesstay.audit.service;

import com.andesstay.audit.dto.AuditEventResponse;
import com.andesstay.audit.dto.MetricsResponse;
import com.andesstay.audit.dto.RiskItem;
import com.andesstay.audit.dto.TimelineResponse;
import com.andesstay.audit.repository.AuditEventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

	private static final int DEFAULT_LIMIT = 50;
	private static final int MAX_LIMIT = 200;

	private final AuditEventRepository auditEventRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional(readOnly = true)
	public TimelineResponse getTimeline(Instant from, Instant to, Integer limit) {
		int effectiveLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
		PageRequest pageable = PageRequest.of(0, effectiveLimit);
		var events = (from != null && to != null)
				? auditEventRepository.findByRange(from, to, pageable)
				: (from != null)
						? auditEventRepository.findByFrom(from, pageable)
						: (to != null)
								? auditEventRepository.findByTo(to, pageable)
								: auditEventRepository.findAllByOrderByOccurredAtDesc(pageable);
		return new TimelineResponse(events.stream().map(AuditEventResponse::from).toList());
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public MetricsResponse getMetrics() {
		Long total = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM reservations").getSingleResult();
		total = total == null ? 0L : total;

		Long checkInsToday = (Long) entityManager.createNativeQuery(
				"SELECT COUNT(*) FROM reservations WHERE status = 'EN_ESTADIA'").getSingleResult();
		checkInsToday = checkInsToday == null ? 0L : checkInsToday;

		Long paymentsConfirmed = (Long) entityManager.createNativeQuery(
				"SELECT COUNT(*) FROM reservations WHERE status = 'CONFIRMADA'").getSingleResult();
		paymentsConfirmed = paymentsConfirmed == null ? 0L : paymentsConfirmed;

		Long cancellations = (Long) entityManager.createNativeQuery(
				"SELECT COUNT(*) FROM reservations WHERE status = 'CANCELADA'").getSingleResult();
		cancellations = cancellations == null ? 0L : cancellations;

		Long reservationsToday = (Long) entityManager.createNativeQuery(
				"SELECT COUNT(*) FROM reservations WHERE check_in_date = CURRENT_DATE").getSingleResult();
		reservationsToday = reservationsToday == null ? 0L : reservationsToday;

		Long completeCount = (Long) entityManager.createNativeQuery(
				"SELECT COUNT(*) FROM reservations WHERE customer_user_id IS NOT NULL AND unit_id IS NOT NULL AND unit_id > 0 AND check_in_date IS NOT NULL AND check_out_date IS NOT NULL AND amount IS NOT NULL")
				.getSingleResult();
		completeCount = completeCount == null ? 0L : completeCount;

		double completenessPercent = total == 0 ? 0.0 : round1((completeCount.doubleValue() / total.doubleValue()) * 100.0);
		double occupationPercent = total == 0 ? 0.0
				: round1((double) (paymentsConfirmed + checkInsToday) / total.doubleValue() * 100.0);
		double availabilityPercent = round1(100.0 - occupationPercent);

		long incidentsOpen = auditEventRepository.countBySeverityInAndStatusIn(
				List.of("WARN", "ERROR"), List.of("OPEN"));
		long incidentsNeedReview = auditEventRepository.countBySeverityInAndStatusIn(
				List.of("WARN", "ERROR"), List.of("NEED_REVIEW"));

		var risks = new java.util.ArrayList<RiskItem>();
		if (paymentsConfirmed > 0) {
			risks.add(new RiskItem("Pago pendiente", paymentsConfirmed + " casos", "INFO"));
		}
		if (cancellations > 0) {
			risks.add(new RiskItem("Cancelaciones", cancellations + " casos", "WARN"));
		}
		if (incidentsOpen > 0) {
			risks.add(new RiskItem("Incidencias abiertas", incidentsOpen + " casos", "WARN"));
		}
		risks.add(new RiskItem("Servicio de lavandería", "Sin novedad", "INFO"));

		return new MetricsResponse(Instant.now(), occupationPercent, reservationsToday, checkInsToday,
				incidentsOpen, incidentsNeedReview, completenessPercent, availabilityPercent,
				paymentsConfirmed, cancellations, risks);
	}

	private static double round1(double value) {
		return Math.round(value * 10.0) / 10.0;
	}
}
