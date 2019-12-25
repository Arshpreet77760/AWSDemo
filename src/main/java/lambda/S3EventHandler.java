package lambda;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import model.PersonDetailsVo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class S3EventHandler implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event event, Context context) {
        String bucket = event.getRecords().get(0).getS3().getBucket().getName();
        String key = event.getRecords().get(0).getS3().getObject().getKey();

        S3Object response = getAmazonS3().getObject(new GetObjectRequest(bucket, key));
        InputStream objectContent = response.getObjectContent();
        ObjectMapper mapper = new ObjectMapper();
        try {
            CollectionType typeReference = TypeFactory.defaultInstance().constructCollectionType(List.class,
                    PersonDetailsVo.class);
            List<PersonDetailsVo> personDetailsVo = mapper.readValue(objectContent, typeReference);
            saveToDynamo(personDetailsVo);
        } catch (IOException e) {
            e.printStackTrace();
            context.getLogger().log("Exception thrown with message " + e.getMessage());
        }
        String contentType = response.getObjectMetadata().getContentType();
        context.getLogger().log("CONTENT TYPE: " + contentType);
        return contentType;
    }

    private void saveToDynamo(List<PersonDetailsVo> personDetailsVo) {
        AmazonDynamoDB client = getAmazonDynamoDB();
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.batchSave(personDetailsVo);
    }

    private AmazonS3 getAmazonS3() {
        BasicAWSCredentials creds = new BasicAWSCredentials("test", "test");
        AmazonS3ClientBuilder standard = AmazonS3ClientBuilder.standard();
        AwsClientBuilder.EndpointConfiguration configuration = new AwsClientBuilder.EndpointConfiguration("http://localhost:4572",
                "us-east-1");
        standard.withEndpointConfiguration(configuration);
        standard.withPathStyleAccessEnabled(true);
        standard.withCredentials(new AWSStaticCredentialsProvider(creds));
        return standard.build();
    }

    private AmazonDynamoDB getAmazonDynamoDB() {
        BasicAWSCredentials creds = new BasicAWSCredentials("test", "test");
        AmazonDynamoDBClientBuilder standard = AmazonDynamoDBClientBuilder.standard();
        AwsClientBuilder.EndpointConfiguration configuration = new AwsClientBuilder.EndpointConfiguration("http://localhost:4569",
                "us-east-1");
        standard.withEndpointConfiguration(configuration);
        standard.withCredentials(new AWSStaticCredentialsProvider(creds));
        return standard.build();
    }
}
