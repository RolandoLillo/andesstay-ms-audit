package com.andesstay.audit.repository;

import com.andesstay.audit.domain.AuditEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

	List<AuditEvent> findTop50ByOrderByOccurredAtDesc();

	List<AuditEvent> findAllByOrderByOccurredAtDesc(Pageable pageable);

	@Query("SELECT e FROM AuditEvent e WHERE e.occurredAt >= :from AND e.occurredAt <= :to ORDER BY e.occurredAt DESC")
	List<AuditEvent> findByRange(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

	@Query("SELECT e FROM AuditEvent e WHERE e.occurredAt >= :from ORDER BY e.occurredAt DESC")
	List<AuditEvent> findByFrom(@Param("from") Instant from, Pageable pageable);

	@Query("SELECT e FROM AuditEvent e WHERE e.occurredAt <= :to ORDER BY e.occurredAt DESC")
	List<AuditEvent> findByTo(@Param("to") Instant to, Pageable pageable);

	@Query("SELECT COUNT(e) FROM AuditEvent e WHERE e.severity IN :severities AND e.status IN :statuses")
	long countBySeverityInAndStatusIn(@Param("severities") Collection<String> severities,
			@Param("statuses") Collection<String> statuses);
}
