package com.invoicemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Payment request for an invoice")
public class InvoicePaymentRequestDto {

    @Schema(description = "Amount to pay", example = "159.99")
    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;

}

