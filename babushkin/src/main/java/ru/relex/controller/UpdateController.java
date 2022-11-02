package ru.relex.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.relex.service.UpdateProducer;
import ru.relex.utils.MessageUtils;

import static ru.relex.model.RabbitQueue.*;

@Component
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("update null");
            return;
        }
        if (update.getMessage() != null) {
            distributeMessagesByType(update);
        } else {
            log.error("massage type" + update);
        }
    }

    private void distributeMessagesByType(Update update) {
        var massage = update.getMessage();
        if (massage.getText() != null) {
            processTextMessage(update);
        } else if (massage.getPhoto() != null) {
            processPhotoMessage(update);
        } else if (massage.getDocument() != null) {
            processDocMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setFileIsReceivedView(Update update) {
        var sendNMessage = messageUtils.generateSendMessageWithText(update,
                "Файл получен обработка ...");
        setView(sendNMessage);
    }

    private void setUnsupportedMessageTypeView(Update update) {
        var sendNMessage = messageUtils.generateSendMessageWithText(update,
                "Не поддерживаемый тип сообщения");
        setView(sendNMessage);
    }

    private void setView(SendMessage sendMessage) {
        telegramBot.sendMessage(sendMessage);
    }

    private void processTextMessage(Update update) {
        updateProducer.producer(TEXT_MESSAGE_UPDATE,update);
        setFileIsReceivedView(update);
    }


    private void processPhotoMessage(Update update) {
        updateProducer.producer(PHOTO_MESSAGE_UPDATE,update);
        setFileIsReceivedView(update);
    }


    private void processDocMessage(Update update) {
        updateProducer.producer(DOC_MESSAGE_UPDATE,update);
        setFileIsReceivedView(update);
    }


}
