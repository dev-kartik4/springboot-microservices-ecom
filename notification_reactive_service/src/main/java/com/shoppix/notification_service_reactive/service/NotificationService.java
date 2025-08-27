package com.shoppix.notification_service_reactive.service;

import com.shoppix.notification_service_reactive.entity.EmailRequestDto;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private Configuration configuration;

    public void sendMail(EmailRequestDto emailRequest, Map<String,String> model){

        String response = "";
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        try{
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            ClassPathResource image = new ClassPathResource("static/shoppix.png");
            Template template = configuration.getTemplate("email.ftl");

            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            helper.setTo(emailRequest.getTo());
            helper.setFrom(fromEmail);
            helper.setSubject(emailRequest.getSubject());
            helper.setText(html, true);
            helper.addInline("shoppix", image);

            emailSender.send(mimeMessage);
            LOGGER.info("Email has been sent to: " + emailRequest.getTo());
        } catch (MessagingException | IOException | TemplateException e) {
            LOGGER.info("Email sending failed to: "+emailRequest.getTo());
        }
    }
}