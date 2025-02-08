package com.example.form_flow_backend.Utilities;

import org.json.JSONException;
import org.json.JSONObject;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * Utility class for accessing AWS Secrets Manager.
 * English comment: Provides methods to retrieve secrets as JSON objects.
 */
public class SecretManagerUtil {

    /**
     * Retrieves a secret from AWS Secrets Manager.
     *
     * @param secretName the name of the secret to retrieve
     * @return a JSONObject containing the secret's key-value pairs
     * @throws JSONException if an error occurs while parsing the secret string to JSON
     */
    public static JSONObject getSecret(String secretName) throws JSONException {
        String region = System.getenv("AWS_REGION");
        if (region == null) {
            region = "us-east-1"; // default region if not specified
        }

        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse response = client.getSecretValue(request);
        return new JSONObject(response.secretString());
    }
}
