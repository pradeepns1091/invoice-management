package com.invoicemanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@Schema(description = "Invoice creation request")
public class CreateInvoiceRequestDto {

    @Schema(description = "Invoice amount", example = "199.99")
    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;

    @Schema(description = "Due date in yyyy-MM-dd format", example = "2021-09-11")
    @NotNull(message = "Due date must not be null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

}
