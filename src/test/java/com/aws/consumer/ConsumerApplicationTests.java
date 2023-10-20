package com.aws.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import com.amazonaws.services.lookoutequipment.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@SpringBootTest
class ConsumerApplicationTests {
	
//	private AmazonS3 s3;
//    private ConsumerApplication consumerApplication;
//    
//    @Before
//    public void setup() {
//        s3 = Mockito.mock(AmazonS3.class);
//        consumerApplication = new ConsumerApplication();
//    }
//    
//    @Test
//    public void testProcess() {
//    
//        S3Object s3Object = Mockito.mock(S3Object.class);
//        ObjectListing objectListing = Mockito.mock(ObjectListing.class);
//        List<S3ObjectSummary> objectSummaries = new ArrayList<>();
//       
//        Mockito.when(s3.listObjects("usu-cs5260-ironman-requests")).thenReturn(objectListing);
//        Mockito.when(objectListing.getObjectSummaries()).thenReturn(new ArrayList<S3ObjectSummary>());
//
// 
//        Boolean result = consumerApplication.process(s3);
//        assertEquals(true,result);
//        Mockito.verify(s3).listObjects("usu-cs5260-ironman-requests");
//    }
//
//
//
//	@Test
//	void contextLoads() {
//	}

}
