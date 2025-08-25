package com.invoicemanagement;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest
class InvoiceManagementApplicationTest {

    @Test
    void main_shouldRunsWithoutException() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(InvoiceManagementApplication.class, new String[]{})).thenReturn(Mockito.mock(ConfigurableApplicationContext.class));
            InvoiceManagementApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(InvoiceManagementApplication.class, new String[]{}));
        }
    }

}
