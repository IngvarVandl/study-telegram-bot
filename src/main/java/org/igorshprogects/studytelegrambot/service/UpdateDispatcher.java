package org.igorshprogects.studytelegrambot.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.igorshprogects.studytelegrambot.service.handler.CallbackQueryHandler;
import org.igorshprogects.studytelegrambot.service.handler.CommandHandler;
import org.igorshprogects.studytelegrambot.service.handler.MessageHandler;
import org.igorshprogects.studytelegrambot.telegram.Bot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class UpdateDispatcher {
    final MessageHandler messageHandler;
    final CommandHandler commandHandler;
    final CallbackQueryHandler callbackQueryHandler;

    public UpdateDispatcher(MessageHandler messageHandler,
                            CommandHandler commandHandler,
                            CallbackQueryHandler callbackQueryHandler) {
        this.messageHandler = messageHandler;
        this.commandHandler = commandHandler;
        this.callbackQueryHandler = callbackQueryHandler;

    }

    public BotApiMethod<?> distribute(Update update, Bot bot){
        if (update.hasCallbackQuery()){
            return callbackQueryHandler.answer(update.getCallbackQuery(), bot);
        }
        if (update.hasMessage()){
            Message message = update.getMessage();
            if(message.hasText()){
                if(message.getText().charAt(0) == '/' ){
                    return commandHandler.answer(message, bot);
                }
            }
            return messageHandler.answer(message, bot);
        }
        log.info("Unsupported update:" + update);
        return null;
    }
}
