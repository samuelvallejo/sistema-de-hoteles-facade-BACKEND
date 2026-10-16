package com.hotel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HotelApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String request(int roomId, String start, String end, String season) throws Exception {
        return json.writeValueAsString(Map.of("roomId", roomId, "checkInDate", start, "checkOutDate", end,
                "season", season, "guest", Map.of("name", "Ana Pérez", "document", "12345",
                        "email", "ana@example.com", "phone", "+57 3001234567")));
    }

    private String reserve(int roomId, String start, String end, String season) throws Exception {
        String response = mvc.perform(post("/api/hotel/reservar").contentType(MediaType.APPLICATION_JSON)
                .content(request(roomId, start, end, season))).andExpect(status().isCreated())
                .andExpect(header().exists("Location")).andReturn().getResponse().getContentAsString();
        return json.readTree(response).path("data").path("id").asText();
    }

    @Test
    void completeLifecycleProducesExactInvoiceAndReleasesRoom() throws Exception {
        String id = reserve(1, "2030-06-01", "2030-06-04", "HIGH");
        mvc.perform(get("/api/hotel/reserva/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rate.nights").value(3))
                .andExpect(jsonPath("$.data.rate.total").value(450.0))
                .andExpect(jsonPath("$.data.digitalKey").isEmpty());
        for (String type : new String[]{"SPA", "BREAKFAST", "TRANSFER"}) {
            mvc.perform(post("/api/hotel/servicios/" + id).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(Map.of("serviceType", type)))).andExpect(status().isOk());
        }
        mvc.perform(put("/api/hotel/checkout/" + id)).andExpect(status().isConflict());
        String checkedIn = mvc.perform(put("/api/hotel/checkin/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CHECKED_IN"))
                .andReturn().getResponse().getContentAsString();
        assertThat(UUID.fromString(json.readTree(checkedIn).at("/data/digitalKey").asText())).isNotNull();
        mvc.perform(put("/api/hotel/checkin/" + id)).andExpect(status().isConflict());
        String checkout = mvc.perform(put("/api/hotel/checkout/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CHECKED_OUT"))
                .andExpect(jsonPath("$.data.servicesTotal").value(125.0))
                .andExpect(jsonPath("$.data.invoice.total").value(575.0))
                .andExpect(jsonPath("$.data.invoice.subtotal").value(575.0))
                .andExpect(jsonPath("$.data.invoice.currency").value("USD"))
                .andExpect(jsonPath("$.data.invoice.lines", hasSize(5)))
                .andExpect(jsonPath("$.data.invoice.lines[1].description").value("Recargo de temporada alta por noche (50 %)"))
                .andExpect(jsonPath("$.data.digitalKey").isEmpty())
                .andReturn().getResponse().getContentAsString();
        JsonNode invoice = json.readTree(checkout).at("/data/invoice");
        mvc.perform(put("/api/hotel/checkout/" + id)).andExpect(status().isConflict());
        mvc.perform(post("/api/hotel/servicios/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"serviceType\":\"SPA\"}")).andExpect(status().isConflict());
        String detail = mvc.perform(get("/api/hotel/reserva/" + id)).andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(detail).at("/data/invoice")).isEqualTo(invoice);
        reserve(1, "2030-06-01", "2030-06-04", "LOW");
    }

    @Test
    void availabilityFiltersAndQuotesAllFifteenRooms() throws Exception {
        mvc.perform(get("/api/hotel/disponibilidad").param("checkInDate", "2031-01-01")
                .param("checkOutDate", "2031-01-03"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data", hasSize(15)));
        mvc.perform(get("/api/hotel/disponibilidad").param("checkInDate", "2031-01-01")
                .param("checkOutDate", "2031-01-03").param("roomType", "SUITE").param("season", "HIGH"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].rate.total").value(750.0));
    }

    @Test
    void overlappingDatesConflictButAdjacentDatesAreAllowed() throws Exception {
        reserve(2, "2032-03-10", "2032-03-13", "LOW");
        mvc.perform(post("/api/hotel/reservar").contentType(MediaType.APPLICATION_JSON)
                .content(request(2, "2032-03-12", "2032-03-14", "LOW")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ROOM_UNAVAILABLE"));
        reserve(2, "2032-03-13", "2032-03-15", "LOW");
        mvc.perform(get("/api/hotel/disponibilidad").param("checkInDate", "2032-03-11")
                .param("checkOutDate", "2032-03-12").param("roomType", "SINGLE"))
                .andExpect(jsonPath("$.data", hasSize(5)));
    }

    @Test
    void validatesDatesGuestAndUnknownFieldsWithoutChangingInventory() throws Exception {
        mvc.perform(post("/api/hotel/reservar").contentType(MediaType.APPLICATION_JSON)
                .content(request(3, "2033-01-03", "2033-01-03", "LOW")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_DATES"));
        String body = request(3, "2033-01-01", "2033-01-02", "LOW").replace("ana@example.com", "invalid");
        mvc.perform(post("/api/hotel/reservar").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors['guest.email']").value("El correo no es válido."));
        mvc.perform(post("/api/hotel/reservar").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.roomId").exists());
        mvc.perform(post("/api/hotel/reservar").contentType(MediaType.APPLICATION_JSON)
                .content(request(3, "2033-01-01", "2033-01-02", "LOW").replace("\"roomId\"", "\"unknown\"")))
                .andExpect(status().isBadRequest());
        reserve(3, "2033-01-01", "2033-01-02", "LOW");
    }

    @Test
    void malformedRequestsAndMissingResourcesUseConsistentSpanishErrors() throws Exception {
        ResultActions[] requests = {
                mvc.perform(get("/api/hotel/reserva/not-a-uuid")),
                mvc.perform(get("/api/hotel/disponibilidad")),
                mvc.perform(get("/api/hotel/disponibilidad").param("checkInDate", "invalid").param("checkOutDate", "2030-01-02")),
                mvc.perform(post("/api/hotel/reservar").contentType(MediaType.APPLICATION_JSON).content("{"))
        };
        for (ResultActions result : requests) {
            result.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(startsWith("La solicitud no es válida.")))
                    .andExpect(jsonPath("$.path").exists()).andExpect(jsonPath("$.errors").isMap());
        }
        mvc.perform(get("/api/hotel/reserva/" + UUID.randomUUID())).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
        mvc.perform(post("/api/hotel/reservar").contentType(MediaType.APPLICATION_JSON)
                .content(request(99, "2034-01-01", "2034-01-02", "LOW"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/missing")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("El recurso solicitado no existe."));
    }

    @Test
    void duplicateServiceIsRejectedAndBreakfastUsesStayLength() throws Exception {
        String id = reserve(7, "2035-01-01", "2035-01-05", "LOW");
        mvc.perform(post("/api/hotel/servicios/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"serviceType\":\"BREAKFAST\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.services[0].quantity").value(4))
                .andExpect(jsonPath("$.data.servicesTotal").value(60.0))
                .andExpect(jsonPath("$.data.total").value(660.0));
        mvc.perform(post("/api/hotel/servicios/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"serviceType\":\"BREAKFAST\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SERVICE_ALREADY_ADDED"));
    }

    @Test
    void corsAllowsConfiguredFrontendAndRejectsOtherOrigins() throws Exception {
        mvc.perform(options("/api/hotel/reservar").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/api/hotel/reservar").header("Origin", "https://untrusted.example")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
