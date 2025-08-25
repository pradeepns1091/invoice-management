package com.invoicemanagement.repository;

import com.invoicemanagement.model.Invoice;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InvoiceRepository {
    private final Map<String, Invoice> invoices = new ConcurrentHashMap<>();

    public Invoice save(Invoice invoice) {
        invoices.put(invoice.getId(), invoice);
        return invoice;
    }

    public List<Invoice> findAll() {
        return new ArrayList<>(invoices.values());
    }

    public Optional<Invoice> findById(String id) {
        return Optional.ofNullable(invoices.get(id));
    }
}