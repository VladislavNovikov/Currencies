package com.example.currencies.service;

import com.example.currencies.entity.Event;
import com.example.currencies.model.Currency;
import com.example.currencies.repository.EventRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyHolderService {
  private final EventRepository eventRepository;
  private final Map<String, Map<String, Double>> currencyRates = new HashMap<>();

  private final CurrencyRetrieveService currencyRetrieveService;

  /**
   * will update only existing currencies taken from the external api will not affect custom
   * currencies
   */
  @Scheduled(fixedRateString = "${scheduler.currency.fixedRate}")
  public void updateCurrencyRates() {
    Currency[] newCurrencies = currencyRetrieveService.retrieveCurrencyRates();
    if (newCurrencies == null || newCurrencies.length == 0) {
      throw new RuntimeException("currency rates can not be null or empty");
    }
    Arrays.stream(newCurrencies)
        .forEach(
            currency -> {
              currencyRates
                  .computeIfAbsent(currency.getBase(), k -> new HashMap<>())
                  .putAll(currency.getRatesAsMap());
            });
    eventRepository.save(new Event("currency rates has been updated"));
  }

  public void addCustomCurrency(Currency newCurrency) {
    // newCurrency == null, newCurrencyName == null, rates ...  model validation is skipped
    String newCurrencyName = newCurrency.getBase();
    validateNewCurrencyDoesNotExist(newCurrencyName);
    Map<String, Double> newRatesAsMap = newCurrency.getRatesAsMap();
    validateAllCurrenciesExist(newRatesAsMap);

    currencyRates.put(newCurrencyName, newRatesAsMap);
    newRatesAsMap.forEach(
        (currencyName, currencyRate) ->
            currencyRates.get(currencyName).put(newCurrencyName, 1 / currencyRate));
  }

  private void validateAllCurrenciesExist(Map<String, Double> newRatesAsMap) {
    newRatesAsMap
        .keySet()
        .forEach(
            newCurrencyRate -> {
              if (!currencyRates.containsKey(newCurrencyRate)) {
                throw new RuntimeException(
                    "new currency can not depend on currency which does not exist");
              }
            });
  }

  private void validateNewCurrencyDoesNotExist(String newCurrencyName) {
    if (currencyRates.containsKey(newCurrencyName)) {
      throw new RuntimeException("new currency '%s' already exists".formatted(newCurrencyName));
    }
  }

  public List<String> getAllCurrencies() {
    return new ArrayList<>(currencyRates.keySet());
  }

  public Map<String, Map<String, Double>> getRatesForBase(String... bases) {
    if (bases == null || bases.length == 0) {
      return getAllCurrenciesWithRates();
    }
    Map<String, Map<String, Double>> currencyRatesForBases = new HashMap<>();
    for (String currency : bases) {
      if (currencyRates.containsKey(currency)) {
        currencyRatesForBases.put(currency, new HashMap<>(currencyRates.get(currency)));
      }
    }
    return currencyRatesForBases;
  }

  private Map<String, Map<String, Double>> getAllCurrenciesWithRates() {
    Map<String, Map<String, Double>> allCurrencyRates = new HashMap<>();
    currencyRates.forEach(
        (currency, v) ->
            allCurrencyRates.put(currency, new HashMap<>(currencyRates.get(currency))));
    return allCurrencyRates;
  }
}
