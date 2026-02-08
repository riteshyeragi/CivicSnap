package com.example.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GeotagOverlayService {

    private static final int FONT_SIZE = 24;
    private static final int PADDING = 10;
    private static final int LINE_HEIGHT = 30;

    public byte[] overlayGeotag(MultipartFile imageFile, String road, String city, String country,
            Double latitude, Double longitude, List<String> tags) throws IOException {
        BufferedImage image = ImageIO.read(imageFile.getInputStream());
        if (image == null) {
            throw new IOException("Could not read image");
        }

        Graphics2D g = image.createGraphics();
        g.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);

        int y = PADDING + FONT_SIZE;

        if (road != null && !road.isBlank()) {
            g.drawString("Road: " + road, PADDING, y);
            y += LINE_HEIGHT;
        }
        if (city != null && !city.isBlank()) {
            g.drawString("City: " + city, PADDING, y);
            y += LINE_HEIGHT;
        }
        if (country != null && !country.isBlank()) {
            g.drawString("Country: " + country, PADDING, y);
            y += LINE_HEIGHT;
        }
        if (latitude != null && longitude != null) {
            g.drawString(String.format("Lat: %.6f, Lon: %.6f", latitude, longitude), PADDING, y);
            y += LINE_HEIGHT;
        }
        if (tags != null && !tags.isEmpty()) {
            String tagStr = "Tags: " + String.join(", ", tags);
            g.drawString(tagStr, PADDING, y);
        }

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String contentType = imageFile.getContentType();
        String format = contentType != null && contentType.contains("png") ? "png" : "jpg";
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    public MultipartFile wrapAsMultipartFile(byte[] bytes, String originalFilename, String contentType) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "image";
            }

            @Override
            public String getOriginalFilename() {
                return originalFilename;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                return bytes == null || bytes.length == 0;
            }

            @Override
            public long getSize() {
                return bytes != null ? bytes.length : 0;
            }

            @Override
            public byte[] getBytes() {
                return bytes;
            }

            @Override
            public java.io.InputStream getInputStream() {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public void transferTo(java.io.File dest) throws IOException {
                java.nio.file.Files.write(dest.toPath(), bytes);
            }
        };
    }
}
