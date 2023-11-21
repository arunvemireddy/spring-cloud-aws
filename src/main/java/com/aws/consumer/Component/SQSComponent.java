package com.aws.consumer.Component;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.aws.consumer.DTO.AwsDTO;
import com.aws.consumer.DTO.OtherAttribute;
import com.aws.consumer.DTO.Widget;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Author arun vemireddy

@Component
public class SQSComponent {
	
	public final static Logger log = LogManager.getLogger(SQSComponent.class);

	public void getMessagesfromSQS(AmazonS3 s3, String bucketName3, String queueUrl) {
		log.info("queue URL = {}",queueUrl);
		AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		GetQueueAttributesRequest request = new GetQueueAttributesRequest().withQueueUrl(queueUrl)
				.withAttributeNames("ApproximateNumberOfMessages");
		log.info(request);

		try {
			GetQueueAttributesResult result = sqs.getQueueAttributes(request);
			log.info(result);
			String messageCount = result.getAttributes().get("ApproximateNumberOfMessages");
			for (int i = 0; i < Integer.parseInt(messageCount); i++) {
				receiveMessages(queueUrl, sqs, s3, bucketName3);
			}
		} catch (AmazonSQSException e) {
			log.error("Error getting SQS queue attributes: {}", e.getMessage());
		}

	}

	public void receiveMessages(String queueUrl, AmazonSQS sqs, AmazonS3 s3, String bucketName3) {

		try {
			List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
			for (Message message : messages) {
				String body = message.getBody();
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(body);
				processMessageSQS(jsonNode, sqs, queueUrl, message, s3, bucketName3);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void processMessageSQS(JsonNode jsonNode, AmazonSQS sqs, String queueUrl, Message message, AmazonS3 s3,
			String bucketName3) {
		S3Component s3Component = new S3Component();

		try {

			Widget widget = s3Component.setWidget(jsonNode);
			String objectKey3 = "widgets/" + widget.getId();
			String requestType = jsonNode.get("type").asText();
			log.info("Request type: {}", requestType);

			if ("create".equals(requestType)) {
				s3Component.putObjectInBucket(s3, bucketName3, objectKey3, widget.toString());
				s3Component.dynamoDBputObject(widget);
				deleteMessageFromQueue(queueUrl, message, sqs, requestType);
			}

			else if ("update".equals(requestType)) {
				s3Component.updateObjectInBucket(s3, bucketName3, objectKey3, widget.toString());
				deleteMessageFromQueue(queueUrl, message, sqs, requestType);
			}

			else if ("delete".equals(requestType)) {
				s3Component.deleteObjectFromBucket(s3, bucketName3, objectKey3);
				deleteMessageFromQueue(queueUrl, message, sqs, requestType);
			}

		} catch (Exception e) {
			log.error("An unexpected exception occurred: {}", e.getMessage());
		}
	}

	public void deleteMessageFromQueue(String queueUrl, Message message, AmazonSQS sqs, String requestType) {
		sqs.deleteMessage(queueUrl, message.getReceiptHandle());
		log.info("Message deleted from SQS, request type = {}", requestType);
	}

}
