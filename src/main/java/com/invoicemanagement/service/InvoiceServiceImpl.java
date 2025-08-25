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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ModelMapper modelMapper;

    @Override
    public InvoiceResponseDto createInvoice(CreateInvoiceRequestDto createInvoiceRequest) {
        try {
            log.info("Creating invoice, amount: {}, dueDate: {}", createInvoiceRequest.getAmount(), createInvoiceRequest.getDueDate());
            String id = UUID.randomUUID().toString();
            Invoice invoice = Invoice.builder().id(id).amount(createInvoiceRequest.getAmount()).paidAmount(0).dueDate(createInvoiceRequest.getDueDate()).status(InvoiceStatus.PENDING).build();
            Invoice savedInvoice = invoiceRepository.save(invoice);
            log.info("Created invoice successfully, amount: {}, dueDate: {}, id:{}", createInvoiceRequest.getAmount(), createInvoiceRequest.getDueDate(), savedInvoice.getId());
            return modelMapper.map(savedInvoice, InvoiceResponseDto.class);
        } catch (Exception e) {
            throw new ProcessingException("Error occurred while creating invoice, amount: " + createInvoiceRequest.getAmount() + ", dueDate: " + createInvoiceRequest.getAmount(), e);
        }
    }

    @Override
    public List<InvoiceResponseDto> getAllInvoices() {
        try {
            log.info("Retrieving all invoice");
            List<InvoiceResponseDto> invoices = invoiceRepository.findAll().stream().map(invoice -> modelMapper.map(invoice, InvoiceResponseDto.class)).toList();
            log.info("Retrieved all invoice successfully, count: {}", invoices.size());
            return invoices;
        } catch (Exception e) {
            throw new ProcessingException("Error occurred while retrieving all invoices", e);
        }
    }

    @Override
    public void addPayment(String invoiceId, InvoicePaymentRequestDto invoicePaymentRequest) {
        try {
            log.info("Adding payment to invoice, invoiceId: {}, amount: {}", invoiceId, invoicePaymentRequest.getAmount());
            Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));
            invoice.setPaidAmount(invoice.getPaidAmount() + invoicePaymentRequest.getAmount());
            if (invoice.getPaidAmount() >= invoice.getAmount()) {
                invoice.setStatus(InvoiceStatus.PAID);
            }
            invoiceRepository.save(invoice);
            log.info("Added payment to invoice successfully, invoiceId: {}, amount: {}", invoiceId, invoicePaymentRequest.getAmount());
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessingException("Error occurred while adding payment to invoice, invoiceId: " + invoiceId + ", amount: " + invoicePaymentRequest.getAmount(), e);
        }
    }

    @Override
    public void processOverdue(ProcessOverdueInvoiceRequestDto processOverdueInvoiceRequest) {
        try {
            log.info("Processing overdue invoices, lateFee: {}, overdueDays: {}", processOverdueInvoiceRequest.getLateFee(), processOverdueInvoiceRequest.getOverdueDays());
            LocalDate now = LocalDate.now();
            for (Invoice invoice : invoiceRepository.findAll()) {
                if (invoice.getStatus() == InvoiceStatus.PENDING && invoice.getDueDate().plusDays(processOverdueInvoiceRequest.getOverdueDays()).isBefore(now)) {
                    double remaining = invoice.getAmount() - invoice.getPaidAmount();
                    if (invoice.getPaidAmount() > 0) {
                        invoice.setStatus(InvoiceStatus.PAID);
                        log.info("Creating new invoice for overdue invoice partially paid, invoiceId: {}, pendingAmount:{}, lateFee:{}", invoice.getId(), invoice.getPaidAmount(), processOverdueInvoiceRequest.getLateFee());
                        CreateInvoiceRequestDto newInvoiceRequest = CreateInvoiceRequestDto.builder()
                                .amount(remaining + processOverdueInvoiceRequest.getLateFee())
                                .dueDate(now.plusDays(processOverdueInvoiceRequest.getOverdueDays()))
                                .build();
                        createInvoice(newInvoiceRequest);
                    } else {
                        invoice.setStatus(InvoiceStatus.VOID);
                        log.info("Creating new invoice for overdue invoice not paid, invoiceId: {}, pendingAmount:{}, lateFee:{}", invoice.getId(), invoice.getPaidAmount(), processOverdueInvoiceRequest.getLateFee());
                        CreateInvoiceRequestDto newInvoiceRequest = CreateInvoiceRequestDto.builder()
                                .amount(invoice.getAmount() + processOverdueInvoiceRequest.getLateFee())
                                .dueDate(now.plusDays(processOverdueInvoiceRequest.getOverdueDays()))
                                .build();
                        createInvoice(newInvoiceRequest);
                    }
                    invoiceRepository.save(invoice);
                    log.info("Updated overdue invoice, invoiceId: {}, status: {}", invoice.getId(), invoice.getStatus());
                }
            }
            log.info("Processed overdue invoices successfully, lateFee: {}, overdueDays: {}", processOverdueInvoiceRequest.getLateFee(), processOverdueInvoiceRequest.getOverdueDays());
        } catch (Exception e) {
            throw new ProcessingException("Error occurred while processing overdue invoices, lateFee: " + processOverdueInvoiceRequest.getLateFee() + ", overdueDays: " + processOverdueInvoiceRequest.getOverdueDays(), e);
        }
    }
}
