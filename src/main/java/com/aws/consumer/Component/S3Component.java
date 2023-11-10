package com.aws.consumer.Component;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.aws.consumer.ConsumerApplication;
import com.aws.consumer.DTO.AwsDTO;
import com.aws.consumer.DTO.OtherAttribute;
import com.aws.consumer.DTO.Widget;
import com.fasterxml.jackson.databind.JsonNode;

// author arun vemireddy

//source https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-s3-objects.html
@Component
public class S3Component {

	public static DynamoDBComponent dbComponent;

	public S3Component() {
		dbComponent = new DynamoDBComponent();
	}

	public final static Logger log = LogManager.getLogger(ConsumerApplication.class);
	
	public Widget setWidget(JsonNode jsonNode) {
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
		return widget;
	}


	public void processObject(JsonNode jsonNode, String bucketName3, String bucketName2, String objectKey2,
			AmazonS3 s3) {
		
		Widget widget = setWidget(jsonNode);
		String objectKey3 = "widgets/" + widget.getId();
		String requestType = jsonNode.get("type").asText();
		log.info("Request type: {}", requestType);

		if ("create".equals(requestType)) {
			putObjectInBucket(s3, bucketName2, objectKey2, widget.toString());
			dynamoDBputObject(widget);
			deleteObjectFromBucket(s3, bucketName2, objectKey2);

		} else if ("update".equals(requestType)) {
			updateObjectInBucket(s3, bucketName3, objectKey3, widget.toString());
			deleteObjectFromBucket(s3, bucketName2, objectKey2);

		} else if ("delete".equals(requestType)) {
			deleteObjectFromBucket(s3, bucketName2, objectKey2);
			deleteObjectFromBucket(s3, bucketName3, objectKey3);
		}
	}

	public void putObjectInBucket(AmazonS3 s3, String bucketName, String objectKey, String widget) {
		try {
			s3.putObject(bucketName, objectKey, widget);
			log.info("{} Object uploaded to bucket {}", objectKey, bucketName);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void updateObjectInBucket(AmazonS3 s3, String bucketName, String objectKey, String widgetString) {
		deleteObjectFromBucket(s3, bucketName, objectKey);
		try {
			s3.putObject(bucketName, objectKey, widgetString);
		} catch (Exception e) {
			log.error("Error updating object '{}' in bucket '{}': {}", objectKey, bucketName, e.getMessage());
		}
	}

	public void deleteObjectFromBucket(AmazonS3 s3, String bucketName, String objectKey) {
		try {
			if (s3.doesObjectExist(bucketName, objectKey)) {
				s3.deleteObject(bucketName, objectKey);
				log.info("Object '{}' deleted from bucket '{}'", objectKey, bucketName);
			} else {
				log.info("Object '{}' does not exist in bucket '{}'", objectKey, bucketName);
			}

		} catch (Exception e) {
			log.error("Error deleting object '{}' from bucket '{}': {}", objectKey, bucketName, e.getMessage());
		}
	}

	public void dynamoDBputObject(Widget widget) {
		dbComponent.putItem("widgets", widget.getId(), widget.getOwner(), widget.getDescription(),
				widget.getOtherAttributes());
	}
}