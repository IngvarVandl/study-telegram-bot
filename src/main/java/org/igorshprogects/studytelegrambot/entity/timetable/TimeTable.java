package org.igorshprogects.studytelegrambot.entity.timetable;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.igorshprogects.studytelegrambot.entity.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "timetable")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimeTable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "title")
    String title;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    WeekDay weekDay;

    @Column(name = "hour")
    Short hour;

    @Column(name = "in_creation")
    Boolean inCreation;

    @Column(name = "minute")
    Short minute;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "timetable_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            name = "users_timetable"
    )
    List<User> users;

    public void addUser(User user) {
        if (users == null){
            users = new ArrayList<>();
        }
        users.add(user);
    }





}
