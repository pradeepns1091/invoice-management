package com.invoicemanagement.service;

import com.invoicemanagement.constants.InvoiceStatus;
import com.invoicemanagement.dto.CreateInvoiceRequestDto;
import com.invoicemanagement.dto.InvoicePaymentRequestDto;
import com.invoicemanagement.dto.InvoiceResponseDto;
import com.invoicemanagement.dto.ProcessOverdueInvoiceRequestDto;
import com.invoicemanagement.exception.NotFoundException;
import com.invoicemanagement.exception.ProcessingException;
import com.invoicemanagement.model.Invoice;
import com.invoicemanagement.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    @Spy
    private InvoiceServiceImpl invoiceService;

    @Test
    void createInvoice_validRequest() {
        CreateInvoiceRequestDto requestDto = CreateInvoiceRequestDto.builder().amount(1000.00).dueDate(LocalDate.of(2025, 8, 30)).build();
        Invoice invoice = Invoice.builder().id("123").amount(1000).paidAmount(0).dueDate(requestDto.getDueDate()).status(InvoiceStatus.PENDING).build();
        InvoiceResponseDto responseDto = new InvoiceResponseDto();
        responseDto.setId("123");
        responseDto.setAmount(1000);
        responseDto.setDueDate(requestDto.getDueDate());
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(modelMapper.map(invoice, InvoiceResponseDto.class)).thenReturn(responseDto);

        InvoiceResponseDto result = invoiceService.createInvoice(requestDto);

        assertNotNull(result);
        assertEquals("123", result.getId());
        assertEquals(1000, result.getAmount());
        assertEquals(requestDto.getDueDate(), result.getDueDate());
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice capturedInvoice = invoiceCaptor.getValue();
        assertAll("Invoice Save Object",
                () -> assertNotNull(capturedInvoice.getId()),
                () -> assertEquals(1000.00, capturedInvoice.getAmount()),
                () -> assertEquals(0, capturedInvoice.getPaidAmount()),
                () -> assertEquals(requestDto.getDueDate(), capturedInvoice.getDueDate()),
                () -> assertEquals(InvoiceStatus.PENDING, capturedInvoice.getStatus())
        );
    }

    @Test
    void createInvoice_whenRepositoryException() {
        CreateInvoiceRequestDto requestDto = CreateInvoiceRequestDto.builder().amount(500.00).dueDate(LocalDate.of(2025, 9, 1)).build();
        when(invoiceRepository.save(any(Invoice.class))).thenThrow(new RuntimeException("DB error"));

        ProcessingException exception = assertThrows(ProcessingException.class, () -> invoiceService.createInvoice(requestDto));

        assertTrue(exception.getMessage().contains("Error occurred while creating invoice"));
    }

    @Test
    void createInvoice_whenModelMapperFail() {
        CreateInvoiceRequestDto requestDto = CreateInvoiceRequestDto.builder().amount(750.00).dueDate(LocalDate.of(2025, 9, 5)).build();
        Invoice invoice = Invoice.builder().id("456").amount(750).paidAmount(0).dueDate(requestDto.getDueDate()).status(InvoiceStatus.PENDING).build();
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(modelMapper.map(invoice, InvoiceResponseDto.class)).thenThrow(new RuntimeException("Mapping failed"));

        ProcessingException exception = assertThrows(ProcessingException.class, () -> invoiceService.createInvoice(requestDto));

        assertTrue(exception.getMessage().contains("Error occurred while creating invoice"));
    }

    @Test
    void getAllInvoices_success() {
        Invoice invoice = Invoice.builder().id("123").amount(1000).paidAmount(0).dueDate(LocalDate.of(2025, 8, 30)).status(InvoiceStatus.PENDING).build();
        InvoiceResponseDto invoiceDto = InvoiceResponseDto.builder().id("123").amount(1000).dueDate(invoice.getDueDate()).build();
        List<Invoice> invoiceList = List.of(invoice);
        when(invoiceRepository.findAll()).thenReturn(invoiceList);
        when(modelMapper.map(invoice, InvoiceResponseDto.class)).thenReturn(invoiceDto);

        List<InvoiceResponseDto> result = invoiceService.getAllInvoices();

        assertEquals(1, result.size());
        assertEquals(invoiceDto, result.get(0));
        verify(invoiceRepository).findAll();
        verify(modelMapper).map(invoice, InvoiceResponseDto.class);
    }

    @Test
    void getAllInvoices_whenNoInvoiceRecordFound() {
        when(invoiceRepository.findAll()).thenReturn(Collections.emptyList());

        List<InvoiceResponseDto> result = invoiceService.getAllInvoices();

        assertTrue(result.isEmpty());
        verify(invoiceRepository).findAll();
        verify(modelMapper, never()).map(any(), eq(InvoiceResponseDto.class));
    }

    @Test
    void getAllInvoices_whenRepositoryException() {
        when(invoiceRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        ProcessingException exception = assertThrows(ProcessingException.class, () -> invoiceService.getAllInvoices());

        assertEquals("Error occurred while retrieving all invoices", exception.getMessage());
        verify(invoiceRepository).findAll();
        verify(modelMapper, never()).map(any(), eq(InvoiceResponseDto.class));
    }

    @Test
    void addPayment_whenAmountFullPaid() {
        Invoice invoice = Invoice.builder().id("INV123").amount(1000.0).paidAmount(200.0).status(InvoiceStatus.PENDING).build();
        when(invoiceRepository.findById("INV123")).thenReturn(Optional.of(invoice));
        InvoicePaymentRequestDto paymentRequest = new InvoicePaymentRequestDto();
        paymentRequest.setAmount(800.0);

        invoiceService.addPayment("INV123", paymentRequest);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice capturedInvoice = invoiceCaptor.getValue();
        assertEquals(1000.0, capturedInvoice.getPaidAmount());
        assertEquals(InvoiceStatus.PAID, capturedInvoice.getStatus());
    }

    @Test
    void addPayment_whenAmountPartiallyPaid() {
        Invoice invoice = Invoice.builder().id("INV123").amount(1000.0).paidAmount(200.0).status(InvoiceStatus.PENDING).build();
        when(invoiceRepository.findById("INV123")).thenReturn(Optional.of(invoice));
        InvoicePaymentRequestDto paymentRequest = new InvoicePaymentRequestDto();
        paymentRequest.setAmount(400.0);

        invoiceService.addPayment("INV123", paymentRequest);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice capturedInvoice = invoiceCaptor.getValue();
        assertEquals(600.0, capturedInvoice.getPaidAmount());
        assertEquals(InvoiceStatus.PENDING, capturedInvoice.getStatus());
    }

    @Test
    void addPayment_whenInvoiceNotFound() {
        when(invoiceRepository.findById("INV124")).thenReturn(Optional.empty());
        InvoicePaymentRequestDto paymentRequest = new InvoicePaymentRequestDto();
        paymentRequest.setAmount(900.0);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> invoiceService.addPayment("INV124", paymentRequest));

        assertEquals("Invoice not found with id: INV124", exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void addPayment_whenRepositoryThrowsUnexpectedException() {
        when(invoiceRepository.findById("INV125")).thenThrow(new RuntimeException("DB error"));
        InvoicePaymentRequestDto paymentRequest = new InvoicePaymentRequestDto();
        paymentRequest.setAmount(1000.00);

        ProcessingException exception = assertThrows(ProcessingException.class, () -> invoiceService.addPayment("INV125", paymentRequest));

        assertTrue(exception.getMessage().contains("Error occurred while adding payment to invoice"));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void processOverdue_whenInvoicePartiallyPaid_shouldMarkAsPaidAndCreateNewInvoiceForPendingAmount() {
        LocalDate dueDate = LocalDate.now().minusDays(15);
        Invoice invoice1 = Invoice.builder().id("INV001").amount(1000.0).paidAmount(400.0).dueDate(dueDate).status(InvoiceStatus.PENDING).build();
        Invoice invoice2 = Invoice.builder().id("INV002").amount(1000.0).paidAmount(1000.0).dueDate(dueDate).status(InvoiceStatus.PAID).build();
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice1, invoice2));
        InvoiceResponseDto mockResponse = new InvoiceResponseDto();
        doReturn(mockResponse).when(invoiceService).createInvoice(any(CreateInvoiceRequestDto.class));

        ProcessOverdueInvoiceRequestDto requestDto = new ProcessOverdueInvoiceRequestDto();
        requestDto.setLateFee(100.0);
        requestDto.setOverdueDays(10);

        invoiceService.processOverdue(requestDto);

        verify(invoiceService, times(1)).createInvoice(argThat(dto -> dto.getAmount() == 700.0 && dto.getDueDate().equals(LocalDate.now().plusDays(10))));

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository, times(1)).save(invoiceCaptor.capture());
        Invoice capturedInvoice = invoiceCaptor.getValue();
        assertAll("Invoice Save Object",
                () -> assertEquals("INV001", capturedInvoice.getId()),
                () -> assertEquals(400.0, capturedInvoice.getPaidAmount()),
                () -> assertEquals(InvoiceStatus.PAID, capturedInvoice.getStatus())
        );
    }

    @Test
    void processOverdue_whenInvoiceNotPaid_shouldMarkAsVoidAndCreateNewInvoice() {
        LocalDate dueDate = LocalDate.now().minusDays(15);
        Invoice invoice1 = Invoice.builder().id("INV001").amount(1000.0).paidAmount(0).dueDate(dueDate).status(InvoiceStatus.PENDING).build();
        Invoice invoice2 = Invoice.builder().id("INV002").amount(1000.0).paidAmount(1000.0).dueDate(dueDate).status(InvoiceStatus.PAID).build();
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice1, invoice2));
        InvoiceResponseDto mockResponse = new InvoiceResponseDto();
        doReturn(mockResponse).when(invoiceService).createInvoice(any(CreateInvoiceRequestDto.class));

        ProcessOverdueInvoiceRequestDto requestDto = new ProcessOverdueInvoiceRequestDto();
        requestDto.setLateFee(100.0);
        requestDto.setOverdueDays(10);

        invoiceService.processOverdue(requestDto);

        verify(invoiceService, times(1)).createInvoice(argThat(dto -> dto.getAmount() == 1100.0 && dto.getDueDate().equals(LocalDate.now().plusDays(10))));
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository, times(1)).save(invoiceCaptor.capture());
        Invoice capturedInvoice = invoiceCaptor.getValue();
        assertAll("Invoice Save Object",
                () -> assertEquals("INV001", capturedInvoice.getId()),
                () -> assertEquals(0, capturedInvoice.getPaidAmount()),
                () -> assertEquals(InvoiceStatus.VOID, capturedInvoice.getStatus())
        );
    }

    @Test
    void processOverdue_whenInvoiceNotOverdue_shouldDoNothing() {
        LocalDate dueDate = LocalDate.now().minusDays(5);
        Invoice invoice = Invoice.builder().id("INV003").amount(500.0).paidAmount(0.0).dueDate(dueDate).status(InvoiceStatus.PENDING).build();
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice));

        ProcessOverdueInvoiceRequestDto requestDto = new ProcessOverdueInvoiceRequestDto();
        requestDto.setLateFee(300.0);
        requestDto.setOverdueDays(20);
        invoiceService.processOverdue(requestDto);

        verify(invoiceService, never()).createInvoice(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void processOverdue_whenNoInvoicePaymentPending_shouldBeSkipped() {
        LocalDate dueDate = LocalDate.now().minusDays(20);
        Invoice invoice = Invoice.builder().id("INV004").amount(500.0).paidAmount(0.0).dueDate(dueDate).status(InvoiceStatus.PAID).build();
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice));

        ProcessOverdueInvoiceRequestDto requestDto = new ProcessOverdueInvoiceRequestDto();
        requestDto.setLateFee(200.0);
        requestDto.setOverdueDays(20);
        invoiceService.processOverdue(requestDto);

        verify(invoiceRepository, never()).save(any());
        verify(invoiceService, never()).createInvoice(any());
    }

    @Test
    void processOverdue_whenUnexpectedException_shouldWrapInProcessingException() {
        when(invoiceRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        ProcessOverdueInvoiceRequestDto requestDto = new ProcessOverdueInvoiceRequestDto();
        requestDto.setLateFee(200.0);
        requestDto.setOverdueDays(20);

        ProcessingException exception = assertThrows(ProcessingException.class, () -> invoiceService.processOverdue(requestDto));

        assertTrue(exception.getMessage().contains("Error occurred while processing overdue invoices"));
    }
}