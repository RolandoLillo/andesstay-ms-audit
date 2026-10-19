package com.andesstay.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "actor", nullable = false, length = 200)
	private String actor;

	@Column(name = "action", nullable = false, length = 100)
	private String action;

	@Column(name = "resource_type", nullable = false, length = 100)
	private String resourceType;

	@Column(name = "resource_id", length = 100)
	private String resourceId;

	@Column(name = "detail", length = 1000)
	private String detail;

	@Column(name = "severity", nullable = false, length = 20)
	private String severity;

	@Column(name = "status", nullable = false, length = 20)
	private String status;
}
