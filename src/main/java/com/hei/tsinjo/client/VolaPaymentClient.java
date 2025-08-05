package com.hei.tsinjo.client;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import java.time.Instant;

@Component
@Slf4j
public class VolaPaymentClient {

    private final RestTemplate restTemplate;
    private final String volaApiUrl;
    private final String volaApiKey;

    public VolaPaymentClient(RestTemplate restTemplate,
                             @Value("${vola.api.url}") String volaApiUrl,
                             @Value("${vola.api.key}") String volaApiKey) {
        this.restTemplate = restTemplate;
        this.volaApiUrl = volaApiUrl;
        this.volaApiKey = volaApiKey;
    }

    public VolaPaymentResponse createPayment(String payerEmail, String pspPaymentId) {
        try {
            String url = String.format("%s/payment?apiKey=%s&payerEmail=%s&pspType=ORANGE_MONEY&pspPaymentId=%s",
                    volaApiUrl, volaApiKey, payerEmail, pspPaymentId);

            log.info("Creating payment verification for pspPaymentId: {}, payerEmail: {}",
                    pspPaymentId, payerEmail);

            VolaPaymentResponse response = restTemplate.postForObject(url, null, VolaPaymentResponse.class);
            log.info("Payment created with status: {}", response.getVerificationStatus());
            return response;

        } catch (RestClientException e) {
            log.error("Error creating payment verification for pspPaymentId: {}", pspPaymentId, e);
            throw new RuntimeException("Failed to create payment verification", e);
        }
    }

    public VolaPaymentResponse getPayment(String payerEmail, String pspPaymentId) {
        try {
            String url = String.format("%s/payment?apiKey=%s&payerEmail=%s&pspType=ORANGE_MONEY&pspPaymentId=%s",
                    volaApiUrl, volaApiKey, payerEmail, pspPaymentId);

            log.debug("Checking payment verification for pspPaymentId: {}", pspPaymentId);

            VolaPaymentResponse response = restTemplate.getForObject(url, VolaPaymentResponse.class);
            log.debug("Payment status: {} for pspPaymentId: {}",
                    response.getVerificationStatus(), pspPaymentId);
            return response;

        } catch (RestClientException e) {
            log.error("Error getting payment verification for pspPaymentId: {}", pspPaymentId, e);
            throw new RuntimeException("Failed to get payment verification", e);
        }
    }

    @Data
    public static class VolaPaymentResponse {
        private String id;
        private PspPayment pspPayment;
        private Instant creationInstant;
        private Instant lastPspVerificationInstant;
        private Integer verificationAttemptNb;
        private User payer;
        private Application application;
        private String verificationStatus;

        @Data
        public static class PspPayment {
            private String pspType;
            private String id;
            private Integer amount;
            private Instant creationInstant;
        }

        @Data
        public static class User {
            private String email;
        }

        @Data
        public static class Application {
            private String name;
            private String apiKey;
        }
    }
}
