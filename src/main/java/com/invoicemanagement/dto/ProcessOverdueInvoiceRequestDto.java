package com.invoicemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Overdue invoice processing request")
public class ProcessOverdueInvoiceRequestDto {

    @Schema(description = "Late fee to apply", example = "10.5")
    @NotNull(message = "Late fee must not be null")
    private Double lateFee;

    @Schema(description = "Number of overdue days", example = "10")
    @NotNull(message = "Overdue days must not be null")
    private Integer overdueDays;

}
