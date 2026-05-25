package client.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;

public class CloudinaryUploader {

    private static final String CLOUD_NAME  = System.getenv("CLOUDINARY_NAME");
    private static final String API_KEY     = System.getenv("CLOUDINARY_KEY");
    private static final String API_SECRET  = System.getenv("CLOUDINARY_SECRET");
    private static final String FOLDER      = "auction-items";

    private static String getUploadUrl() {
        return "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";
    }

    public static String upload(File imageFile) {
        if (imageFile == null || !imageFile.exists()) return null;

        try {
            // --- 1. Tạo signature ---
            long timestamp = System.currentTimeMillis() / 1000;
            String toSign   = "folder=" + FOLDER + "&timestamp=" + timestamp + API_SECRET;
            String signature = sha1Hex(toSign);

            // --- 2. Build multipart/form-data ---
            String boundary = "---AuctionBoundary" + System.nanoTime();

            HttpURLConnection conn = (HttpURLConnection) URI.create(getUploadUrl()).toURL().openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setConnectTimeout(15_000);
            conn.setReadTimeout(30_000);

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                writeField(dos, boundary, "api_key",   API_KEY);
                writeField(dos, boundary, "timestamp", String.valueOf(timestamp));
                writeField(dos, boundary, "signature", signature);
                writeField(dos, boundary, "folder",    FOLDER);

                String mime = Files.probeContentType(imageFile.toPath());
                if (mime == null) mime = "image/jpeg";

                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + imageFile.getName() + "\"\r\n");
                dos.writeBytes("Content-Type: " + mime + "\r\n\r\n");
                Files.copy(imageFile.toPath(), dos);
                dos.writeBytes("\r\n--" + boundary + "--\r\n");
            }

            // --- 3. Đọc response ---
            int status = conn.getResponseCode();
            try (InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream()) {
                String body = new String(is.readAllBytes());
                conn.disconnect();

                if (status != 200) {
                    System.err.println("Cloudinary upload failed (" + status + "): " + body);
                    return null;
                }
                return extractJsonValue(body, "secure_url");
            }

        } catch (Exception e) {
            System.err.println("CloudinaryUploader error: " + e.getMessage());
            return null;
        }
    }

    private static void writeField(DataOutputStream dos, String boundary, String name, String value) throws IOException {
        dos.writeBytes("--" + boundary + "\r\n");
        dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        dos.writeBytes(value + "\r\n");
    }

    private static String sha1Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] bytes = md.digest(input.getBytes("UTF-8"));
        return HexFormat.of().formatHex(bytes);
    }

    private static String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end).replace("\\/", "/");
    }
}