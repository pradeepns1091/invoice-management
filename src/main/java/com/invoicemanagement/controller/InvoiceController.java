package com.invoicemanagement.controller;

import com.invoicemanagement.dto.CreateInvoiceRequestDto;
import com.invoicemanagement.dto.InvoicePaymentRequestDto;
import com.invoicemanagement.dto.InvoiceResponseDto;
import com.invoicemanagement.dto.ProcessOverdueInvoiceRequestDto;
import com.invoicemanagement.exception.dto.ApiError;
import com.invoicemanagement.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice", description = "Operations related to invoice and payments")
@Validated
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Operation(summary = "Create a new invoice", description = "Creates an invoice with amount and due date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Invoice created successfully", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                       "id": "INV12345"
                                    }
                                    """),
                    schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "status": "BAD_REQUEST",
                                      "message": "Validation failed",
                                      "errors": [
                                        "amount: Amount must not be null"
                                      ]
                                    }
                                    """),
                    schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "status": "INTERNAL_SERVER_ERROR",
                                      "message": "Error occurred while processing",
                                      "errors": [
                                        "Error occurred while processing"
                                      ]
                                    }
                                    """),
                    schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<Map<String, String>> createInvoice(@Valid @RequestBody CreateInvoiceRequestDto createInvoiceRequest) {
        InvoiceResponseDto invoice = invoiceService.createInvoice(createInvoiceRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", invoice.getId()));
    }

    @Operation(summary = "Get all invoices", description = "Retrieves a list of all invoices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of invoice returned"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "status": "INTERNAL_SERVER_ERROR",
                                      "message": "Error occurred while processing",
                                      "errors": [
                                        "Error occurred while processing"
                                      ]
                                    }
                                    """),
                    schema = @Schema(implementation = ApiError.class)))})
    @GetMapping
    public ResponseEntity<List<InvoiceResponseDto>> getInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @Operation(summary = "Add payment to an invoice", description = "Adds a payment to the specified invoice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "status": "BAD_REQUEST",
                                      "message": "Validation failed",
                                      "errors": [
                                        "amount: Amount must not be null"
                                      ]
                                    }
                                    """),
                    schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Invoice not found", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "status": "NOT_FOUND",
                                      "message": "Invoice not found with id",
                                      "errors": [
                                        "Resource not found"
                                      ]
                                    }
                                    """),
                    schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "status": "INTERNAL_SERVER_ERROR",
                                      "message": "Error occurred while processing",
                                      "errors": [
                                        "Error occurred while processing"
                                      ]
                                    }
                                    """),
                    schema = @Schema(implementation = ApiError.class)))})
    @PostMapping("/{id}/payments")
    public ResponseEntity<Void> payInvoice(@PathVariable(name = "id") @NotBlank(message = "ID must not be blank") String id,
                                           @Valid @RequestBody InvoicePaymentRequestDto invoicePaymentRequest) {
        invoiceService.addPayment(id, invoicePaymentRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Process overdue invoices", description = "Applies late fees to overdue invoices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue invoices processed"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "status": "BAD_REQUEST",
                                      "message": "Validation failed",
                                      "errors": [
                                        "lateFee: Late fee must not be null"
                                      ]
                                    }
                                    """),
                    schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            value = """
                                    {
                                      "status": "INTERNAL_SERVER_ERROR",
                                      "message": "Error occurred while processing",
                                      "errors": [
                                        "Error occurred while processing"
                                      ]
                                    }
                                    """),
                    schema = @Schema(implementation = ApiError.class)))})
    @PostMapping("/process-overdue")
    public ResponseEntity<Void> processOverdue(@Valid @RequestBody ProcessOverdueInvoiceRequestDto processOverdueInvoiceRequest) {
        invoiceService.processOverdue(processOverdueInvoiceRequest);
        return ResponseEntity.ok().build();
    }
}
