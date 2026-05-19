package org.example.credit4.service;

import lombok.RequiredArgsConstructor;
import org.example.credit4.entity.CreditRequestEntity;
import org.example.credit4.entity.CreditRequestStatus;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.example.credit4.entity.CreditRequestEntity;
import org.example.credit4.entity.CreditRequestStatus;
import org.springframework.beans.factory.annotation.Value;
import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class TelegramBotService {
    @Value("${telegram.bot.token:}")
    private String botToken;
    @Value("${telegram.bot.enabled:false}")
    private boolean enabled;

    public void sendNotification(CreditRequestEntity request) {
        if (!enabled || botToken == null || botToken.isBlank()) {
            return;
        }
        if (request.getTelegramChatId() == null || request.getTelegramChatId().isBlank()) {
            return;
        }
        String notification = send(request.getStatus());
        String message =
                "\nРешение по кредитной заявке: " +
                        "\nФИО: " + request.getFullName() +
                        "\nНомер телефона: " + request.getPhone() +
                        "\nСумма кредита: " + request.getPrincipal() +
                        "\nСрок: " + request.getMonths() +
                        "\nЕжемесячный платеж " + request.getMonthlyPayment() +
                        "\nИтог " + request.getTotalPaid() +

                        "\nРешение Менеджера: " + notification;
        sendMessage(request.getTelegramChatId(), message);

    }

    private String send(CreditRequestStatus status) {
        if (status == CreditRequestStatus.APPROVED) {
            return "Одобрен";
        }
        if (status == CreditRequestStatus.CANCELLED) {
            return "не одобрен";
        } else {
            return "Ожидает решения";
        }
    }
    private void sendMessage(String chatId, String text) {
        try {
            String uri= "https://api.telegram.org/bot" + botToken + "/sendMessage";
            String body = "chat_id=" + encode(chatId) + "&text=" + encode(text);
            HttpsURLConnection connection = (HttpsURLConnection) URI.create(uri).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int responseCode = connection.getResponseCode();
            System.out.println("Telegram response code:" + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String encode (String value){
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}