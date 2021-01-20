package micronaut_image_test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import javax.inject.Singleton;

@Singleton
public class S3Service {

    private String s3Region  = System.getenv("S3_REGION");
    private  String s3AccessKey = System.getenv("ACCESS_KEY_ID");
    private String s3Secret = System.getenv("SECRET_KEY_ID");

    public AmazonS3 s3client =  (s3Region != null && s3AccessKey != null && s3Secret != null) ?  AmazonS3Client.builder()
            .withRegion(s3Region)
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(s3AccessKey, s3Secret)))
            .build() : null;
}
