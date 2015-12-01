package com.tweet.scanner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import ch.qos.logback.core.net.server.Client;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.tweet.data.TweetData;

/**
 * @author Pankaj
 *
 *         This class contains the Scheduled methods, which is independent of this application. This class is separate implemeantation to schedule the read API calls.
 */
@Component
public class TweetScheduler {
	
	static DynamoDBMapper dynamoDB=null;

	public DynamoDBMapper connect() {

		AWSCredentials cred = new BasicAWSCredentials("Authkey",
				"Secretekey");
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(cred);
		Region US_WEST_1 = Region.getRegion(Regions.US_WEST_2);
		client.setRegion(US_WEST_1);
		DynamoDBMapper dbMapper = new DynamoDBMapper(client);
		return dbMapper;
	}

	@Scheduled(fixedRate = 5000)
	public void scanTweets() {

		
		fetchTweeterData("#ipl");

	}

	public void fetchTweeterData(String hashtag) {
		InputStream input;
		Properties properties = null;
		List<Status> tweets = new ArrayList<Status>();
		Query query = new Query(hashtag);
		query.count(10);

		ConfigurationBuilder cb = new ConfigurationBuilder();

		TwitterFactory tf = null;
		Twitter twitter = null;

		Map<String, RateLimitStatus> rateLimitStatus =null;
		RateLimitStatus rate_status =null;

		int twit_count = 0;
		
		ArrayList<TweetData> atw=new ArrayList<TweetData>(); 

		try {

			input = this.getClass().getClassLoader()
					.getResourceAsStream("application.properties");
			properties = new Properties();
			properties.load(input);
			input.close();

			cb.setDebugEnabled(true)
					.setOAuthConsumerKey(
							properties.getProperty("tweet.consumer"))
					.setOAuthConsumerSecret(
							properties.getProperty("tweet.consumersecret"))
					.setOAuthAccessToken(
							properties.getProperty("tweet.accesstoken"))
					.setOAuthAccessTokenSecret(
							properties.getProperty("tweet.accesstokensecret"));
			
			 tf = new TwitterFactory(cb.build());
			 twitter = tf.getInstance();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (Exception e) {
			System.out.println("Exception in File handling");
			e.printStackTrace();
			System.exit(-1);
		}

		try {
			System.out.println("startting twitt scan...");

			rateLimitStatus = twitter.getRateLimitStatus();
			 rate_status = rateLimitStatus.get("/search/tweets");
			 
			 System.out.println("Time remaining is"+rate_status.getResetTimeInSeconds());
			 System.out.println(" 1+*************************Rate is Limit: " + rate_status.getLimit());
             System.out.println(" 1+*************************Rate Limit remaining is: " + rate_status.getRemaining());
		
			
			//if (rate_status.getRemaining() <= rate_status.getLimit()) {
				if (rate_status.getRemaining()>0) {
					
				QueryResult result=null;
				do {
					
					result = twitter.search(query);
					tweets = result.getTweets();
					for (Status tweet : tweets) {
						System.out.println("@"
								+ tweet.getUser().getScreenName() + " - "); // +
																			// //tweet.getText());
						String tweeter_username = tweet.getUser()
								.getScreenName();
						
						TweetData tw=new TweetData();
						
						tw.setTweet_user(tweet.getUser().getScreenName());
						tw.setTweet_text(tweet.getText());
						
						atw.add(tw);

						twit_count++;
					}
					
					saveTweet(atw);
					
					atw.clear();
					
					rateLimitStatus = twitter.getRateLimitStatus();
					 rate_status = rateLimitStatus.get("/search/tweets");
					 System.out.println(" *************************Rate is Limit: " + rate_status.getLimit());
                    System.out.println(" *************************Rate Limit remaining is: " + rate_status.getRemaining());
                    System.out.println("Time seconds untill reset is "+ rate_status.getSecondsUntilReset());
                    System.out.println("Time  reset time in seconds is "+ rate_status.getResetTimeInSeconds());
                    
					if(rate_status.getRemaining()-5<=0){
							System.out.println("Rate limit over....Sleeping");
						
							System.out.println("Time remaining is "+ rate_status.getSecondsUntilReset());
						
                    	 Thread.sleep(rate_status.getSecondsUntilReset()*1000);
                     }
					

				} while ((query = result.nextQuery()) != null);
			}else{
				 System.out.println("Sleeping");
				Thread.sleep(900000);
			}

		} catch (TwitterException te) {
			System.out.println("************************Exception in Twitter Exception");
			te.printStackTrace();
			
		}catch (InterruptedException ie) {

			ie.printStackTrace();
		}

	}
	
	public void saveTweet(ArrayList<TweetData> tw){
		
		if(dynamoDB==null)
		 dynamoDB = connect();
		
		try{ 
			dynamoDB.batchSave(tw);
			System.out.println("Data Saved...");
		  
		  }
		catch(Exception e)
		{ System.out.println("Exception occured..."+
		 e.getMessage()); e.printStackTrace(); 
		 }
		 
		
		
	}



}
