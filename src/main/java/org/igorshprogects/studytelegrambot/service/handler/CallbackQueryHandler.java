package org.igorshprogects.studytelegrambot.service.handler;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.aspectj.apache.bcel.generic.RET;
import org.igorshprogects.studytelegrambot.service.manager.auth.AuthManager;
import org.igorshprogects.studytelegrambot.service.manager.feedback.FeedbackManager;
import org.igorshprogects.studytelegrambot.service.manager.help.HelpManager;
import org.igorshprogects.studytelegrambot.service.manager.profile.ProfileManager;
import org.igorshprogects.studytelegrambot.service.manager.progress_control.ProgressControlManager;
import org.igorshprogects.studytelegrambot.service.manager.search.SearchManager;
import org.igorshprogects.studytelegrambot.service.manager.task.TaskManager;
import org.igorshprogects.studytelegrambot.service.manager.timetable.TimetableManager;
import org.igorshprogects.studytelegrambot.telegram.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import static org.igorshprogects.studytelegrambot.service.data.CallbackData.*;
import static org.igorshprogects.studytelegrambot.service.data.CallbackData.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallbackQueryHandler {
    final HelpManager helpManager;
    final FeedbackManager feedbackManager;
    final TimetableManager timetableManager;
    final TaskManager taskManager;
    final ProgressControlManager progressControlManager;
    final AuthManager authManager;
    final ProfileManager profileManager;
    final SearchManager searchManager;

    @Autowired
    public CallbackQueryHandler(HelpManager helpManager,
                                FeedbackManager feedbackManager,
                                TimetableManager timetableManager,
                                TaskManager taskManager,
                                ProgressControlManager progressControlManager,
                                AuthManager authManager,
                                ProfileManager profileManager,
                                SearchManager searchManager) {
        this.helpManager = helpManager;
        this.feedbackManager = feedbackManager;
        this.timetableManager = timetableManager;
        this.taskManager = taskManager;
        this.progressControlManager = progressControlManager;
        this.authManager = authManager;
        this.profileManager = profileManager;
        this.searchManager = searchManager;
    }

    public BotApiMethod<?> answer(CallbackQuery callbackQuery, Bot bot){
        String callbackData = callbackQuery.getData();
        String keyWord = callbackData.split("_")[0];
        switch (keyWord){
            case TIMETABLE -> {
                return timetableManager.answerCallbackQuery(callbackQuery,bot);
            }
            case TASK -> {
                return taskManager.answerCallbackQuery(callbackQuery,bot);
            }
            case PROGRESS -> {
                return progressControlManager.answerCallbackQuery(callbackQuery,bot);
            }
            case AUTH -> {
                return authManager.answerCallbackQuery(callbackQuery,bot);
            }
            case PROFILE -> {
                return profileManager.answerCallbackQuery(callbackQuery,bot);
            }
            case SEARCH -> {
                return searchManager.answerCallbackQuery(callbackQuery,bot);
            }
        }
        switch (callbackData){
            case FEEDBACK -> {
                return feedbackManager.answerCallbackQuery(callbackQuery,bot);
            }
            case HELP -> {
                return helpManager.answerCallbackQuery(callbackQuery, bot);
            }
        }
        return null;
    }
}
