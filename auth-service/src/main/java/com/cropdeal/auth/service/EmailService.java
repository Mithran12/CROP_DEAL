package com.cropdeal.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;

	@org.springframework.beans.factory.annotation.Value("${internal.api.key:change-this-internal-key}")
	private String internalApiKey;

	public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendOtpEmail(String toEmail, String otp) {
		log.info("CropDeal Password Reset OTP generated for {}: {}", toEmail, otp);

		// 1. First delegate to Notification Service
		boolean sentViaNotificationService = false;
		try {
			java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
			String payload = String.format("{\"email\":\"%s\",\"otp\":\"%s\"}", toEmail, otp);
			java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
					.uri(java.net.URI.create("http://localhost:8086/notifications/internal/send-otp"))
					.header("Content-Type", "application/json").header("X-Internal-Key", internalApiKey)
					.POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload))
					.timeout(java.time.Duration.ofSeconds(3)).build();

			java.net.http.HttpResponse<String> response = client.send(request,
					java.net.http.HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				log.info("OTP dispatched successfully via Notification Service to {}", toEmail);
				sentViaNotificationService = true;
			}
		} catch (Exception ex) {
			log.warn("Could not delegate OTP email to Notification Service: {}", ex.getMessage());
		}

		if (sentViaNotificationService) {
			return;
		}

		// 2. Fallback to local JavaMailSender if configured
		if (mailSender != null) {
			try {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setTo(toEmail);
				message.setSubject("CropDeal Password Reset OTP");
				message.setText("CROPDEAL\n\n" + "Your password reset OTP is: " + otp + "\n\n"
						+ "This OTP is valid for 5 minutes.\n" + "Do not share this OTP with anyone.\n\n"
						+ "If you did not request this password reset, you can safely ignore this email.\n");

				mailSender.send(message);
				log.info("Password reset OTP email sent successfully to {}", toEmail);
			} catch (Exception e) {
				log.warn("Failed to send OTP email locally to {}: {}. OTP is logged for development.", toEmail,
						e.getMessage());
			}
		}
	}
}
