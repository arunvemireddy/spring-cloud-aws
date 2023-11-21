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
//	public static SQSComponent sqsComponent;

	public ConsumerApplication() {
		awsDTO = new AwsDTO();
		s3Component = new S3Component();
		dbComponent = new DynamoDBComponent();
		
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
					s3Component.processObject(jsonNode, awsDTO.getBucketName3(), awsDTO.getBucketName2(), objectKey2, s3);
				} catch (Exception e) {
					log.error("Unexpected error: {}", e.getMessage());
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
		AwsDTO awsDTO = new AwsDTO();
		
		log.info("consumer application for AWS");

	    String bucketName2 = null;
        String bucketName3 = null;
        String queueUrl = null;

        for (String arg : args) {
            String prefix = null;
            String value = null;

            if (arg.startsWith("--request-bucket=")) {
                prefix = "--request-bucket=";
            } else if (arg.startsWith("--widget-bucket=")) {
                prefix = "--widget-bucket=";
            } else if (arg.startsWith("--request-queue=")) {
                prefix = "--request-queue=";
            }

            if (prefix != null) {
                value = arg.substring(prefix.length());
            }

            if (value != null) {
                switch (prefix) {
                    case "--request-bucket=":
                        bucketName2 = value;
                        awsDTO.setBucketName2(bucketName2);
                        break;
                    case "--widget-bucket=":
                        bucketName3 = value;
                        awsDTO.setBucketName3(bucketName3);
                        break;
                    case "--request-queue=":
                        queueUrl = value;
                        awsDTO.setQueueUrl(queueUrl);
                        break;
                    default:
                        // Handle unsupported argument
                    	log.info("Missing Arguments");
                        break;
                }
            }
        }


        if (bucketName2 == null || bucketName3 == null || queueUrl == null) {
        	log.info("Missing arguments: Buckets and SQS queue URL are not provided.");
        }
        
        log.info("Bucket Name 2: {}", awsDTO.getBucketName2());
        log.info("Bucket Name 3: {}", awsDTO.getBucketName3());
        log.info("SQS Queue URL: {}", awsDTO.getQueueUrl());

        SQSComponent sqsComponent = new SQSComponent();
     // Check if queueUrl is provided, then retrieve messages from SQS
        if (queueUrl != null) {
//        	HW4
            sqsComponent.getMessagesfromSQS(s3,awsDTO.getBucketName3(),awsDTO.getQueueUrl());
        }

        // Check if both bucket names are provided, then process
        if (bucketName2 != null && bucketName3 != null) {
//        	HW2
            consumerApplication.process(s3, bucketName2, bucketName3);
        }
        
//      System.exit(0);
	}

}
