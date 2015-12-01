/**
 * 
 */
package com.tweet.scanner;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tweet.data.TweetData;

import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;



/**
 * @author Pankaj
 
 * This class reads the data from tweeter, stores tweets in files and upload those files in AWS S3 bucket.
 */
public class WorkerThread implements Runnable {
	private String message;
	private String hashtag;

	public WorkerThread(String s) {
		this.hashtag = s;
	}

	public void run() {

		System.out.println(Thread.currentThread().getName()
				+ " (Start) message = " + message);
		processTweet(hashtag);// call processmessage method that sleeps the
								// thread for 2 seconds
		System.out.println(Thread.currentThread().getName() + " (End)");// prints
																		// thread
																		// name
	}

	private void processTweet(String hashtag) {

		InputStream input;
		Properties properties = null;
		List<Status> tweets = new ArrayList<Status>();
		Query query = new Query(hashtag);
		query.count(10);

		ConfigurationBuilder cb = new ConfigurationBuilder();

		TwitterFactory tf = null;
		Twitter twitter = null;

		Map<String, RateLimitStatus> rateLimitStatus = null;
		RateLimitStatus rate_status = null;

		int twit_count = 0;

		ArrayList<TweetData> atw = new ArrayList<TweetData>();
		try {

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
								properties
										.getProperty("tweet.accesstokensecret"));

				
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
			
	

			//Code for Rest API
			try {
				System.out.println("startting twitt scan...");

				rateLimitStatus = twitter.getRateLimitStatus();
				rate_status = rateLimitStatus.get("/search/tweets");

				System.out.println("Time remaining is"
						+ rate_status.getResetTimeInSeconds());
				System.out
						.println(" 1+*************************Rate is Limit: "
								+ rate_status.getLimit());
				System.out
						.println(" 1+*************************Rate Limit remaining is: "
								+ rate_status.getRemaining());

				// if (rate_status.getRemaining() <= rate_status.getLimit()) {
				if (rate_status.getRemaining() - 5 > 0) {

					QueryResult result = null;
					do {

						result = twitter.search(query);
						tweets = result.getTweets();
						for (Status tweet : tweets) {
							System.out.println("@"
									+ tweet.getUser().getScreenName() + " - "); // +
																				// //tweet.getText());
							String tweeter_username = tweet.getUser()
									.getScreenName();

							TweetData tw = new TweetData();

							tw.setTweet_user(tweet.getUser().getScreenName());
							tw.setTweet_text(tweet.getText());

							atw.add(tw);

							twit_count++;
						}

						 saveTweet(atw);

						atw.clear();

						rateLimitStatus = twitter.getRateLimitStatus();
						rate_status = rateLimitStatus.get("/search/tweets");
						System.out.println(hashtag
								+ " *************************Rate is Limit: "
								+ rate_status.getLimit());
						System.out
								.println(hashtag
										+ " *************************Rate Limit remaining is: "
										+ rate_status.getRemaining());
						System.out.println(hashtag
								+ "Time seconds untill reset is "
								+ rate_status.getSecondsUntilReset());
						System.out.println(hashtag
								+ "Time  reset time in seconds is "
								+ rate_status.getResetTimeInSeconds());

						if (rate_status.getRemaining() - 5 <= 0) {
							System.out.println(hashtag
									+ "Rate limit over....Sleeping");

							System.out.println(hashtag + "Time remaining is "
									+ rate_status.getSecondsUntilReset());

							Thread.sleep(rate_status.getSecondsUntilReset() * 1000);
						}

					} while ((query = result.nextQuery()) != null);
				} else {
					System.out.println(hashtag + "Sleeping");
					Thread.sleep(900000);
				}

			} catch (TwitterException te) {
				System.out
						.println(hashtag
								+ "************************Exception in Twitter Exception");
				te.printStackTrace();

			} catch (InterruptedException ie) {

				ie.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveTweet(ArrayList<TweetData> atw) {
		
		AWSCredentials cred = new BasicAWSCredentials("Authkey",
				"secretekey");
		
		AmazonS3 s3client = new AmazonS3Client(cred);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);
		
		StringBuffer sb=new StringBuffer();
		
		for(TweetData td:atw){
			sb.append(td.getTweet_text());
			sb.append("\n");
			
		}
		
		String text = sb.toString();
		DateTime dt=new DateTime();
		
		String filename=""+dt.getMinuteOfHour();
		String key=""+dt.getHourOfDay()+"/"+filename;
		
        BufferedWriter output = null;
        File file=null;
        try {
             file = new File(filename+".txt");
            output = new BufferedWriter(new FileWriter(file));
            output.write(text);
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( output != null )
				try {
					output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
           
        }
		
        s3client.putObject("tweetertexts", key, file);
	}
	
	
	


}
