package com.aws.consumer.Component;

import org.springframework.stereotype.Component;
import com.amazonaws.services.athena.AmazonAthena;
import com.amazonaws.services.athena.AmazonAthenaClient;
import com.amazonaws.services.athena.AmazonAthenaClientBuilder;

@Component
public class AthenaClientFactory {
	private final AmazonAthenaClientBuilder builder = AmazonAthenaClient.builder()
            .withRegion("us-east-1");

    public AmazonAthena createClient() {
        return builder.build();
    }
}
