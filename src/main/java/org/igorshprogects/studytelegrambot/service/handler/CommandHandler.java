package org.igorshprogects.studytelegrambot.service.handler;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.igorshprogects.studytelegrambot.service.manager.feedback.FeedbackManager;
import org.igorshprogects.studytelegrambot.service.manager.help.HelpManager;
import org.igorshprogects.studytelegrambot.service.manager.profile.ProfileManager;
import org.igorshprogects.studytelegrambot.service.manager.progress_control.ProgressControlManager;
import org.igorshprogects.studytelegrambot.service.manager.search.SearchManager;
import org.igorshprogects.studytelegrambot.service.manager.start.StartManager;
import org.igorshprogects.studytelegrambot.service.manager.task.TaskManager;
import org.igorshprogects.studytelegrambot.service.manager.timetable.TimetableManager;
import org.igorshprogects.studytelegrambot.telegram.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.igorshprogects.studytelegrambot.service.data.Command.*;
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommandHandler {
    final FeedbackManager feedbackManager;
    final HelpManager helpManager;
    final StartManager startManager;
    final TimetableManager timetableManager;
    final TaskManager taskManager;
    final ProgressControlManager progressControlManager;
    final ProfileManager profileManager;
    final SearchManager searchManager;



    @Autowired
    public CommandHandler(FeedbackManager feedbackManager,
                          HelpManager helpManager,
                          StartManager startManager,
                          TimetableManager timetableManager,
                          TaskManager taskManager,
                          final ProgressControlManager progressControlManager,
                          ProfileManager profileManager,
                          SearchManager searchManager) {
        this.feedbackManager = feedbackManager;
        this.helpManager = helpManager;
        this.startManager = startManager;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
        this.progressControlManager = progressControlManager;
        this.profileManager = profileManager;
        this.searchManager = searchManager;
    }

    public BotApiMethod<?> answer(Message message, Bot bot){
        String command = message.getText();
        switch (command){
            case START ->{
                return startManager.answerCommand(message, bot);
            }
            case FEEDBACK_COMMAND -> {
                return feedbackManager.answerCommand(message,bot);
            }
            case HELP_COMMAND -> {
                return helpManager.answerCommand(message, bot);
            }
            case TIMETABLE -> {
                return timetableManager.answerCommand(message,bot);
            }
            case TASK -> {
                return taskManager.answerCommand(message,bot);
            }
            case PROGRESS -> {
                return progressControlManager.answerCommand(message,bot);
            }
            case PROFILE -> {
                return profileManager.answerCommand(message,bot);
            }
            case SEARCH -> {
                return searchManager.answerCommand(message,bot);
            }
            default -> {
                return defaultAnswer(message);
            }
        }
    }

    private BotApiMethod<?> defaultAnswer(Message message) {
        return SendMessage.builder()
                .text("Неподдерживаемая команда")
                .chatId(message.getChatId())
                .build();
    }

}
