package com.example.currencies.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.currencies.service.CurrencyHolderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CurrencyController.class)
@ActiveProfiles(value = "integration-test")
class CurrencyControllerIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean CurrencyHolderService currencyHolderService;

  private static final String PATH = "/api/v1/currencies";

  @Test
  void whenGetCurrenciesShouldReturnListOfAllCurrencies() throws Exception {
    List<String> expectedAnswer = List.of("curr1", "curr2", "curr3");

    when(currencyHolderService.getAllCurrencies()).thenReturn(expectedAnswer);

    mockMvc
        .perform(get(PATH))
        .andExpect(content().json(objectMapper.writeValueAsString(expectedAnswer)))
        .andExpect(status().isOk());

    verify(currencyHolderService).getAllCurrencies();
  }

  @Test
  void whenGetCurrenciesAndErrorHappensShouldReturnError() throws Exception {
    doThrow(RuntimeException.class).when(currencyHolderService).getAllCurrencies();

    mockMvc.perform(get(PATH)).andExpect(status().is5xxServerError());

    verify(currencyHolderService).getAllCurrencies();
  }
}
