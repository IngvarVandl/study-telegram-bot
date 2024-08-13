package org.igorshprogects.studytelegrambot.service.manager.task;

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

import java.util.List;

import static org.igorshprogects.studytelegrambot.service.data.CallbackData.*;
import static org.igorshprogects.studytelegrambot.service.data.CallbackData.TIMETABLE_ADD;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskManager extends AbstractManager {
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;

    @Autowired
    public TaskManager(AnswerMethodFactory methodFactory, KeyboardFactory keyboardFactory) {
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
            case TASK -> {
                return mainMenu(callbackQuery);
            }
            case TASK_CREATE -> {
                return crate(callbackQuery);
            }

        }
        return null;
    }

    private BotApiMethod<?> mainMenu(Message message){
        return methodFactory.getSendMessage(
                message.getChatId(),
                """
                        🗂 Вы можете добавить домашнее задание вашему ученику""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TIMETABLE_SHOW)
                )

        );
    }
    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery){
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        🗂 Вы можете добавить домашнее задание вашему ученику""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Прикрепить домашнее задание"),
                        List.of(1),
                        List.of(TIMETABLE_SHOW)
                )

        );
    }
    private BotApiMethod<?> crate(CallbackQuery callbackQuery) {
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        👤 Выберете ученика, которому хотите дать домашнее задание
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TASK)
                )
        );
    }
}
