package com.example.form_flow_backend.Utilities;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class SecretManagerUtilTest {

    @BeforeAll
    static void setup() {
        System.setProperty("DEPLOY_MODE", "local");
    }

    @Test
    public void testGetSecret_validJson() throws JSONException {
        // Arrange
        String secretName = "mySecret";
        String validJson = "{\"username\":\"testUser\",\"password\":\"secretPass\"}";

        // Create a mock SecretsManagerClient that returns a valid JSON string.
        SecretsManagerClient mockClient = mock(SecretsManagerClient.class);
        GetSecretValueResponse fakeResponse = GetSecretValueResponse.builder()
                .secretString(validJson)
                .build();
        when(mockClient.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(fakeResponse);

        // Create a mock builder to simulate the client building process.
        SecretsManagerClientBuilder mockBuilder = mock(SecretsManagerClientBuilder.class);
        when(mockBuilder.region(any(Region.class))).thenReturn(mockBuilder);
        when(mockBuilder.credentialsProvider(any(DefaultCredentialsProvider.class))).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockClient);

        // Use Mockito to mock the static builder() method.
        try (MockedStatic<SecretsManagerClient> mockedStatic = mockStatic(SecretsManagerClient.class)) {
            mockedStatic.when(SecretsManagerClient::builder).thenReturn(mockBuilder);

            // Act
            JSONObject result = SecretManagerUtil.getSecret(secretName);

            // Assert: verify that the returned JSONObject contains the expected data.
            assertNotNull(result);
            assertEquals("testUser", result.getString("username"));
            assertEquals("secretPass", result.getString("password"));

            // Capture and verify that the default region "us-east-1" is used (if AWS_REGION is not set).
            ArgumentCaptor<Region> regionCaptor = ArgumentCaptor.forClass(Region.class);
            verify(mockBuilder).region(regionCaptor.capture());
            assertEquals("us-east-1", regionCaptor.getValue().id());

            // Verify that the secret name was correctly used in the GetSecretValueRequest.
            ArgumentCaptor<GetSecretValueRequest> requestCaptor = ArgumentCaptor.forClass(GetSecretValueRequest.class);
            verify(mockClient).getSecretValue(requestCaptor.capture());
            assertEquals(secretName, requestCaptor.getValue().secretId());
        }
    }

    @Test
    public void testGetSecret_invalidJson() {
        // Arrange
        String secretName = "mySecret";
        String invalidJson = "this is not a json";

        // Create a mock SecretsManagerClient that returns an invalid JSON string.
        SecretsManagerClient mockClient = mock(SecretsManagerClient.class);
        GetSecretValueResponse fakeResponse = GetSecretValueResponse.builder()
                .secretString(invalidJson)
                .build();
        when(mockClient.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(fakeResponse);

        // Create a mock builder to simulate the client building process.
        SecretsManagerClientBuilder mockBuilder = mock(SecretsManagerClientBuilder.class);
        when(mockBuilder.region(any(Region.class))).thenReturn(mockBuilder);
        when(mockBuilder.credentialsProvider(any(DefaultCredentialsProvider.class))).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockClient);

        // Use Mockito to mock the static builder() method.
        try (MockedStatic<SecretsManagerClient> mockedStatic = mockStatic(SecretsManagerClient.class)) {
            mockedStatic.when(SecretsManagerClient::builder).thenReturn(mockBuilder);

            // Act & Assert: A JSONException is expected because the returned string is not valid JSON.
            assertThrows(JSONException.class, () -> SecretManagerUtil.getSecret(secretName));
        }
    }


    /**
     * Test branch where AWS_REGION is set.
     * The code should use the provided region value.
     */
    @Test
    public void testGetSecret_customRegion() throws Exception {
        // Use SystemLambda to temporarily set the AWS_REGION environment variable.
        withEnvironmentVariable("AWS_REGION", "us-east-1").execute(() -> {
            // Arrange
            String secretName = "customSecret";
            String validJson = "{\"key\":\"value\"}";

            // Mock the SecretsManagerClient to return a valid JSON response.
            SecretsManagerClient mockClient = mock(SecretsManagerClient.class);
            GetSecretValueResponse fakeResponse = GetSecretValueResponse.builder()
                    .secretString(validJson)
                    .build();
            when(mockClient.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(fakeResponse);

            // Mock the builder chain.
            SecretsManagerClientBuilder mockBuilder = mock(SecretsManagerClientBuilder.class);
            when(mockBuilder.region(any(Region.class))).thenReturn(mockBuilder);
            when(mockBuilder.credentialsProvider(any(DefaultCredentialsProvider.class))).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockClient);

            // Use Mockito to mock the static builder() method.
            try (MockedStatic<SecretsManagerClient> mockedStatic = mockStatic(SecretsManagerClient.class)) {
                mockedStatic.when(SecretsManagerClient::builder).thenReturn(mockBuilder);

                // Act: Call the method under test.
                JSONObject result = SecretManagerUtil.getSecret(secretName);

                // Assert: Verify that the custom region "us-east-1" was used.
                ArgumentCaptor<Region> regionCaptor = ArgumentCaptor.forClass(Region.class);
                verify(mockBuilder).region(regionCaptor.capture());
                assertEquals("us-east-1", regionCaptor.getValue().id());
            }
        });
    }
}
