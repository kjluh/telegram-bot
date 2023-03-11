package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.notificationTask;
import pro.sky.telegrambot.repositorty.notificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;


@Service
@EnableScheduling
public class TelegramBotUpdatesListener implements UpdatesListener {
    @Autowired
    notificationTaskRepository notificationTaskRepository;

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message().text().equals("/start")) {
                Long chatId = update.message().chat().id();
                SendMessage message = new SendMessage(chatId, "привет");
                telegramBot.execute(message);
            } else {
                try {
                    notificationTask example = new notificationTask();
                    LocalDateTime dateTime = LocalDateTime.parse(update.message().text().substring(0, 16),
                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                    String message = update.message().text().
                            substring(update.message().text().lastIndexOf(":") + 3);

                    example.setDateTime(dateTime);
                    example.setMessage(message);
                    example.setChatId(update.message().chat().id());
                    notificationTaskRepository.save(example);
                } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
                    Long chatId = update.message().chat().id();
                    SendMessage message = new SendMessage(chatId, "ошибка " + e);
                    telegramBot.execute(message);
                }

            }
            run();
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void run() {
        notificationTask task = notificationTaskRepository.
                findByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        if (task != null) {
            SendMessage message = new SendMessage(task.getChatId(), task.getMessage());
            telegramBot.execute(message);

        }
    }
}