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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class TelegramBotService {
    @Value("${telegram.bot.token:}")
    private String botToken;
    @Value("${telegram.bot.username:}")
    private String botUsername;
    @Value("${telegram.bot.enabled:false}")
    private boolean enabled;
    public String getBotLink(){
        if (botUsername == null || botUsername.isBlank()){
            return null;
        }
        return "https://t.me/" + botUsername;
    }
    public String getUpdates(long offset) {
        if (!enabled || botToken == null || botToken.isBlank()) {
            return "";
        }
        try {
            String uri = "https://api.telegram.org/bot" + botToken + "/getUpdates?offset=" + offset;
            HttpsURLConnection connection = (HttpsURLConnection) URI.create(uri).toURL().openConnection();
            connection.setRequestMethod("GET");
            try (InputStream inputStream = connection.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public void sendContactRequest(String chatId) {
        String text = "Здравствуйте! Чтобы получать уведомления, нажмите кнопку ниже и подтвердите свой номер телефона.";
        String replyMarkup = "{\"keyboard\":[[{\"text\":\"Подтвердить номер телефона\",\"request_contact\":true}]],\"resize_keyboard\":true,\"one_time_keyboard\":true}";
        sendMessageWithMarkup(chatId, text, replyMarkup);
    }
    public void sendText(String chatId, String text){
        sendMessageWithMarkup(chatId,text,null);
    }
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
        sendMessageWithMarkup(request.getTelegramChatId(), message, null);
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
    private void sendMessageWithMarkup(String chatId, String text, String replyMarkup) {
        try {
            String uri = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            String body = "chat_id=" + encode(chatId) + "&text=" + encode(text);
            if (replyMarkup != null && !replyMarkup.isBlank()) {
                body = body + "&reply_markup=" + encode(replyMarkup);
            }
            HttpsURLConnection connection = (HttpsURLConnection) URI.create(uri).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int responseCode = connection.getResponseCode();
            System.out.println("Telegram response code: " + responseCode);
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}