This project implemets the Fixed Thread pool to read the tweets in parallel. 
It then stores the tweets in the form of files on AWS S3 bucket.
This is Maven project. Can be executed using following commands.
Please go to folder where you have stored this project.

mvn clean install (you will get JAR file, now execute follwong command)
java -jar tweet_scan-version (Please check pom.xml for version name)

