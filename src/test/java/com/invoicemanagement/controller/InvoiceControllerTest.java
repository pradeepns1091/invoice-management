package com.invoicemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invoicemanagement.dto.CreateInvoiceRequestDto;
import com.invoicemanagement.dto.InvoicePaymentRequestDto;
import com.invoicemanagement.dto.InvoiceResponseDto;
import com.invoicemanagement.dto.ProcessOverdueInvoiceRequestDto;
import com.invoicemanagement.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceService invoiceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createInvoice_success() throws Exception {
        InvoiceResponseDto invoiceResponse = new InvoiceResponseDto();
        invoiceResponse.setId("INV001");
        invoiceResponse.setAmount(1000.00);
        invoiceResponse.setDueDate(LocalDate.of(2025, 8, 30));
        when(invoiceService.createInvoice(any(CreateInvoiceRequestDto.class)))
                .thenReturn(invoiceResponse);

        CreateInvoiceRequestDto createInvoiceRequest = CreateInvoiceRequestDto.builder()
                .amount(1000.00)
                .dueDate(LocalDate.of(2025, 8, 30))
                .build();
        mockMvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createInvoiceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("INV001"));
    }

    @Test
    void createInvoice_whenCreateInvoiceInputIsInvalid_shouldReturnBadRequest() throws Exception {
        CreateInvoiceRequestDto invalidRequest = CreateInvoiceRequestDto.builder()
                .amount(null)
                .dueDate(null)
                .build();

        mockMvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInvoices_shouldReturnListOfInvoices() throws Exception {
        InvoiceResponseDto invoiceResponse = new InvoiceResponseDto();
        invoiceResponse.setId("INV001");
        invoiceResponse.setAmount(1000.00);
        invoiceResponse.setDueDate(LocalDate.of(2025, 8, 30));
        when(invoiceService.getAllInvoices()).thenReturn(List.of(invoiceResponse));

        mockMvc.perform(get("/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("INV001"));
    }

    @Test
    void payInvoice_success() throws Exception {
        InvoicePaymentRequestDto paymentRequest = new InvoicePaymentRequestDto();
        paymentRequest.setAmount(500.00);

        mockMvc.perform(post("/invoices/INV001/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk());

        verify(invoiceService).addPayment(eq("INV001"), any(InvoicePaymentRequestDto.class));
    }

    @Test
    void payInvoice_whenPaymentInputIsInvalid_shouldReturnBadRequest() throws Exception {
        InvoicePaymentRequestDto invalidPayment = new InvoicePaymentRequestDto(); // missing amount

        mockMvc.perform(post("/invoices/INV001/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payInvoice_whenInvoiceNotFoundForPayment_shouldReturnInternalServerError() throws Exception {
        InvoicePaymentRequestDto paymentRequest = new InvoicePaymentRequestDto();
        paymentRequest.setAmount(500.00);

        doThrow(new RuntimeException("Invoice not found"))
                .when(invoiceService).addPayment(eq("INVALID_ID"), any());

        mockMvc.perform(post("/invoices/INVALID_ID/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void processOverdue_success() throws Exception {
        ProcessOverdueInvoiceRequestDto overdueRequest = new ProcessOverdueInvoiceRequestDto();
        overdueRequest.setOverdueDays(5);
        overdueRequest.setLateFee(10.0);

        mockMvc.perform(post("/invoices/process-overdue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overdueRequest)))
                .andExpect(status().isOk());

        verify(invoiceService).processOverdue(overdueRequest);
    }

    @Test
    void processOverdue_whenOverdueRequestIsInvalid_shouldReturnBadRequest() throws Exception {
        ProcessOverdueInvoiceRequestDto invalidRequest = new ProcessOverdueInvoiceRequestDto(); // missing percentage

        mockMvc.perform(post("/invoices/process-overdue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}