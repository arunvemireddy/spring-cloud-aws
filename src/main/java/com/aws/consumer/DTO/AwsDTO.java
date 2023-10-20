package com.aws.consumer.DTO;

import org.springframework.stereotype.Component;

@Component
public class AwsDTO {

	private String bucketName2 = "usu-cs5260-ironman-requests";

	private String bucketName3 = "usu-cs5260-ironman-web";

	private String requestType = "create";

	private String programEnded = "programEnded";

	private String regionName = "us-east-1";

	private String tableName = "widgets";

	public String getBucketName2() {
		return bucketName2;
	}

	public void setBucketName2(String bucketName2) {
		this.bucketName2 = bucketName2;
	}

	public String getBucketName3() {
		return bucketName3;
	}

	public void setBucketName3(String bucketName3) {
		this.bucketName3 = bucketName3;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getProgramEnded() {
		return programEnded;
	}

	public void setProgramEnded(String programEnded) {
		this.programEnded = programEnded;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
