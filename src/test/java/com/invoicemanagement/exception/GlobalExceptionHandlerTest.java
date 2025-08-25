package com.invoicemanagement.exception;

import com.invoicemanagement.exception.dto.ApiError;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFoundException() {
        NotFoundException ex = new NotFoundException("Invoice not found");
        ResponseEntity<Object> response = handler.handleNotFoundExceptions(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiError error = (ApiError) response.getBody();
        assertNotNull(error);
        assertEquals("Invoice not found", error.getMessage());
        assertEquals("Resource not found", error.getErrors().get(0));
    }

    @Test
    void testHandleProcessingException() {
        ProcessingException ex = new ProcessingException("Failed to process", new RuntimeException("Cause"));
        ResponseEntity<Object> response = handler.handleProcessingExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiError error = (ApiError) response.getBody();
        assertNotNull(error);
        assertEquals("Failed to process", error.getMessage());
        assertTrue(error.getErrors().toString().contains("java.lang.RuntimeException"));
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<Object> response = handler.handleAllExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiError error = (ApiError) response.getBody();
        assertNotNull(error);
        assertEquals("Unexpected error", error.getMessage());
        assertEquals("Unexpected error occurred", error.getErrors().get(0));
    }

    @Test
    void testHandleHttpMessageNotReadable() {
        HttpInputMessage mockInputMessage = Mockito.mock(HttpInputMessage.class);

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON", mockInputMessage);
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiError error = (ApiError) response.getBody();
        assertNotNull(error);
        assertEquals("Validation failed", error.getMessage());
        assertEquals("Invalid date format. Please use yyyy-MM-dd.", error.getErrors().get(0));
    }

    @Test
    void testHandleMethodArgumentNotValid() {
        // Simulate a validation error on field "dueDate"
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "invoice");
        bindingResult.addError(new FieldError("invoice", "dueDate", "must not be null"));
        bindingResult.addError(new FieldError("invoice", "amount", "must be greater than zero"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

        var response = handler.handleMethodArgumentNotValid(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ApiError apiError = (ApiError) response.getBody();
        assertNotNull(apiError);
        assertEquals("Validation failed", apiError.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, apiError.getStatus());

        List<String> details = apiError.getErrors();
        assertTrue(details.contains("dueDate: must not be null"));
        assertTrue(details.contains("amount: must be greater than zero"));

    }
}