package com.aws.consumer.Component;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.aws.consumer.DTO.AwsDTO;
import com.aws.consumer.DTO.OtherAttribute;
import com.aws.consumer.DTO.Widget;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.s3.model.S3Object;

// HW3 InProgress
// Author arun vemireddy

public class SQSComponent {

	public final static Logger log = LogManager.getLogger(SQSComponent.class);

	public void getMessagesfromSQS(AmazonS3 s3) {
		AwsDTO awsDTO = new AwsDTO();
		String queueUrl = awsDTO.getQueueUrl();
		String bucketName3 = awsDTO.getBucketName3();
		AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

		GetQueueAttributesRequest request = new GetQueueAttributesRequest().withQueueUrl(queueUrl)
				.withAttributeNames("ApproximateNumberOfMessages");

		GetQueueAttributesResult result = sqs.getQueueAttributes(request);
		String messageCount = result.getAttributes().get("ApproximateNumberOfMessages");
		for (int i = 0; i < Integer.parseInt(messageCount); i++) {
			receiveMessages(queueUrl, sqs, s3, bucketName3);
		}
	}

	public void receiveMessages(String queueUrl, AmazonSQS sqs, AmazonS3 s3, String bucketName3) {

		try {
			List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
			for (Message message : messages) {
				String body = message.getBody();
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(body);
				checkObjectSQS(jsonNode, sqs, queueUrl, message, s3, bucketName3);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	void checkObjectSQS(JsonNode jsonNode, AmazonSQS sqs, String queueUrl, Message message, AmazonS3 s3,
			String bucketName3) {

		try {
			log.info("request type" + jsonNode.get("type").toString());
			String requestType = jsonNode.get("type").asText();
			String objectKey = null;

			if ("create".equals(requestType)) {

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

				objectKey = "widgets/" + widget.getId();
				AwsDTO awsDTO = new AwsDTO();
				s3.putObject(awsDTO.getBucketName3(), objectKey, widget.toString());

				S3Component s3Component = new S3Component();
				s3Component.dynamoDBputObject(widget);
				log.info(objectKey + "Object uploaded to bucket" + awsDTO.getBucketName3());

				sqs.deleteMessage(queueUrl, message.getReceiptHandle());
				log.info("Message deleted from SQS, request type = {}", requestType);
			}

			else if ("update".equals(requestType)) {
				S3Object s3Object = s3.getObject(bucketName3, objectKey);
				log.info(s3Object);
				sqs.deleteMessage(queueUrl, message.getReceiptHandle());
				log.info("Message deleted from SQS, request type = {}", requestType);
			}

			else if ("delete".equals(requestType)) {
				s3.deleteObject(bucketName3, objectKey);
				sqs.deleteMessage(queueUrl, message.getReceiptHandle());
				log.info("Message deleted from SQS, request type = {}", requestType);
			}

		} catch (Exception e) {
			log.error("An unexpected exception occurred: {}", e.getMessage());
		}
	}
}
