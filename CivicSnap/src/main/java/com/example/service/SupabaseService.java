package com.example.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SupabaseService {

    private final String supabaseUrl;
    private final String anonKey;
    private final String serviceRoleKey;
    private final String storageBucket;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public SupabaseService(
            @Value("${supabase.url:https://example.supabase.co}") String supabaseUrl,
            @Value("${supabase.anon-key:dev-anon-key}") String anonKey,
            @Value("${supabase.service-role-key:dev-service-role-key}") String serviceRoleKey,
            @Value("${supabase.storage.bucket:public}") String storageBucket
    ) {
        this.supabaseUrl = supabaseUrl;
        this.anonKey = anonKey;
        this.serviceRoleKey = serviceRoleKey;
        this.storageBucket = storageBucket;
    }

    // ================= AUTH =================

    public SupabaseAuthResponse signUp(String email, String password)
            throws IOException, InterruptedException {

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        return postAuth("/auth/v1/signup", body, anonKey);
    }

    public SupabaseAuthResponse signIn(String email, String password)
            throws IOException, InterruptedException {

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        return postAuth("/auth/v1/token?grant_type=password", body, anonKey);
    }

    public void recoverPassword(String email)
            throws IOException, InterruptedException {

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/auth/v1/recover"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("apikey", anonKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            JsonNode error = objectMapper.readTree(response.body());
            String msg = error.has("msg") ? error.get("msg").asText() : response.body();
            throw new RuntimeException("Password recovery failed: " + msg);
        }
    }

    // ================= STORAGE =================

    public String uploadImage(MultipartFile file, String fileName)
            throws IOException, InterruptedException {

        return uploadImage(
                file.getBytes(),
                fileName,
                file.getContentType() != null ? file.getContentType() : "image/jpeg"
        );
    }

    public String uploadImage(byte[] bytes, String fileName, String contentType)
            throws IOException, InterruptedException {

        String path = "issues/" + fileName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        supabaseUrl + "/storage/v1/object/" + storageBucket + "/" + path))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("Storage upload failed: " + response.body());
        }

        return supabaseUrl
                + "/storage/v1/object/public/"
                + storageBucket
                + "/"
                + path;
    }

    // ================= INTERNAL =================

    private SupabaseAuthResponse postAuth(
            String path, Map<String, Object> body, String key)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + path))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("apikey", key)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode json = objectMapper.readTree(response.body());

        if (response.statusCode() >= 400) {
            String msg = json.has("msg") ? json.get("msg").asText() : response.body();
            throw new RuntimeException("Auth failed: " + msg);
        }

        String accessToken = json.has("access_token")
                ? json.get("access_token").asText()
                : null;

        String userId = json.has("user") && json.get("user").has("id")
                ? json.get("user").get("id").asText()
                : null;

        String userEmail = json.has("user") && json.get("user").has("email")
                ? json.get("user").get("email").asText()
                : null;

        return new SupabaseAuthResponse(accessToken, userId, userEmail);
    }

    // ================= DTO =================

    public static class SupabaseAuthResponse {
        private final String accessToken;
        private final String userId;
        private final String email;

        public SupabaseAuthResponse(String accessToken, String userId, String email) {
            this.accessToken = accessToken;
            this.userId = userId;
            this.email = email;
        }

        public String getAccessToken() { return accessToken; }
        public String getUserId() { return userId; }
        public String getEmail() { return email; }
    }
}