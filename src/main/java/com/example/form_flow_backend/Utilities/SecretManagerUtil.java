package com.example.form_flow_backend.Utilities;

import org.json.JSONException;
import org.json.JSONObject;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class SecretManagerUtil {
    public static JSONObject getSecret(String secretName) throws JSONException {
        final String REGION = System.getenv("AWS_REGION") != null ?
                System.getenv("AWS_REGION") : "us-east-1";

        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(REGION))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse response = client.getSecretValue(request);
        return new JSONObject(response.secretString());
    }
}

