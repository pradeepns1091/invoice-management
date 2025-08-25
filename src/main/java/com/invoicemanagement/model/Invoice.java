package com.invoicemanagement.model;

import com.invoicemanagement.constants.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class Invoice {
    private String id;
    private double amount;
    private double paidAmount;
    private LocalDate dueDate;
    private InvoiceStatus status;
}
