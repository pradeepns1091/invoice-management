package com.invoicemanagement.dto;

import com.invoicemanagement.constants.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Invoice details")
public class InvoiceResponseDto {

    @Schema(example = "1234")
    private String id;

    @Schema(example = "199.99")
    private double amount;

    @Schema(example = "0")
    private double paidAmount;

    @Schema(example = "2021-09-11")
    private LocalDate dueDate;

    @Schema(description = "Invoice status", allowableValues = {"pending", "paid", "void"})
    private InvoiceStatus status;

}
