package com.example.currencies.controller;

import com.example.currencies.controller.api.CurrencyApi;
import com.example.currencies.model.Currency;
import com.example.currencies.service.CurrencyHolderService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class CurrencyController implements CurrencyApi {

  private final CurrencyHolderService currencyHolderService;

  @GetMapping(value = "/currencies", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<String> getAvailableCurrencies() {
    return currencyHolderService.getAllCurrencies();
  }

  @GetMapping(value = "/rates", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Map<String, Double>> getAvailableCurrenciesWithRates(
      @RequestParam(name = "base", required = false) String[] base) {
    return currencyHolderService.getRatesForBase(base);
  }

  @PostMapping("/rates")
  public void addNewCurrency(@RequestBody Currency newCurrency) {
    currencyHolderService.addCustomCurrency(newCurrency);
  }
}
