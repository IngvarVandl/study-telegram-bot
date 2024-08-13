package org.igorshprogects.studytelegrambot.service.manager.progress_control;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.igorshprogects.studytelegrambot.service.factory.AnswerMethodFactory;
import org.igorshprogects.studytelegrambot.service.factory.KeyboardFactory;
import org.igorshprogects.studytelegrambot.service.manager.AbstractManager;
import org.igorshprogects.studytelegrambot.telegram.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import static org.igorshprogects.studytelegrambot.service.data.CallbackData.*;

import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProgressControlManager extends AbstractManager {
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;

    @Autowired
    public ProgressControlManager(AnswerMethodFactory methodFactory,
                                  KeyboardFactory keyboardFactory) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return mainMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        switch (callbackData){
            case PROGRESS -> {
                return mainMenu(callbackQuery);
            }
            case PROGRESS_STAT -> {
                return stat(callbackQuery);
            }
        }
        return null;
    }

    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery){
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        Здесь вы можете увидеть
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Статистика успеваемости"),
                        List.of(1),
                        List.of(PROGRESS_STAT)
                )
        );

    }

    private BotApiMethod<?> mainMenu(Message message){
        return methodFactory.getSendMessage(
                message.getChatId(),
                """
                        Здесь вы можете увидеть
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Статистика успеваемости"),
                        List.of(1),
                        List.of(PROGRESS_STAT)
                )
        );
    }

    private BotApiMethod<?> stat(CallbackQuery callbackQuery){
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        Здесь будет статистика
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(PROGRESS)
                )
        );

    }



}
