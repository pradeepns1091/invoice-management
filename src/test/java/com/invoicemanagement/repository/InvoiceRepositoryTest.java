package com.invoicemanagement.repository;

import com.invoicemanagement.constants.InvoiceStatus;
import com.invoicemanagement.model.Invoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceRepositoryTest {

    private InvoiceRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InvoiceRepository();
    }

    private Invoice createSampleInvoice(String id, double amount) {
        return Invoice.builder()
                .id(id)
                .amount(amount)
                .paidAmount(0)
                .dueDate(LocalDate.of(2025, 8, 30))
                .status(InvoiceStatus.PENDING)
                .build();
    }

    @Test
    void save_success() {
        Invoice invoice = createSampleInvoice("INV001", 1000.00);

        Invoice saved = repository.save(invoice);

        assertNotNull(saved);
        assertEquals("INV001", saved.getId());
        assertEquals(1000.00, saved.getAmount());
    }

    @Test
    void findById_success() {
        Invoice invoice = createSampleInvoice("INV002", 1500.00);
        repository.save(invoice);

        Optional<Invoice> result = repository.findById("INV002");

        assertTrue(result.isPresent());
        assertEquals("INV002", result.get().getId());
    }

    @Test
    void findById_whenInvoiceNotFound_shouldReturnEmpty() {
        Optional<Invoice> result = repository.findById("NON_EXISTENT");
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_shouldReturnAllInvoices() {
        repository.save(createSampleInvoice("INV003", 2000.00));
        repository.save(createSampleInvoice("INV004", 3000.00));

        List<Invoice> allInvoices = repository.findAll();

        assertEquals(2, allInvoices.size());
        assertTrue(allInvoices.stream().anyMatch(inv -> inv.getId().equals("INV003")));
        assertTrue(allInvoices.stream().anyMatch(inv -> inv.getId().equals("INV004")));
    }

}