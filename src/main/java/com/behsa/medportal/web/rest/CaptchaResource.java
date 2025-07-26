package com.behsa.medportal.web.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class CaptchaResource {

    private final Random random = new Random();
    private final Map<String, String> captchaStore = new HashMap<>();

    @PostMapping("/captcha-endpoint")
    public ResponseEntity<Map<String, Object>> generateCaptcha() {
        // Generate a simple 6-character captcha
        String captchaText = generateCaptchaText();
        String captchaId = generateCaptchaId();
        
        // Store the captcha text for validation
        captchaStore.put(captchaId, captchaText);
        
        Map<String, Object> response = new HashMap<>();
        response.put("captchaId", captchaId);
        response.put("captchaImageUrl", "/api/captcha-image/" + captchaId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/captcha-image/{captchaId}")
    public ResponseEntity<byte[]> getCaptchaImage(@PathVariable String captchaId) {
        try {
            // Get the stored captcha text
            String captchaText = captchaStore.getOrDefault(captchaId, "ERROR");
            
            // Create a simple text-based image
            BufferedImage image = createCaptchaImage(captchaText);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageData = baos.toByteArray();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(imageData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(imageData);
                
        } catch (Exception e) {
            // Return a simple error image
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new byte[0]);
        }
    }

    @PostMapping("/captcha-validate")
    public ResponseEntity<Map<String, Object>> validateCaptcha(@RequestBody Map<String, String> request) {
        String captchaId = request.get("captchaId");
        String userInput = request.get("userInput");
        
        Map<String, Object> response = new HashMap<>();
        
        // Validate against stored captcha
        boolean isValid = validateCaptchaInput(captchaId, userInput);
        response.put("valid", isValid);
        
        // Clean up the stored captcha
        if (captchaId != null) {
            captchaStore.remove(captchaId);
        }
        
        return ResponseEntity.ok(response);
    }

    private String generateCaptchaText() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            captcha.append(chars.charAt(random.nextInt(chars.length())));
        }
        return captcha.toString();
    }

    private String generateCaptchaId() {
        return "captcha_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
    }

    private BufferedImage createCaptchaImage(String text) {
        int width = 200;
        int height = 60;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // Add some noise
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g2d.drawLine(x, y, x + 1, y + 1);
        }
        
        // Draw the text
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (width - textWidth) / 2;
        int y = (height + fm.getAscent()) / 2;
        
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return image;
    }

    private boolean validateCaptchaInput(String captchaId, String userInput) {
        if (captchaId == null || userInput == null) {
            return false;
        }
        
        String storedCaptcha = captchaStore.get(captchaId);
        if (storedCaptcha == null) {
            return false;
        }
        
        // Case-insensitive comparison
        return storedCaptcha.equalsIgnoreCase(userInput.trim());
    }
} 