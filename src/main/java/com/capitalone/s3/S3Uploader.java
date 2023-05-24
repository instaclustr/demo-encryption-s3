package com.capitalone.s3;

import java.io.File;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.encryption.s3.S3EncryptionClient;

public class S3Uploader {

    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println("the first argument has to be name of bucket, the second argument has to be object key, " +
                               "the third argument has to be KMS key id, the fourth argument points to local file to upload");
            System.out.println("Example: ");
            System.out.println("java -jar target/capitalone.jar mybucket it/will/be/uploaded/here this-is-kms-keyid /my/file/to/upload");
            System.exit(1);
        }

        String bucketName = args[0];
        String objectKey = args[1];
        String kmsKeyId = args[2];
        String file = args[3];

        S3Client s3Client = null;
        S3EncryptionClient s3EncryptionClient = null;

        try
        {
            // Create an S3Client instance
            s3Client = S3Client.builder()
                               .credentialsProvider(DefaultCredentialsProvider.create())
                               .build();

            // wrap it in encryption client

            s3EncryptionClient = S3EncryptionClient.builder()
                                                   .wrappedClient(s3Client)
                                                   .kmsKeyId(kmsKeyId)
                                                   .build();

            // Create a PutObjectRequest with the bucket name and object key
            PutObjectRequest request = PutObjectRequest.builder()
                                                       .bucket(bucketName)
                                                       .key(objectKey)
                                                       .build();

            // uploads a text "upload me" to given bucket and object key

            PutObjectResponse response = s3EncryptionClient.putObject(request, RequestBody.fromFile(new File(file)));

            // Check the response status

            System.out.println("Response successful : " + response.sdkHttpResponse().isSuccessful());
            System.out.println("Response status code: " + response.sdkHttpResponse().statusCode());
            System.out.println("Response status text: " + response.sdkHttpResponse().statusText());

            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (s3EncryptionClient != null) {
                s3EncryptionClient.close();
            }
            if (s3Client != null) {
                s3Client.close();
            }
        }
    }
}

