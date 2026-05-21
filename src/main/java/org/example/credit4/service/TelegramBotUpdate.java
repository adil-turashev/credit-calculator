package org.example.credit4.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.credit4.entity.CreditRequestEntity;
import org.example.credit4.entity.CreditRequestStatus;
import org.example.credit4.repository.CreditRequestRepository;
import org.example.credit4.util.PhoneUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TelegramBotUpdate {

    private final TelegramBotService telegramBotService;
    private final CreditRequestRepository requestRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private long lastUpdateId = 0;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void readUpdates() {
        try {
            String json = telegramBotService.getUpdates(lastUpdateId + 1);

            if (json == null || json.isBlank()) {
                return;
            }

            JsonNode root = objectMapper.readTree(json);
            JsonNode result = root.get("result");

            if (result == null || !result.isArray()) {
                return;
            }
            for (JsonNode update : result) {
                processUpdate(update);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void processUpdate(JsonNode update) {
        JsonNode updateIdNode = update.get("update_id");

        if (updateIdNode != null) {
            lastUpdateId = Math.max(lastUpdateId, updateIdNode.asLong());
        }
        JsonNode message = update.get("message");
        if (message == null) {
            return;
        }
        JsonNode chat = message.get("chat");
        if (chat == null || chat.get("id") == null) {
            return;
        }
        String chatId = chat.get("id").asText();
        JsonNode text = message.get("text");
        if (text != null && text.asText().startsWith("/start")) {
            telegramBotService.sendContactRequest(chatId);
            return;
        }
        JsonNode contact = message.get("contact");
        if (contact != null) {
            handleContact(message, contact, chatId);
        }
    }
    private void handleContact(JsonNode message, JsonNode contact, String chatId) {
        JsonNode phoneNode = contact.get("phone_number");
        if (phoneNode == null) {
            telegramBotService.sendText(chatId, "Не удалось получить номер телефона.");
            return;
        }
        JsonNode contactUserIdNode = contact.get("user_id");
        JsonNode fromUserIdNode = message.path("from").get("id");
        if (contactUserIdNode != null && fromUserIdNode != null) {
            long contactUserId = contactUserIdNode.asLong();
            long fromUserId = fromUserIdNode.asLong();
            if (contactUserId != fromUserId) {
                telegramBotService.sendText(chatId, "Отправьте именно свой номер телефона.");
                return;
            }
        }
        String phoneNormalized = PhoneUtils.normalize(phoneNode.asText());
        CreditRequestEntity request = requestRepository
                .findFirstByPhoneNormalizedOrderByRequestedAtDesc(phoneNormalized)
                .orElse(null);

        if (request == null) {
            telegramBotService.sendText(
                    chatId,
                    "Заявка с таким номером телефона не найдена. Проверьте номер на сайте."
            );
            return;
        }
        request.setTelegramChatId(chatId);
        requestRepository.save(request);
        if (request.getStatus() == CreditRequestStatus.PENDING) {
            telegramBotService.sendText(chatId,
                    "Telegram подключён к заявке №" + request.getId() + ". Ожидайте решение менеджера."
            );
        } else {
            telegramBotService.sendNotification(request);
        }
    }
}