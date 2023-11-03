package com.aws.consumer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.aws.consumer.Component.DynamoDBComponent;
import com.aws.consumer.Component.S3Component;
import com.aws.consumer.Component.SQSComponent;
import com.aws.consumer.DTO.AwsDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//author arun vemireddy

@SpringBootApplication(scanBasePackages = { "com.aws.consumer" })
public class ConsumerApplication {

	public static AwsDTO awsDTO;
	public static S3Component s3Component;
	public static DynamoDBComponent dbComponent;
	public static SQSComponent sqsComponent;

	public ConsumerApplication() {
		awsDTO = new AwsDTO();
		s3Component = new S3Component();
		dbComponent = new DynamoDBComponent();
		sqsComponent = new SQSComponent();
	}

	public final static Logger log = LogManager.getLogger(ConsumerApplication.class);

	public Boolean process(AmazonS3 s3, String bucket2, String bucket3) {
		boolean stopCondition = false;
		if(bucket2 !=null && bucket3!=null) {
			awsDTO.setBucketName2(bucket2);
			awsDTO.setBucketName3(bucket3);
		}
		ObjectListing objectListing = s3.listObjects(awsDTO.getBucketName2());

		while (!stopCondition) {

			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				String objectKey2 = objectSummary.getKey();
				S3Object s3Object = s3.getObject(awsDTO.getBucketName2(), objectKey2);
				ObjectMapper objectMapper = new ObjectMapper();

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))) {
					String json = reader.readLine();
					JsonNode jsonNode = objectMapper.readTree(json);
					s3Component.putObject(jsonNode, awsDTO.getBucketName3(), awsDTO.getBucketName2(), objectKey2,
							awsDTO.getRequestType(), s3);

				} catch (Exception e) {
//					some requests does not have type or object key is invalid so I am deleting those objects here
					s3.deleteObject(awsDTO.getBucketName2(), objectKey2);
				}
			}

			ObjectListing objectListing2 = s3.listObjects(awsDTO.getBucketName2());
			if (objectListing2.getObjectSummaries().isEmpty()) {
				try {
					Thread.sleep(100);
					if (objectListing2.getObjectSummaries().isEmpty()) {
						stopCondition = true;
					} else {
						objectListing = objectListing2;
						stopCondition = false;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				stopCondition = false;
			}
		}
		log.info(awsDTO.getProgramEnded());
		return true;
	}
	
//	source https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-s3-objects.html
	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
		ConsumerApplication consumerApplication = new ConsumerApplication();
		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build();
		log.info("consumer application for AWS s3 bucket");
		
	    String bucketName2 = null;
        String bucketName3 = null;
        String queueUrl = "https://sqs.us-east-1.amazonaws.com/725671772159/cs5260-requests";

        for (String arg : args) {
            if (arg.startsWith("--request-bucket=")) {
                bucketName2 = arg.substring("--request-bucket=".length());
            } 
            if (arg.startsWith("--widget-bucket=")) {
                bucketName3 = arg.substring("--widget-bucket=".length());
            }
            if(arg.startsWith("--request-queue=")) {
            	queueUrl = arg.substring("--request-queue=".length());
            }
        }

        if (bucketName2 == null || bucketName3 == null || queueUrl == null) {
           log.info("Missing arguments, buckets are missing, sqs queue is missing");
        }
        
        
        log.info("bucket name 2"+bucketName2);
        log.info("bucket name 3"+bucketName3);
        log.info("sqs queueUrl"+queueUrl);
        
        if(queueUrl != null) {
        	sqsComponent.getMessagesfromSQS(queueUrl,s3,bucketName3);
        }
        
        if(bucketName2 != null && bucketName3 != null) {
        	consumerApplication.process(s3,bucketName2,bucketName3);
        }

//		consumerApplication.process(s3,bucketName2,bucketName3);
        
      System.exit(0);
	}

}
