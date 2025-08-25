package com.invoicemanagement.service;

import com.invoicemanagement.dto.CreateInvoiceRequestDto;
import com.invoicemanagement.dto.InvoicePaymentRequestDto;
import com.invoicemanagement.dto.InvoiceResponseDto;
import com.invoicemanagement.dto.ProcessOverdueInvoiceRequestDto;

import java.util.List;

public interface InvoiceService {

    InvoiceResponseDto createInvoice(CreateInvoiceRequestDto createInvoiceRequest);

    List<InvoiceResponseDto> getAllInvoices();

    void addPayment(String invoice, InvoicePaymentRequestDto invoicePaymentRequest);

    void processOverdue(ProcessOverdueInvoiceRequestDto processOverdueInvoiceRequest);
}
