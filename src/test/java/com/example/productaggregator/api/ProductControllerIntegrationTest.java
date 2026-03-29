package com.example.productaggregator.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.productaggregator.domain.AggregatedProductResponse;
import com.example.productaggregator.domain.ProductView;
import com.example.productaggregator.domain.ResponseContext;
import com.example.productaggregator.exception.CatalogUnavailableException;
import com.example.productaggregator.exception.InvalidMarketException;
import com.example.productaggregator.service.ProductAggregationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductAggregationService productAggregationService;

    @Test
    void shouldReturnAggregatedResponse() throws Exception {
        AggregatedProductResponse response = new AggregatedProductResponse(
                new ProductView("P-100", null, null, null, null),
                new ResponseContext("pl-PL", "pl"),
                List.of()
        );

        when(productAggregationService.getProduct("P-100", "pl-PL", "DEALER-1"))
                .thenReturn(response);

        mockMvc.perform(get("/v1/products/P-100")
                        .queryParam("market", "pl-PL")
                        .queryParam("customerId", "DEALER-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.productId").value("P-100"))
                .andExpect(jsonPath("$.context.market").value("pl-PL"));
    }

    @Test
    void shouldReturnBadRequestWhenMarketHasInvalidFormat() throws Exception {
        mockMvc.perform(get("/v1/products/P-100")
                        .queryParam("market", "pl_PL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void shouldReturnBadRequestWhenMarketIsUnsupported() throws Exception {
        when(productAggregationService.getProduct("P-100", "fr-FR", null))
                .thenThrow(new InvalidMarketException("Unsupported market: fr-FR"));

        mockMvc.perform(get("/v1/products/P-100")
                        .queryParam("market", "fr-FR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Unsupported market: fr-FR"));
    }

    @Test
    void shouldReturnBadGatewayWhenCatalogServiceFails() throws Exception {
        when(productAggregationService.getProduct("P-100", "pl-PL", null))
                .thenThrow(new CatalogUnavailableException("Catalog service is required but unavailable", null));

        mockMvc.perform(get("/v1/products/P-100")
                        .queryParam("market", "pl-PL"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error").value("Bad Gateway"))
                .andExpect(jsonPath("$.message").value("Catalog service is required but unavailable"));
    }
}
