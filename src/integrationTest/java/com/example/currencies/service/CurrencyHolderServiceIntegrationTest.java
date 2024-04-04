package com.example.currencies.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.example.currencies.entity.Event;
import com.example.currencies.model.Currency;
import com.example.currencies.repository.EventRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles(value = "integration-test")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(
    includeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {CurrencyHolderService.class}))
class CurrencyHolderServiceIntegrationTest {

  @Container
  private static final PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:16.1-alpine")
          .withDatabaseName("tests-db")
          .withUsername("username")
          .withPassword("password")
          .withInitScript("initSchema.sql");

  static {
    postgreSQLContainer.start();
  }

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
    dynamicPropertyRegistry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    dynamicPropertyRegistry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    dynamicPropertyRegistry.add("spring.datasource.password", postgreSQLContainer::getPassword);
  }

  @MockBean private CurrencyRetrieveService currencyRetrieveService;

  @Autowired private CurrencyHolderService service;

  @Autowired private EventRepository eventRepository;

  @Test
  void whenUpdateCurrencyRatesCalledShouldUpdateRateAndSaveEvent() {
    Currency currency = new Currency("base1", List.of(new Currency.Rate("base2", 1D)));
    when(currencyRetrieveService.retrieveCurrencyRates()).thenReturn(new Currency[] {currency});

    service.updateCurrencyRates();

    List<Event> allEvents = eventRepository.findAll();
    assertNotNull(allEvents);
    assertEquals(1, allEvents.size());
    assertEquals("currency rates has been updated", allEvents.get(0).getEventLog());
  }
}
