package org.igorshprogects.studytelegrambot.repositiry;

import org.igorshprogects.studytelegrambot.entity.timetable.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TimeTableRepo  extends JpaRepository<TimeTable, UUID> {
}
