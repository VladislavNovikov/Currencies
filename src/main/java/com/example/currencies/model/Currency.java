package com.example.currencies.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * It is better to use different models for the external and internal api, but it is not so
 * important. It implemented this way just for simplicity
 */
@Data
@AllArgsConstructor
public class Currency {
  @JsonDeserialize(using = RateDeserializer.class)
  public record Rate(String key, Double value) {}

  private String base;
  private List<Rate> rates;

  @Schema(hidden = true)
  public Map<String, Double> getRatesAsMap() {
    return getRates().stream().collect(Collectors.toMap(Currency.Rate::key, Currency.Rate::value));
  }

  public static class RateDeserializer extends JsonDeserializer<Rate> {
    @Override
    public Rate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException {
      Map<String, Double> map = jsonParser.readValueAs(new TypeReference<Map<String, Double>>() {});
      String currency = map.keySet().iterator().next();
      Double rate = map.get(currency);
      return new Rate(currency, rate);
    }
  }
}
