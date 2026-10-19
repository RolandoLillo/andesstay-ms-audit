package com.andesstay.audit.dto;

import java.util.List;

public record TimelineResponse(List<AuditEventResponse> items) {
}
