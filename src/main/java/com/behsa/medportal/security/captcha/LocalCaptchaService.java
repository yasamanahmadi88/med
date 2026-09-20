package com.behsa.medportal.security.captcha;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
@Service
public class LocalCaptchaService {

    public static final class Issue {
        public final String id;
        Issue(String id) { this.id = id; }
    }

    private static final class Entry {
        final String text;
        final long expiresAt;
        Entry(String text, long expiresAt) { this.text = text; this.expiresAt = expiresAt; }
    }

    private final SecureRandom rng = new SecureRandom();

    // id -> (text, expiry). Replace with Redis for multi-node deployments.
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    // Configuration (you can surface these in CaptchaProperties later)
    private final int width = 200;
    private final int height = 60;
    private final int length = 6;
    private final long ttlMillis = 120_000; // 2 minutes
    private final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I

    /** Create a new captcha and keep the answer server-side. */
    public Issue issue() {
        String text = randomText(length);
        String id = UUID.randomUUID().toString();
        store.put(id, new Entry(text.toLowerCase(), System.currentTimeMillis() + ttlMillis));
        return new Issue(id);
    }

    /** Render PNG for an issued id (does not consume). */
//    public byte[] renderPng(String id) {
//        Entry e = store.get(id);
//        if (e == null || e.expiresAt < System.currentTimeMillis()) return null;
//        return render(e.text);
//    }
    public byte[] renderPng(String id) {
        Entry e = store.get(id);

        if (e == null) {
            return null;
        }

        if (e.expiresAt < System.currentTimeMillis()) {
            store.remove(id);
            return null;
        }

        return render(e.text);
    }

    /** Peek validity (no consume). */
//    public boolean peek(String id, String userInput) {
//        Entry e = store.get(id);
//        if (e == null || e.expiresAt < System.currentTimeMillis()) return false;
//        return userInput != null && userInput.trim().toLowerCase().equals(e.text);
//    }

    /** Verify and consume (one-time). */
    public boolean verifyAndConsume(String id, String userInput) {
        Entry e = store.remove(id);
        if (e == null || e.expiresAt < System.currentTimeMillis()) return false;
        return userInput != null && userInput.trim().toLowerCase().equals(e.text);
    }

    // ── helpers ──────────────────────────────────────────────────
    private String randomText(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(alphabet.charAt(rng.nextInt(alphabet.length())));
        return sb.toString();
    }

    private byte[] render(String text) {
        try {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(240, 240, 240));
                g.fillRect(0, 0, width, height);

                // noise lines
                for (int i = 0; i < 12; i++) {
                    g.setColor(randColor(120, 200));
                    g.drawLine(rng.nextInt(width), rng.nextInt(height), rng.nextInt(width), rng.nextInt(height));
                }

                int x = 16;
                for (char c : text.toCharArray()) {
                    g.setFont(new Font("Arial", Font.BOLD, 30 + rng.nextInt(8)));
                    AffineTransform old = g.getTransform();
                    g.rotate(Math.toRadians(rng.nextInt(21) - 10), x, height / 2.0);
                    g.setColor(new Color(rng.nextInt(50), rng.nextInt(50), rng.nextInt(50)));
                    g.drawString(String.valueOf(c), x, (int) (height * 0.7));
                    g.setTransform(old);
                    x += width / (text.length() + 1);
                }

                // speckles
                for (int i = 0; i < 200; i++) {
                    g.setColor(randColor(150, 255));
                    g.fillRect(rng.nextInt(width), rng.nextInt(height), 1, 1);
                }
            } finally { g.dispose(); }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to render captcha", ex);
        }
    }

    private Color randColor(int min, int max) {
        int r = min + rng.nextInt(max - min + 1);
        int g = min + rng.nextInt(max - min + 1);
        int b = min + rng.nextInt(max - min + 1);
        return new Color(r, g, b);
    }
    @Scheduled(fixedDelay = 60000)
    public void cleanupExpiredCaptchas() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(entry -> entry.getValue().expiresAt < now);
    }
}
