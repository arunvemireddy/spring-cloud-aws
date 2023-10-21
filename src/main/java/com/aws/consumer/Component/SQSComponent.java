package com.aws.consumer.Component;

import java.util.List;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.ListQueuesResult;

// HW3 InProgress
// Author Arun

public class SQSComponent {

	public void listQueues() {
		AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		ListQueuesResult lq_result = sqs.listQueues();
		System.out.println("Your SQS Queue URLs:");
		for (String url : lq_result.getQueueUrls()) {
		    System.out.println(url);
		}
	}
	
	public void receiveMessages() {
		AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		List<Message> messages = sqs.receiveMessage("https://sqs.us-east-1.amazonaws.com/725671772159/cs5260-requests").getMessages();
		for (Message message : messages) {
			System.out.println(message);
		}
	}
}
