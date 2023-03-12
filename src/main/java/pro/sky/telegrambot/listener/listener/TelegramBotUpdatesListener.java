package pro.sky.telegrambot.listener.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.listener.model.notificationTask;
import pro.sky.telegrambot.listener.repositorty.notificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@EnableScheduling
public class TelegramBotUpdatesListener implements UpdatesListener {
    @Autowired
    notificationTaskRepository notificationTaskRepository;

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);   // для логов
    private static final Pattern NOTIFICATION_TASK_PATTERN = Pattern.compile(
            "([\\d\\\\.:\\s]{16})(\\s)([А-яA-z\\s\\d]+)"); // парсим сообщение на группы по круглым скобкам
    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                User user = update.message().from();
                Long chatId = user.id();
                logger.info("Processing update: {}", update);
                if (update.message().text().equals("/start")) {

                    SendMessage message = new SendMessage(chatId, "привет");
                    telegramBot.execute(message);
                } else {

                    notificationTask example = new notificationTask();
//                    LocalDateTime dateTime = LocalDateTime.parse(update.message().text().substring(0, 16),   1-е решение разбивал все в ручную
//                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
//                    String message = update.message().text().
//                            substring(update.message().text().lastIndexOf(":") + 3);
                    Matcher matcher = NOTIFICATION_TASK_PATTERN.matcher(update.message().text());  //  получаем сообщение
                    if (matcher.find()) {  //find запускает matcher
                        try {
                            LocalDateTime dateTime = LocalDateTime.parse(matcher.group(1), // получаем 1 группу и форматируем до день, месяц, год
                                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                            String messageText = matcher.group(3); // получаем текст сообщения

                            example.setDateTime(dateTime);
                            example.setMessage(messageText);
                            example.setChatId(chatId);
                            notificationTaskRepository.save(example); // создаем и пишем напоминалку в базу
                            SendMessage message = new SendMessage(chatId, "Ваша задача запланирована!");
                            telegramBot.execute(message);
                        }catch (DateTimeParseException e){
                            SendMessage messageEx = new SendMessage(chatId, "Некорректный формат даты и/или времени!");
                            telegramBot.execute(messageEx);
                        }
                    } else {
                        SendMessage messageEx = new SendMessage(chatId, "ошибка в вводимых данных");
                        telegramBot.execute(messageEx);
                    }
//                } catch (DateTimeParseException | StringIndexOutOfBoundsException | IllegalStateException e) {  Это к изначальному способу парса
//                    Long chatId = update.message().chat().id();
//                    SendMessage message = new SendMessage(chatId, "ошибка " + e);
//                    telegramBot.execute(message);
//                }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void run() {
        notificationTask task = notificationTaskRepository.
                findByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        if (task != null) {
            SendMessage message = new SendMessage(task.getChatId(), " напоминаю" + task.getMessage());
            telegramBot.execute(message);
            notificationTaskRepository.delete(task);

        }
    }
}