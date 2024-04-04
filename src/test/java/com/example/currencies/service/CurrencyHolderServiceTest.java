package com.example.currencies.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.currencies.entity.Event;
import com.example.currencies.model.Currency;
import com.example.currencies.repository.EventRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

@ExtendWith(MockitoExtension.class)
class CurrencyHolderServiceTest {

  public static final String CURRENCY_RATES_CAN_NOT_BE_NULL_OR_EMPTY =
      "currency rates can not be null or empty";
  public static final String CURRENCY_RATES_HAS_BEEN_UPDATED = "currency rates has been updated";
  public static final String RATES_JSON = "{\"EUR\":{\"USD\":0.5},\"USD\":{\"EUR\":2.0}}";

  public ObjectMapper mapper = new ObjectMapper();

  @Mock CurrencyRetrieveService currencyRetrieveService;
  @Mock EventRepository eventRepository;

  @Captor ArgumentCaptor<Event> eventCaptor;

  @InjectMocks CurrencyHolderService currencyHolderService;

  @SneakyThrows
  private Field getRatesField() {
    Field field = CurrencyHolderService.class.getDeclaredField("currencyRates");
    field.setAccessible(true);
    return field;
  }

  @SneakyThrows
  private Map<String, Map<String, Double>> getRatesMap() {
    return mapper.readValue(RATES_JSON, new TypeReference<>() {});
  }

  @SuppressWarnings("unchecked")
  private Map<String, Map<String, Double>> getCurrentRatesFieldValue(
      CurrencyHolderService currencyHolderService) {
    return (Map<String, Map<String, Double>>)
        ReflectionUtils.getField(getRatesField(), currencyHolderService);
  }

  @Test
  void whenRetrieverReturnsNullShouldThrowRuntimeException() {
    var runtimeException =
        assertThrows(RuntimeException.class, () -> currencyHolderService.updateCurrencyRates());
    assertEquals(runtimeException.getMessage(), "currency rates can not be null or empty");
  }

  @Test
  void whenRetrieverReturnsEmptyArrayShouldThrowRuntimeException() {
    when(currencyRetrieveService.retrieveCurrencyRates()).thenReturn(new Currency[] {});

    var runtimeException =
        assertThrows(RuntimeException.class, () -> currencyHolderService.updateCurrencyRates());
    assertEquals(runtimeException.getMessage(), CURRENCY_RATES_CAN_NOT_BE_NULL_OR_EMPTY);
  }

  @Test
  @SneakyThrows
  void shouldUpdateCurrencyRates() {
    Currency currency1 = new Currency("USD", List.of(new Currency.Rate("EUR", 2D)));
    Currency currency2 = new Currency("EUR", List.of(new Currency.Rate("USD", 0.5D)));
    Currency[] currencies = {currency1, currency2};
    when(currencyRetrieveService.retrieveCurrencyRates()).thenReturn(currencies);

    currencyHolderService.updateCurrencyRates();

    String ratesAsJson =
        mapper.writeValueAsString(getCurrentRatesFieldValue(currencyHolderService));

    assertEquals(RATES_JSON, ratesAsJson);
    verify(eventRepository).save(eventCaptor.capture());
    assertEquals(eventCaptor.getValue().getEventLog(), CURRENCY_RATES_HAS_BEEN_UPDATED);
  }

  @Test
  void whenCustomCurrencyExistsShouldThrowRuntimeError() {
    initRates();

    var runtimeException =
        assertThrows(
            RuntimeException.class,
            () -> currencyHolderService.addCustomCurrency(new Currency("USD", null)));
    assertEquals("new currency 'USD' already exists", runtimeException.getMessage());
  }

  @Test
  void whenRateInCustomCurrencyDoesNotExistShouldThrowRuntimeError() {
    initRates();

    var runtimeException =
        assertThrows(
            RuntimeException.class,
            () -> {
              Currency newCurrency = new Currency("PLN", List.of(new Currency.Rate("GBP", 1D)));
              currencyHolderService.addCustomCurrency(newCurrency);
            });
    assertEquals(
        "new currency can not depend on currency which does not exist",
        runtimeException.getMessage());
  }

  @Test
  @SneakyThrows
  void shouldAddCustomCurrency() {

    initRates();

    Currency newCurrency = new Currency("PLN", List.of(new Currency.Rate("USD", 3D)));
    currencyHolderService.addCustomCurrency(newCurrency);

    var currentRatesFieldValue = getCurrentRatesFieldValue(currencyHolderService);
    assertEquals(
        "{\"EUR\":{\"USD\":0.5},\"USD\":{\"EUR\":2.0,\"PLN\":0.3333333333333333},\"PLN\":{\"USD\":3.0}}",
        mapper.writeValueAsString(currentRatesFieldValue));
  }

  @Test
  void shouldReturnAllCurrenciesNames() {
    initRates();
    assertEquals(List.of("EUR", "USD"), currencyHolderService.getAllCurrencies());
  }

  @Test
  @SneakyThrows
  void whenBaseIsNullShouldReturnAllRates() {
    initRates();
    assertEquals(RATES_JSON, mapper.writeValueAsString(currencyHolderService.getRatesForBase((String[]) null)));
  }

  @Test
  @SneakyThrows
  void whenBaseIsEmptyShouldReturnAllRates() {
    initRates();
    assertEquals(RATES_JSON, mapper.writeValueAsString(currencyHolderService.getRatesForBase(new String[]{})));
  }

  @Test
  @SneakyThrows
  void whenRateDoesNotExistShouldReturnEmptyObject() {
    initRates();
    assertTrue(currencyHolderService.getRatesForBase("UDS").isEmpty());
  }

  @Test
  @SneakyThrows
  void shouldReturnAllRateForUSD() {
    initRates();
    assertEquals("{\"USD\":{\"EUR\":2.0}}", mapper.writeValueAsString(currencyHolderService.getRatesForBase("USD")));
  }

  private void initRates() {
    ReflectionUtils.setField(getRatesField(), currencyHolderService, getRatesMap());
  }
}
