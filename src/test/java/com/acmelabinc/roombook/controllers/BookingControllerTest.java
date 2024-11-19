package com.acmelabinc.roombook.controllers;

import com.acmelabinc.roombook.dtos.BookingRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetBookingsPerRoom() throws Exception {

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(builidValidBookingRequestDto()));

        mockMvc.perform(mockRequest);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/bookings?roomName=Earth&date=2024-11-17")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testGetAll() throws Exception {

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(builidValidBookingRequestDto()));

        mockMvc.perform(mockRequest);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/bookings/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testSave() throws Exception {

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(builidValidBookingRequestDto()));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testSave_RoomNotFound() throws Exception {

        BookingRequestDto requestDto = builidValidBookingRequestDto();
        requestDto.setRoomName("Milky Way");

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestDto));

        mockMvc.perform(mockRequest)
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testSave_Overlap() throws Exception {

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(builidValidBookingRequestDto()));

        mockMvc.perform(mockRequest);

        mockMvc.perform(mockRequest)
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    public void testSave_OverlapButFirstRequestIsSuccessful() throws Exception {

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(builidValidBookingRequestDto()));

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(mockRequest)
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    public void testSave_InvalidDuration() throws Exception {

        BookingRequestDto requestDto = builidValidBookingRequestDto();
        requestDto.setEndTime(LocalTime.of(10, 30));

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestDto));

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testCancel() throws Exception {

        BookingRequestDto requestDto = builidValidBookingRequestDto();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestDto)));

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.delete("/api/bookings/cancel/1");

        mockMvc.perform(mockRequest)
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testCancel_ForPastBooking() throws Exception {

        BookingRequestDto requestDto = buildValidBookingRequestDtoForCancelTest();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestDto)));

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.delete("/api/bookings/cancel/1");

        mockMvc.perform(mockRequest)
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    private static BookingRequestDto builidValidBookingRequestDto() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setRoomName("Earth");
        requestDto.setEmployeeEmail("pluto@acme.com");
        requestDto.setBookingDate(LocalDate.of(2024, 12, 17));
        requestDto.setStartTime(LocalTime.of(17, 0));
        requestDto.setEndTime(LocalTime.of(18, 0));
        return requestDto;
    }

    private static BookingRequestDto buildValidBookingRequestDtoForCancelTest() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setRoomName("Earth");
        requestDto.setEmployeeEmail("pluto@acme.com");
        requestDto.setBookingDate(LocalDate.now());
        requestDto.setStartTime(LocalTime.of(5, 0));
        requestDto.setEndTime(LocalTime.of(6, 0));
        return requestDto;
    }

    private String asJsonString(Object o) throws JsonProcessingException {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule())
                .build();

        return mapper.writeValueAsString(o);
    }
}
