package com.example.currencies.controller.api;

import com.example.currencies.model.Currency;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;

@Tag(name = "Currency", description = "the currency api")
public interface CurrencyApi {

  @Operation(
      summary = "Fetch available currencies",
      description = "returns an array of names of all available currencies")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Success",
            content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  examples = {
                    @ExampleObject(
                        name = "available currencies",
                        value = "[\"USD\", \"GBP\", \"EUR\", \"PLN\"]")
                  })
            }),
        @ApiResponse(
            responseCode = "500",
            description = "Some internal server error happened",
            content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  examples = {
                    @ExampleObject(name = "error", value = "{ \"error:\": \"internal error\" }")
                  })
            })
      })
  List<String> getAvailableCurrencies();

  @Operation(summary = "Fetch rates for specified currencies")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                """
                  returns an object, where key is currency name and value exchange rates.
                  returns {} if none of the provided currencies are represented in the system
                  """,
            content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = Map.class),
                  examples = {
                    @ExampleObject(
                        name = "exchange rates for USD",
                        value =
                            """
                                                  {
                                                    "USD": {
                                                      "GBP": 3.4,
                                                      "EUR": 4.4,
                                                      "PLN": 2.5
                                                    }
                                                  }
                                                  """)
                  })
            }),
        @ApiResponse(
            responseCode = "500",
            description = "Some internal server error happened",
            content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  examples = {
                    @ExampleObject(name = "error", value = "{ \"error:\": \"internal error\" }")
                  })
            })
      })
  Map<String, Map<String, Double>> getAvailableCurrenciesWithRates(String[] base);

  @Operation(
      summary = "Create new custom currency",
      description = "allows user to create a new custom currency")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Currency created and all related exchange rates updated"),
        @ApiResponse(
            responseCode = "500",
            description =
                "Some internal error, happens if"
                    + "user tries to add new currency which already exists or "
                    + "any currency from rates depends on currency which does not exist",
            content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  examples = {
                    @ExampleObject(name = "error", value = "{ \"error:\": \"internal error\" }")
                  })
            })
      })
  public void addNewCurrency(
      @RequestBody(
              description =
                  "will be created new currency with name 'newCurrency', plus corresponding "
                      + "rates for USD and EUR updated",
              content =
                  @Content(
                      examples = {
                        @ExampleObject(
                            name = "New currency sample",
                            value =
                                """
                                          {
                                             "base": "newCurrency",
                                            "rates": [
                                              {
                                                "USD": 1.24
                                              },
                                              {
                                                "EUR": 1
                                              }
                                            ]
                                          }
                                          """)
                      }))
          Currency newCurrency);
}
