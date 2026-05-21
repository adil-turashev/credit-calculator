package org.example.credit4.service;

import lombok.RequiredArgsConstructor;
import org.example.credit4.entity.CreditRequestEntity;
import org.example.credit4.entity.CreditRequestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;



@Service
@RequiredArgsConstructor
public class TelegramUserbotService {
    @Value("${telegram.userbot.url:http://127.0.0.1:5000/send}")
    private String userbotUrl;
    @Value("${telegram.userbot.enabled:false}")
    private boolean enabled;
    public void sendRequestCreatedNotification(CreditRequestEntity request) {
        if (!enabled) {
            return;
        }

        if (request.getPhone() == null || request.getPhone().isBlank()) {
            return;
        }

        String message =
                "Здравствуйте!\n" + "Ваша кредитная заявка принята и отправлена менеджеру";

        sendMessage(request.getPhone(), message);
    }
    public void sendDecisionNotification(CreditRequestEntity request) {
        if (!enabled) {
            return;
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            return;
        }
        String message =
                "Решение по кредитной заявке:\n\n" +
                        "ID заявки: " + request.getId() + "\n" +
                        "ФИО: " + request.getFullName() + "\n" +
                        "Телефон: " + request.getPhone() + "\n" +
                        "Сумма кредита: " + request.getPrincipal() + "\n" +
                        "Срок: " + request.getMonths() + " мес.\n" +
                        "Ежемесячный платёж: " + request.getMonthlyPayment() + "\n" +
                        "Итого к оплате: " + request.getTotalPaid() + "\n\n" +
                        "Решение менеджера: " + getDecisionText(request.getStatus());
        sendMessage(request.getPhone(), message);
    }
    private String getDecisionText(CreditRequestStatus status) {
        if (status == CreditRequestStatus.APPROVED) {
            return "Одобрен";
        }
        if (status == CreditRequestStatus.CANCELLED) {
            return "Не одобрен";
        }
        return "Ожидает решения";
    }
    private void sendMessage(String phone, String message) {
        try {
            String json = """
                    {
                      "phone": "%s",
                      "message": "%s"
                    }
                    """.formatted(
                    escapeJson(phone),
                    escapeJson(message)
            );


            HttpURLConnection connection = (HttpURLConnection) URI.create(userbotUrl).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();

            System.out.println("Userbot response code: " + responseCode);

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}