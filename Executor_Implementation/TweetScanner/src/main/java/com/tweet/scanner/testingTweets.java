/**
 * 
 */
package com.tweet.scanner;

import java.util.Date;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * @author Pankaj
 * Stores the Tweet data, Not used
 */
@DynamoDBTable(tableName="testingTweets")
public class testingTweets {
	
	private String id;
	private long entryDate = new Date().getTime();
	private String metaData;
	private String extraData;
	
	@DynamoDBAttribute(attributeName="entry_date")
	public long getEntryDate() {
		return entryDate;
	}
	
	@DynamoDBHashKey(attributeName="id")
	@DynamoDBAutoGeneratedKey
	public String getId() {
		return id;
	}
	
	@DynamoDBAttribute(attributeName="meta_data")
	public String getMetaData() {
		return metaData;
	}
	
	@DynamoDBAttribute(attributeName="extra_data")
	public String getExtraData() {
		return extraData;
	}

	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setEntryDate(long entryDate) {
		this.entryDate = entryDate;
	}

}
