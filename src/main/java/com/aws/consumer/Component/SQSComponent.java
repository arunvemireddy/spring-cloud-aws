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

// HW3 InProgress
// Author Arun

public class SQSComponent {

	public final static Logger log = LogManager.getLogger(SQSComponent.class);

	public void getMessagesfromSQS(String queueUrl,AmazonS3 s3, String bucketName3) {
		AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		GetQueueAttributesRequest request = new GetQueueAttributesRequest().withQueueUrl(queueUrl)
				.withAttributeNames("ApproximateNumberOfMessages");

		GetQueueAttributesResult result = sqs.getQueueAttributes(request);
		String messageCount = result.getAttributes().get("ApproximateNumberOfMessages");
		for (int i = 0; i < Integer.parseInt(messageCount); i++) {
			receiveMessages(queueUrl, sqs,s3,bucketName3);
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

	void checkObjectSQS(JsonNode jsonNode, AmazonSQS sqs, String queueUrl, Message message, AmazonS3 s3, String bucketName3) {

		try {
			log.info("request type" + jsonNode.get("type").toString());

			if ("create".equals(jsonNode.get("type").asText())) {
				log.info("created");
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
				AwsDTO awsDTO = new AwsDTO();
				s3.putObject(awsDTO.getBucketName3(), objectKey3, widget.toString());
				
				S3Component s3Component = new S3Component();
				s3Component.dynamoDBputObject(widget);
				log.info(objectKey3 + "Object uploaded to bucket" + awsDTO.getBucketName3());
				
				sqs.deleteMessage(queueUrl, message.getReceiptHandle());
				log.info("message deleted from sqs, request type = created");
			}

			if ("update".equals(jsonNode.get("type").asText())) {
				sqs.deleteMessage(queueUrl, message.getReceiptHandle());
				log.info("message deleted from sqs, request type = updated");
			}

			if ("delete".equals(jsonNode.get("type").asText())) {
				sqs.deleteMessage(queueUrl, message.getReceiptHandle());
				log.info("message deleted from sqs, request type = deleted");
			}
			
		} catch (Exception e) {
			log.error(e);
		}
	}
}
