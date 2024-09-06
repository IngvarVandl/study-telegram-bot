package org.igorshprogects.studytelegrambot.service.manager.timetable;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.apache.bcel.generic.RET;
import org.igorshprogects.studytelegrambot.entity.timetable.TimeTable;
import org.igorshprogects.studytelegrambot.entity.timetable.WeekDay;
import org.igorshprogects.studytelegrambot.entity.user.Action;
import org.igorshprogects.studytelegrambot.entity.user.Role;
import org.igorshprogects.studytelegrambot.entity.user.User;
import org.igorshprogects.studytelegrambot.entity.user.UserDetails;
import org.igorshprogects.studytelegrambot.repositiry.DetailsRepo;
import org.igorshprogects.studytelegrambot.repositiry.TimeTableRepo;
import org.igorshprogects.studytelegrambot.repositiry.UserRepo;
import org.igorshprogects.studytelegrambot.service.data.CallbackData;
import org.igorshprogects.studytelegrambot.service.factory.AnswerMethodFactory;
import org.igorshprogects.studytelegrambot.service.factory.KeyboardFactory;
import org.igorshprogects.studytelegrambot.service.manager.AbstractManager;
import org.igorshprogects.studytelegrambot.telegram.Bot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.igorshprogects.studytelegrambot.service.data.CallbackData.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimetableManager extends AbstractManager {
    final AnswerMethodFactory methodFactory;
    final KeyboardFactory keyboardFactory;
    final UserRepo userRepo;
    final TimeTableRepo timeTableRepo;
    final DetailsRepo detailsRepo;

    @Autowired
    public TimetableManager(AnswerMethodFactory methodFactory,
                            KeyboardFactory keyboardFactory,
                            UserRepo userRepo,
                            TimeTableRepo timeTableRepo,
                            DetailsRepo detailsRepo) {
        this.methodFactory = methodFactory;
        this.keyboardFactory = keyboardFactory;
        this.userRepo = userRepo;
        this.timeTableRepo = timeTableRepo;
        this.detailsRepo = detailsRepo;
    }

    @Override
    public BotApiMethod<?> answerCommand(Message message, Bot bot) {
        return mainMenu(message);
    }

    @Override
    public BotApiMethod<?> answerMessage(Message message, Bot bot) {
        var user = userRepo.findUserByChatId(message.getChatId());
        try {
            bot.execute(methodFactory.getDeleteMessage(
                    message.getChatId(), message.getMessageId() - 1
            ));
            bot.execute(methodFactory.getSendMessage(
                    message.getChatId(),
                    "Значение успешно установленно",
                    null
            ));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        switch (user.getAction()){
            case SENDING_TITLE -> {
                return setTitle(message, user);
            }
            case SENDING_DESCRIPTION -> {
                return setDescription(message, user);
            }
        }
        return null;
    }

    @Override
    public BotApiMethod<?> answerCallbackQuery(CallbackQuery callbackQuery, Bot bot) {
        String callbackData = callbackQuery.getData();
        String[] splitCallbackData = callbackData.split("_");
        if (splitCallbackData.length > 1 && "add".equals(splitCallbackData[1])) {
            if (splitCallbackData.length == 2 || splitCallbackData.length == 3) {
                return add(callbackQuery,splitCallbackData);
            }
                switch (splitCallbackData[2]) {
                    case WEEKDAY -> {
                        return addWeekDay(callbackQuery, splitCallbackData);
                    }
                    case HOUR -> {
                        return addHour(callbackQuery,splitCallbackData);
                    }
                    case MINUTE -> {
                        return addMinute(callbackQuery,splitCallbackData);
                    }
                    case USER -> {
                        return addUser(callbackQuery,splitCallbackData);
                    }
                    case TITLE -> {
                        return askTitle(callbackQuery, splitCallbackData);
                    }
                    case DESCRIPTION -> {
                        return askDescription(callbackQuery, splitCallbackData);
                    }
                }
            }
        if (FINISH.equals(splitCallbackData[1])){
            try {
                return finish(callbackQuery, splitCallbackData,bot);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
        if (BACK.equals(splitCallbackData[1])){
                return back(callbackQuery, splitCallbackData);
        }
        switch (callbackData){
            case TIMETABLE -> {
                return mainMenu(callbackQuery);
            }
            case TIMETABLE_SHOW ->{
                return show(callbackQuery);
            }
            case TIMETABLE_REMOVE ->{
                return remove(callbackQuery);
            }
            case TIMETABLE_1,TIMETABLE_2,TIMETABLE_3,
                 TIMETABLE_4,TIMETABLE_5,TIMETABLE_6,
                 TIMETABLE_7 ->{
                return showDat(callbackQuery);
            }
        }
        return null;
    }

    private BotApiMethod<?> setDescription(Message message, User user) {
        user.setAction(Action.FREE);
        userRepo.save(user);
        String id = user.getDetails().getTimetableId();
        var timeTable = timeTableRepo.findTimeTableById(
                UUID.fromString(id)
        );
        timeTable.setDescription(message.getText());
        timeTableRepo.save(timeTable);
        return back(message,id);

    }

    private BotApiMethod<?> setTitle(Message message, User user) {
        user.setAction(Action.FREE);
        userRepo.save(user);
        String id = user.getDetails().getTimetableId();
        var timeTable = timeTableRepo.findTimeTableById(
                UUID.fromString(id)
        );
        timeTable.setTitle(message.getText());
        timeTableRepo.save(timeTable);
        return back(message,id);
    }

    private BotApiMethod<?> askDescription(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[3];
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_DESCRIPTION);
        userRepo.save(user);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Введите описание:",
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_BACK + id)
                )
        );
    }

    private BotApiMethod<?> askTitle(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[3];
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.SENDING_TITLE);
        userRepo.save(user);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Введите заголовок:",
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_BACK + id)
                )
        );
    }

    private BotApiMethod<?> finish(CallbackQuery callbackQuery, String[] splitCallbackData,Bot bot)
            throws TelegramApiException {

        var timeTable = timeTableRepo.findTimeTableById(UUID.fromString(
                splitCallbackData[2]
        ));

        timeTable.setInCreation(false);
        timeTableRepo.save(timeTable);

        bot.execute(methodFactory.getAnswerCallbackQuery(
                callbackQuery.getId(),
                "Процесс создания записи в расписании успешно завершен"
        ));
        return methodFactory.getDeleteMessage(callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId());
    }

    private BotApiMethod<?> back (Message message, String id){
        return methodFactory.getSendMessage(
                message.getChatId(),
                "Вы можете настроить описание и заголовок",
                keyboardFactory.getInlineKeyboard(
                        List.of(TIMETABLE_ADD_TITLE + id,
                                TIMETABLE_ADD_DESCRIPTION + id,
                                TIMETABLE_FINISH + id),
                        List.of(2,1),
                        List.of("Изменить заголовок","Изменить описание",
                                "Завершить создание")
                )
        );
    }

    private BotApiMethod<?> back(CallbackQuery callbackQuery, String[] splitCallbackData){
        String id = splitCallbackData[2];
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        user.setAction(Action.FREE);
        userRepo.save(user);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Вы можете настроить описание и заголовок",
                keyboardFactory.getInlineKeyboard(
                        List.of(TIMETABLE_ADD_TITLE + id,
                                TIMETABLE_ADD_DESCRIPTION + id,
                                TIMETABLE_FINISH + id),
                        List.of(2,1),
                        List.of("Изменить заголовок","Изменить описание",
                                "Завершить создание")
                )
        );
    }

    private BotApiMethod<?> addUser(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[4];
        var timeTable = timeTableRepo.findTimeTableById(UUID.fromString(id));
        var user = userRepo.findUserByChatId(Long.valueOf(splitCallbackData[3]));
        var details = user.getDetails();
        details.setTimetableId(id);
        detailsRepo.save(details);
        user.setDetails(details);

        timeTable.addUser(user);
        timeTable.setTitle(user.getDetails().getFirstName());
        timeTableRepo.save(timeTable);
        userRepo.save(user);

        return methodFactory.getEditMessageText(
                callbackQuery,
                "Успешно! Запись добавдена, теперь вы можете настроить описание и заголовок",
                keyboardFactory.getInlineKeyboard(
                        List.of("Изменить заголовок","Изменить описание",
                                "Завершить создание"),
                        List.of(2,1),
                        List.of(TIMETABLE_ADD_TITLE + id,
                                TIMETABLE_ADD_DESCRIPTION + id,
                                TIMETABLE_FINISH + id)
                )
        );
    }

    private BotApiMethod<?> addMinute(CallbackQuery callbackQuery, String[] splitCallbackData) {
        String id = splitCallbackData[4];
        var timeTable = timeTableRepo.findTimeTableById(UUID.fromString(id));
        List<String> text = new ArrayList<>();
        List<String> data = new ArrayList<>();
        List<Integer> cfg = new ArrayList<>();
        timeTable.setMinute(Short.valueOf(splitCallbackData[3]));
        int index = 0;

        var me = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        for (User user: me.getUsers()){
            log.info(user.getToken());
            text.add(user.getToken());
            data.add(TIMETABLE_ADD_USER + user.getChatId() + "_" + id);
            if (index == 5){
                cfg.add(5);
                index = 0;
            }else {
                index += 1;
            }
        }
        if (index != 0){
            cfg.add(index);
        }
        cfg.add(1);
        data.add(TIMETABLE_ADD_HOUR + timeTable.getHour() + "_" + id);
        text.add("Назад");
        timeTableRepo.save(timeTable);

        String messageText = "Выберете ученика";
        if (cfg.size() == 1){
            messageText = "У вас нет ни одного ученика";
        }
        return methodFactory.getEditMessageText(
                callbackQuery,
                messageText,
                keyboardFactory.getInlineKeyboard(
                        text,
                        cfg,
                        data
                )
        );

    }

    private BotApiMethod<?> addHour(CallbackQuery callbackQuery, String[] splitCallbackData) {
          String id = splitCallbackData[4];
          var timeTable = timeTableRepo.findTimeTableById(UUID.fromString(id));
          List<String> text = new ArrayList<>();
          List<String> data = new ArrayList<>();
          timeTable.setHour(Short.valueOf(splitCallbackData[3]));

          for (int i = 0; i <= 59; i ++){
              text.add(String.valueOf(i));
              data.add(TIMETABLE_ADD_MINUTE + i + "_" + id);
          }
          text.add("Назад");
          switch (timeTable.getWeekDay()){
              case MONDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 1 + "_" + id);
              case TUESDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 2 + "_" + id);
              case WEDNESDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 3 + "_" + id);
              case THURSDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 4 + "_" + id);
              case FRIDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 5 + "_" + id);
              case SATURDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 6 + "_" + id);
              case SUNDAY -> data.add(TIMETABLE_ADD_WEEKDAY + 7 + "_" + id);
          }
          timeTableRepo.save(timeTable);
          return methodFactory.getEditMessageText(
                  callbackQuery,
                  "Выберете минуту",
                  keyboardFactory.getInlineKeyboard(
                          text,
                          List.of(6,6,6,6,6,6,6,6,6,6,1),
                          data
                  )
          );
    }

    private BotApiMethod<?> addWeekDay(CallbackQuery callbackQuery, String[] data) {
        UUID id = UUID.fromString(data[4]);
        var timeTable = timeTableRepo.findTimeTableById(id);
        switch (data[3]){
            case "1" -> timeTable.setWeekDay(WeekDay.MONDAY);
            case "2" -> timeTable.setWeekDay(WeekDay.TUESDAY);
            case "3" -> timeTable.setWeekDay(WeekDay.WEDNESDAY);
            case "4" -> timeTable.setWeekDay(WeekDay.THURSDAY);
            case "5" -> timeTable.setWeekDay(WeekDay.FRIDAY);
            case "6" -> timeTable.setWeekDay(WeekDay.SATURDAY);
            case "7" -> timeTable.setWeekDay(WeekDay.SUNDAY);
        }
        List<String> buttonsData = new ArrayList<>();
        List<String> text = new ArrayList<>();
        for (int i = 1; i <= 24; i++){
            text.add(String.valueOf(i));
            buttonsData.add(TIMETABLE_ADD_HOUR + i + "_" + data[4]);
        }
        buttonsData.add(TIMETABLE_ADD + "_" + data[4]);
        text.add("Назад");
        timeTableRepo.save(timeTable);
        return methodFactory.getEditMessageText(
                callbackQuery,
                "Выберете час",
                keyboardFactory.getInlineKeyboard(
                        text,
                        List.of(6,6,6,6,1),
                        buttonsData
                )
        );
    }


    private BotApiMethod<?> mainMenu(Message message){
        var user = userRepo.findUserByChatId(message.getChatId());
        if (user.getRole() == Role.STUDENT){
            return methodFactory.getSendMessage(
                    message.getChatId(),
                    """
                            📆 Здесь вы можете просматривать ваше расписание""",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Показать мое рассписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)
                    )
            );
        }
        return methodFactory.getSendMessage(
                message.getChatId(),
                """
                        📆 Здесь вы можете управлять вашим расписанием""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Показать мое рассписание",
                                "Удалить занятеи", "Добавить занятие"),
                        List.of(1,2),
                        List.of(TIMETABLE_SHOW,TIMETABLE_REMOVE,TIMETABLE_ADD)
                )

        );
    }


    private BotApiMethod<?> mainMenu(CallbackQuery callbackQuery){
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        if (user.getRole() == Role.STUDENT) {
            return methodFactory.getEditMessageText(
                    callbackQuery,
                    """
                            📆 Здесь вы можете просматривать ваше расписание""",
                    keyboardFactory.getInlineKeyboard(
                            List.of("Показать мое рассписание"),
                            List.of(1),
                            List.of(TIMETABLE_SHOW)
                    )
            );
        }
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        📆 Здесь вы можете управлять вашим расписанием""",
                keyboardFactory.getInlineKeyboard(
                        List.of("Показать мое рассписание",
                                "Удалить занятеи", "Добавить занятие"),
                        List.of(1,2),
                        List.of(TIMETABLE_SHOW,TIMETABLE_REMOVE,TIMETABLE_ADD)
                )

        );
    }


    private BotApiMethod<?> showDat(CallbackQuery callbackQuery) {
        var user = userRepo.findUserByChatId(callbackQuery.getMessage().getChatId());
        WeekDay weekDay = WeekDay.MONDAY;
        switch (callbackQuery.getData().split("_")[1]){
            case "2" -> weekDay = WeekDay.TUESDAY;
            case "3" -> weekDay = WeekDay.WEDNESDAY;
            case "4" -> weekDay = WeekDay.THURSDAY;
            case "5" -> weekDay = WeekDay.FRIDAY;
            case "6" -> weekDay = WeekDay.SATURDAY;
            case "7" -> weekDay = WeekDay.SUNDAY;

        }
        List<TimeTable> timeTableList = timeTableRepo
                .findAllByUsersContainingAndWeekDay(user,weekDay);
        StringBuilder text = new StringBuilder();
        if (timeTableList == null || timeTableList.isEmpty()) {
            text.append( "У вас нет заянтйи в это день!");
        }else {
            text.append("Ваше занятие сегодня: \n");
            for (TimeTable t: timeTableList) {
                text.append("\uFE0F")
                        .append(t.getHour())
                        .append(":")
                        .append(t.getMinute())
                        .append(" - ")
                        .append(t.getTitle())
                        .append("\n\n");
            }
        }
        return methodFactory.getEditMessageText(
                callbackQuery,
                text.toString(),
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE_SHOW)
                )
        );
    }


    private BotApiMethod<?> show(CallbackQuery callbackQuery){
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        📆 Выберете день недели
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of(
                                "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС",
                                "Назад"),
                        List.of(7,1),
                        List.of(TIMETABLE_1,TIMETABLE_2,TIMETABLE_3,TIMETABLE_4,
                                TIMETABLE_5,TIMETABLE_6,TIMETABLE_7,
                                TIMETABLE)
                )
        );
    }


    private BotApiMethod<?> remove(CallbackQuery callbackQuery){
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        ✂️ Выберете занятие, которое хотите удалить из вашего расписания
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("Назад"),
                        List.of(1),
                        List.of(TIMETABLE)
                )
        );
    }


    private BotApiMethod<?> add(CallbackQuery callbackQuery, String[] splitCallbackData){
        String id = "";
        if (splitCallbackData.length == 2){
            var timeTable = new TimeTable();
            timeTable.addUser(userRepo.findUserByChatId(callbackQuery.getMessage().getChatId()));
            timeTable.setInCreation(true);
            id = timeTableRepo.save(timeTable).getId().toString();
        }else {
            id = splitCallbackData[2];
        }
        List<String> data = new ArrayList<>();
        for (int i = 1; i <= 7; i++ ){
            data.add(TIMETABLE_ADD_WEEKDAY + i + "_" + id);
        }
        data.add(TIMETABLE);
        return methodFactory.getEditMessageText(
                callbackQuery,
                """
                        ✏️ Выберете день, в который хотите добавить занятие:
                        """,
                keyboardFactory.getInlineKeyboard(
                        List.of("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС",
                                "\uD83D\uDD19Назад"),
                        List.of(7,1),
                        data
                )
        );
    }


}
