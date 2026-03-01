package com.fablix.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RecaptchaVerifyUtils {
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public static void verify(String gRecaptchaResponse) throws Exception {
        if (gRecaptchaResponse == null || gRecaptchaResponse.isBlank()) {
            throw new Exception("Missing reCAPTCHA response.");
        }

        String postData = "secret=" + URLEncoder.encode(RecaptchaConstants.SECRET_KEY, StandardCharsets.UTF_8)
                + "&response=" + URLEncoder.encode(gRecaptchaResponse, StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) new URL(VERIFY_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        if (!response.toString().contains("\"success\": true") && !response.toString().contains("\"success\":true")) {
            throw new Exception("reCAPTCHA verification failed.");
        }
    }
}
