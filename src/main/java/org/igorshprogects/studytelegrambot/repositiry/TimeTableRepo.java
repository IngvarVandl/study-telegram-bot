package org.igorshprogects.studytelegrambot.repositiry;

import org.igorshprogects.studytelegrambot.entity.timetable.TimeTable;
import org.igorshprogects.studytelegrambot.entity.timetable.WeekDay;
import org.igorshprogects.studytelegrambot.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimeTableRepo  extends JpaRepository<TimeTable, UUID> {

    List<TimeTable> findAllByUsersContainingAndWeekDay(User user, WeekDay weekDay);
    TimeTable findTimeTableById(UUID id);
}
