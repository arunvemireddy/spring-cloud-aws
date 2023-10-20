package com.aws.consumer.Component;

import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.aws.consumer.DTO.OtherAttribute;

//src https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-dynamodb-tables.html

@Component
public class DynamoDBComponent {
	final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.standard().build();
	public final static Logger log = LogManager.getLogger(DynamoDBComponent.class);

	public void describeTable(String tableName) {

		try {
			TableDescription table_info = ddb.describeTable(tableName).getTable();

			if (table_info != null) {
//	        System.out.format("Table name  : %s\n",
//	              table_info.getTableName());
//	        System.out.format("Table ARN   : %s\n",
//	              table_info.getTableArn());
//	        System.out.format("Status      : %s\n",
//	              table_info.getTableStatus());
//	        System.out.format("Item count  : %d\n",
//	              table_info.getItemCount().longValue());
//	        System.out.format("Size (bytes): %d\n",
//	              table_info.getTableSizeBytes().longValue());
//
//	        ProvisionedThroughputDescription throughput_info =
//	           table_info.getProvisionedThroughput();
//	        System.out.println("Throughput");
//	        System.out.format("  Read Capacity : %d\n",
//	              throughput_info.getReadCapacityUnits().longValue());
//	        System.out.format("  Write Capacity: %d\n",
//	              throughput_info.getWriteCapacityUnits().longValue());

				List<AttributeDefinition> attributes = table_info.getAttributeDefinitions();
				System.out.println("Attributes");
				for (AttributeDefinition a : attributes) {
					System.out.format("  %s (%s)\n", a.getAttributeName(), a.getAttributeType());
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void putItem(String tableName, String id, String owner, String description,
			List<OtherAttribute> otherAttributes) {
//		src https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-dynamodb-items.html
		HashMap<String, AttributeValue> item_values = new HashMap<String, AttributeValue>();
		item_values.put("id", new AttributeValue(id));
		item_values.put("owner", new AttributeValue(owner));
		item_values.put("description", new AttributeValue(description));
		item_values.put("otherAttributes", new AttributeValue(otherAttributes.toString()));
		try {
			ddb.putItem(tableName, item_values);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public void updateItem() {

	}

	public void deleteItem() {

	}
}
