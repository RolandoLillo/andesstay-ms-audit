package com.andesstay.audit.listener;

import com.andesstay.audit.domain.AuditEvent;
import com.andesstay.audit.event.ReservationEvent;
import com.andesstay.audit.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReservationEventListenerTest {

	private static final String RESERVATION_CREATED_JSON = """
			{"eventId":"evt-1","occurredAt":"2026-10-26T12:00:00Z","actor":"cliente@x.cl","action":"RESERVATION_CREATED","resourceType":"RESERVATION","resourceId":7,"detail":{"newStatus":"PENDING","previousStatus":null,"unitName":"Cabana 1","guests":2}}
			""";

	private static final String RESERVATION_STATUS_CHANGED_JSON = """
			{"eventId":"evt-2","occurredAt":"2026-10-26T13:00:00Z","actor":"operador@x.cl","action":"RESERVATION_STATUS_CHANGED","resourceType":"RESERVATION","resourceId":7,"detail":{"previousStatus":"PENDING","newStatus":"CONFIRMED","unitId":10}}
			""";

	private static final String RESERVATION_WITHOUT_DETAIL_JSON = """
			{"eventId":"evt-3","occurredAt":"2026-10-26T14:00:00Z","actor":"sistema@x.cl","action":"RESERVATION_CREATED","resourceType":"RESERVATION","resourceId":7,"detail":null}
			""";

	@Test
	void consumeElJsonExactoDelProductorParaReservaCreada() {
		AuditEventRepository repository = mock(AuditEventRepository.class);
		ReservationEventListener listener = new ReservationEventListener(repository);

		ReservationEvent event = deserializar(RESERVATION_CREATED_JSON);

		listener.handleReservationEvent(event);

		ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
		verify(repository).save(captor.capture());
		AuditEvent saved = captor.getValue();

		assertThat(saved.getOccurredAt()).isEqualTo(Instant.parse("2026-10-26T12:00:00Z"));
		assertThat(saved.getActor()).isEqualTo("cliente@x.cl");
		assertThat(saved.getAction()).isEqualTo("RESERVATION_CREATED");
		assertThat(saved.getResourceType()).isEqualTo("RESERVATION");
		assertThat(saved.getResourceId()).isEqualTo("7");
		assertThat(saved.getDetail()).contains("PENDING").contains("Cabana 1");
		assertThat(saved.getSeverity()).isEqualTo("INFO");
		assertThat(saved.getStatus()).isEqualTo("RESOLVED");
	}

	@Test
	void consumeElJsonExactoDelProductorParaCambioDeEstado() {
		AuditEventRepository repository = mock(AuditEventRepository.class);
		ReservationEventListener listener = new ReservationEventListener(repository);

		ReservationEvent event = deserializar(RESERVATION_STATUS_CHANGED_JSON);

		listener.handleReservationEvent(event);

		ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
		verify(repository).save(captor.capture());
		AuditEvent saved = captor.getValue();

		assertThat(saved.getOccurredAt()).isEqualTo(Instant.parse("2026-10-26T13:00:00Z"));
		assertThat(saved.getActor()).isEqualTo("operador@x.cl");
		assertThat(saved.getAction()).isEqualTo("RESERVATION_STATUS_CHANGED");
		assertThat(saved.getResourceId()).isEqualTo("7");
		assertThat(saved.getDetail()).isEqualTo("PENDING -> CONFIRMED");
	}

	@Test
	void toleraDetailNuloDelProductor() {
		AuditEventRepository repository = mock(AuditEventRepository.class);
		ReservationEventListener listener = new ReservationEventListener(repository);

		ReservationEvent event = deserializar(RESERVATION_WITHOUT_DETAIL_JSON);

		listener.handleReservationEvent(event);

		ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
		verify(repository).save(captor.capture());
		assertThat(captor.getValue().getDetail()).isEqualTo("action=RESERVATION_CREATED");
	}

	private ReservationEvent deserializar(String json) {
		JsonDeserializer<ReservationEvent> deserializer = new JsonDeserializer<>();
		deserializer.configure(Map.of(
				JsonDeserializer.TRUSTED_PACKAGES, "com.andesstay.*",
				JsonDeserializer.USE_TYPE_INFO_HEADERS, false,
				JsonDeserializer.VALUE_DEFAULT_TYPE, ReservationEvent.class.getName()), false);
		return deserializer.deserialize("reservations.events", json.getBytes(StandardCharsets.UTF_8));
	}
}
