package com.aws.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.s3.model.S3Object;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ConsumerApplication {
	
	public Boolean process(AmazonS3 s3) {
	
		String bucketName2 = "usu-cs5260-ironman-requests";
		String bucketName3 = "usu-cs5260-ironman-web";
		String requestType = "create";
		String programEnded = "consumer program ended";
		
		ObjectListing objectListing = s3.listObjects(bucketName2);
		boolean stopCondition = false;
		
		while(!stopCondition) {
			
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				String objectKey2 = objectSummary.getKey();
				S3Object s3Object = s3.getObject(bucketName2, objectKey2);
				ObjectMapper objectMapper = new ObjectMapper();
				
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))) {
					String json = reader.readLine();
					JsonNode jsonNode = objectMapper.readTree(json);

					Widget widget = new Widget();				
					widget.setId(jsonNode.get("widgetId").asText());
					widget.setOwner(jsonNode.get("owner").asText());
					widget.setDescription(jsonNode.get("description").asText());
					
					JsonNode otherAttributesNode = jsonNode.get("otherAttributes");
					List<OtherAttribute> otherAttributes = new ArrayList<>();
			        for (JsonNode attributeNode : otherAttributesNode) {
			            OtherAttribute otherAttribute = new OtherAttribute();
			            otherAttribute.setName(attributeNode.get("name").asText());
			            otherAttribute.setValue(attributeNode.get("value").asText());
			            otherAttributes.add(otherAttribute);
			        }
			        widget.setOtherAttributes(otherAttributes);
			        String objectKey3 = "widgets/" + widget.getId();
			        
			        if (requestType.equals(jsonNode.get("type").asText())) {
			        	s3.putObject(bucketName3, objectKey3, widget.toString());
			            System.out.println("Object uploaded to S3: " + objectKey3);
			            s3.deleteObject(bucketName2,objectKey2);
			            System.out.println(objectKey2+"is deleted from bucket 2");
	
			        }else {
//			        	deleting other requests(delete and update)
			        	s3.deleteObject(bucketName2,objectKey2);
			        	 System.out.println(objectKey2+"is deleted from bucket 2");
			        }
			        
				} catch (Exception e) {
//					some requests does not have type or object key is invalid so I am deleting those objects here
					s3.deleteObject(bucketName2,objectKey2);
				}
			}
			
			ObjectListing objectListing2 = s3.listObjects(bucketName2);
			if(objectListing2.getObjectSummaries().isEmpty()) {
				try {
					Thread.sleep(100);
					if(objectListing2.getObjectSummaries().isEmpty()) {
						stopCondition = true;
					}else {
						objectListing = objectListing2;
						stopCondition = false;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else {
				stopCondition = false;
			}
		}
		System.out.println(programEnded);
		return true;
	}

//	source https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-s3-objects.html
	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
		System.out.println("consumer application for AWS s3 bucket");
		final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build();
		
		ConsumerApplication consumerApplication = new ConsumerApplication();
		consumerApplication.process(s3);
		System.exit(0);
		
//		list of s3 buckets
//		List<Bucket> buckets = s3.listBuckets();
//		System.out.println("Your {S3} buckets are:");
//		for (Bucket b : buckets) {
//			System.out.println("bucket name" + b.getName());
//		}
//		System.out.println();
		
	}

}
