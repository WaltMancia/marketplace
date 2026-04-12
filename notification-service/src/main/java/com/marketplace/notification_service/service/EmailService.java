package com.marketplace.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // TemplateEngine procesa los templates de Thymeleaf
    private final TemplateEngine templateEngine;

    @Value("${notification.from-email}")
    private String fromEmail;

    @Value("${notification.from-name}")
    private String fromName;

    public void sendEmail(
            String to,
            String subject,
            String templateName,
            Map<String, Object> variables
    ) {
        try {
            // Context es el "modelo" que Thymeleaf usa para renderizar el template
            // Es como pasar props a un componente React
            Context context = new Context();
            context.setVariables(variables);

            // Procesamos el template con las variables
            String htmlContent = templateEngine.process(
                    "email/" + templateName,
                    context
            );

            // Construimos el mensaje MIME (formato estándar de email)
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,    // multipart = true permite adjuntos y HTML
                    "UTF-8"
            );

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = es HTML

            mailSender.send(message);
            log.info("Email sent to {} with template {}", to, templateName);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            // Logueamos el error pero NO lanzamos excepción
            // Si el email falla, no queremos que falle todo el proceso
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}