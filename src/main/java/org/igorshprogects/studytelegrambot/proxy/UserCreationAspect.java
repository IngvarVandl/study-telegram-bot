package org.igorshprogects.studytelegrambot.proxy;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.igorshprogects.studytelegrambot.entity.user.Action;
import org.igorshprogects.studytelegrambot.entity.user.Role;
import org.igorshprogects.studytelegrambot.entity.user.UserDetails;
import org.igorshprogects.studytelegrambot.repositiry.DetailsRepo;
import org.igorshprogects.studytelegrambot.repositiry.UserRepo;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

@Aspect
@Component
@Order(10)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationAspect {
    final UserRepo userRepo;
    final DetailsRepo detailsRepo;

    public UserCreationAspect(UserRepo userRepo,
                              DetailsRepo detailsRepo) {
        this.userRepo = userRepo;
        this.detailsRepo = detailsRepo;
    }

    @Pointcut("execution(* org.igorshprogects.studytelegrambot.service.UpdateDispatcher.distribute(..))")
    public void distributeMethodPointcut(){}

    @Around("distributeMethodPointcut()")
    public Object distributeMethodAdvice(ProceedingJoinPoint joinPoint)
            throws Throwable{
        Update update = (Update)joinPoint.getArgs()[0];

        User telegramUser;

        if (update.hasMessage()){
            telegramUser = update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            telegramUser = update.getCallbackQuery().getFrom();
        }else {
            return joinPoint.proceed();
        }

        if(userRepo.existsById(telegramUser.getId())){
            return joinPoint.proceed();
        }

        UserDetails details = UserDetails.builder()
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .username(telegramUser.getUserName())
                .registeredAt(String.valueOf(LocalDateTime.now()))
                .build();
        detailsRepo.save(details);
        org.igorshprogects.studytelegrambot.entity.user.User newUser =
                org.igorshprogects.studytelegrambot.entity.user.User.builder()
                        .chatId(telegramUser.getId())
                        .action(Action.FREE)
                        .role(Role.EMPTY)
                        .details(details)
                        .build();
        userRepo.save(newUser);
        return joinPoint.proceed();

    }

}
