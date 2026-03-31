package com.hrm.auth;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

@Component
public class CaptchaService {

	private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int WIDTH = 110;
	private static final int HEIGHT = 40;
	private static final long EXPIRE_MILLIS = 3 * 60 * 1000;

	private final Map<String, Entry> store = new ConcurrentHashMap<>();

	public CaptchaResponse generate() {
		String code = randomCode(4);
		String id = UUID.randomUUID().toString().replace("-", "");
		store.put(id, new Entry(code, Instant.now().toEpochMilli() + EXPIRE_MILLIS));

		CaptchaResponse resp = new CaptchaResponse();
		resp.setCaptchaId(id);
		resp.setImageBase64(renderBase64(code));
		return resp;
	}

	public boolean validate(String id, String code) {
		if (id == null || code == null) {
			return false;
		}
		Entry entry = store.get(id);
		if (entry == null) {
			return false;
		}
		if (Instant.now().toEpochMilli() > entry.expireAt) {
			store.remove(id);
			return false;
		}
		boolean ok = entry.code.equalsIgnoreCase(code.trim());
		store.remove(id);
		return ok;
	}

	private String randomCode(int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i += 1) {
			int idx = (int) (Math.random() * CHARS.length());
			sb.append(CHARS.charAt(idx));
		}
		return sb.toString();
	}

	private String renderBase64(String code) {
		try {
			BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.setColor(new Color(245, 243, 236));
			g.fillRect(0, 0, WIDTH, HEIGHT);
			g.setFont(new Font("SansSerif", Font.BOLD, 22));
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(27, 51, 75));
			g.drawString(code, 18, 27);

			g.setColor(new Color(200, 190, 170));
			for (int i = 0; i < 6; i += 1) {
				int x1 = (int) (Math.random() * WIDTH);
				int y1 = (int) (Math.random() * HEIGHT);
				int x2 = (int) (Math.random() * WIDTH);
				int y2 = (int) (Math.random() * HEIGHT);
				g.drawLine(x1, y1, x2, y2);
			}
			g.dispose();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		} catch (Exception ex) {
			return "";
		}
	}

	private static class Entry {
		private final String code;
		private final long expireAt;

		private Entry(String code, long expireAt) {
			this.code = code;
			this.expireAt = expireAt;
		}
	}
}
