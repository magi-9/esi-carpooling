package com.esi.ridebooking.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class TestRestClientConfig {

    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        RestClient.Builder mockBuilder = mock(RestClient.Builder.class);
        RestClient mockClient = createMockRestClient();
        
        when(mockBuilder.build()).thenReturn(mockClient);
        
        return mockBuilder;
    }
    
    private RestClient createMockRestClient() {
        RestClient mockClient = mock(RestClient.class);
        
        // Create specs for GET requests
        RestClient.RequestHeadersUriSpec getUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec getResponseSpec = mock(RestClient.ResponseSpec.class);
        
        // Setup GET chain
        when(mockClient.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri(anyString())).thenReturn(getUriSpec);
        when(getUriSpec.header(anyString(), anyString())).thenReturn(getUriSpec);
        when(getUriSpec.retrieve()).thenReturn(getResponseSpec);
        when(getResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
        
        // Mock geocoding responses (returns coordinates)
        when(getResponseSpec.body(Map.class)).thenAnswer(invocation -> {
            String uri = (String) invocation.getMock();
            // Return different coordinates for start vs end based on address
            return Map.of(
                "latitude", 54.6872,
                "longitude", 25.2797
            );
        });
        
        // Create specs for POST requests (payment service)
        RestClient.RequestBodyUriSpec postBodySpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.ResponseSpec postResponseSpec = mock(RestClient.ResponseSpec.class);
        
        // Setup POST chain
        when(mockClient.post()).thenReturn(postBodySpec);
        when(postBodySpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.header(anyString(), anyString())).thenReturn(postBodySpec);
        when(postBodySpec.body(any())).thenReturn(postBodySpec);
        when(postBodySpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
        
        // Mock payment response (returns paymentId)
        when(postResponseSpec.body(Map.class)).thenAnswer(invocation -> {
            return Map.of("paymentId", UUID.randomUUID().toString());
        });
        
        return mockClient;
    }
}
