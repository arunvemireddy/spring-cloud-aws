package com.aws.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.amazonaws.services.lookoutequipment.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.aws.consumer.Component.DynamoDBComponent;
import com.aws.consumer.Component.S3Component;
import com.aws.consumer.DTO.AwsDTO;
//import com.aws.consumer.Component.DynamoDBComponent;
//import com.aws.consumer.Component.S3Component;
//import com.aws.consumer.DTO.AwsDTO;

@SpringBootTest
class ConsumerApplicationTests {
	
	@MockBean
	private AmazonS3 mockAmazonS3;
	
	
    @Test
    void testProcessMethod() {
 
        AwsDTO awsDTO = new AwsDTO(); 
        ConsumerApplication consumerApplication = new ConsumerApplication();
        consumerApplication.awsDTO = awsDTO;
        consumerApplication.s3Component = new S3Component();
        consumerApplication.dbComponent = new DynamoDBComponent();

      
        Boolean bool = consumerApplication.process(mockAmazonS3,awsDTO.getBucketName2(),awsDTO.getBucketName3());
        assertEquals(true, bool);
    }
}
