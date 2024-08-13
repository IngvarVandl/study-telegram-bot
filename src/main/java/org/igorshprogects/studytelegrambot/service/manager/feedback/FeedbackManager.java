package org.igorshprogects.studytelegrambot.service.manager.feedback;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.igorshprogects.studytelegrambot.service.factory.AnswerMethodFactory;
import org.igorshprogects.studytelegrambot.service.factory.KeyboardFactory;
import org.igorshprogects.studytelegrambot.service.manager.AbstractManager;
import org.igorshprogects.studytelegrambot.telegram.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;


@Component
@FieldDefaults(level = AccessLevel.PRIVATE)

public class FeedbackManager extends AbstractManager {
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;

    @Autowired
    public FeedbackManager(AnswerMethodFactory methodFactory, KeyboardFactory keyboardFactory) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
    }
    @Override
    public BotApiMethod<?> answerCommand (Message message, Bot bot){
        return methodFactory.getSendMessage(
                message.getChatId(),
                """
                        📍 Ссылки для обратной связи
                        GitHub - 
                        LinkedIn - 
                        Telegram - 
                        """,
                null
                );
    }
    @Override
    public BotApiMethod<?> answerCallbackQuery (CallbackQuery callbackQuery, Bot bot){
        return methodFactory.getEditMessageText(
                        callbackQuery,
                        """
                                📍 Ссылки для обратной связи
                                GitHub - 
                                LinkedIn - 
                                Telegram - 
                                """,
                null
                );
    }
    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }
}
