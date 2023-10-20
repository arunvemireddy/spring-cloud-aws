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


//source https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-s3-objects.html
@Component
public class S3Component {
	
	public static AwsDTO awsDTO;
	public static DynamoDBComponent dbComponent;

	public S3Component() {
		awsDTO = new AwsDTO();
		dbComponent = new DynamoDBComponent();
	}

	public final static Logger log = LogManager.getLogger(ConsumerApplication.class);
	
//	putMethod
	public void putObject(JsonNode jsonNode, String bucketName3, String bucketName2, String objectKey2,
			String requestType, AmazonS3 s3) {
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
			dynamoDBputObject(widget);
			log.info(objectKey3 + "Object uploaded to bucket" + bucketName3);
			s3.deleteObject(bucketName2, objectKey2);
			log.info(objectKey2 + "is deleted from bucket" + bucketName2);

		} else {
//        	deleting other requests(delete and update)
			deleteObject(s3, bucketName2, objectKey2);
			updateObject(s3, bucketName2, objectKey2);
			log.info(objectKey2 + "is deleted from bucket" + bucketName2);
		}
	}

//	deleteObject
	public void deleteObject(AmazonS3 s3, String bucketName, String objectKey) {
		try {
			s3.deleteObject(bucketName, objectKey);
		} catch (Exception e) {
			log.info("object is not deleted" + objectKey);
		}
	}

//	updateObject
	public void updateObject(AmazonS3 s3, String bucketName, String objectKey) {
		try {
			s3.deleteObject(bucketName, objectKey);
		} catch (Exception e) {
			log.info("object is not deleted" + objectKey);
		}
	}
// dynamoDB
	public void dynamoDBputObject(Widget widget) {
		dbComponent.putItem("widgets",widget.getId(),widget.getOwner(), widget.getDescription(), widget.getOtherAttributes());
	}
}
