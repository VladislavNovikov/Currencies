package com.example.currencies.service;

import com.example.currencies.model.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CurrencyRetrieveService {
  private final RestTemplate restTemplate;
  private static final String API_PATH = "/currencies";

  public Currency[] retrieveCurrencyRates() {
    Currency[] externalCurrencies = restTemplate.getForObject(API_PATH, Currency[].class);
    if (externalCurrencies == null) {
      throw new RuntimeException("error: can not get external currency rates");
    }
    return externalCurrencies;
  }
}
