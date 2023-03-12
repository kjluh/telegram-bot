package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.listener.listener.TelegramBotUpdatesListener;
import pro.sky.telegrambot.listener.model.notificationTask;
import pro.sky.telegrambot.listener.repositorty.notificationTaskRepository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TelegramBotUpdatesListenerTests {

	@Mock
	private TelegramBot telegramBot;

	@InjectMocks
	private TelegramBotUpdatesListener telegramBotUpdatesListener;

    @Mock
    private notificationTaskRepository notificationTaskRepository;


	@Test
	public void handleStartTest() throws URISyntaxException, IOException {
		String json = Files.readString(
				Paths.get(TelegramBotUpdatesListenerTests.class.getResource("text_update.json").toURI()));
		Update update = getUpdate(json, "/start");
		telegramBotUpdatesListener.process(Collections.singletonList(update));

		ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
		Mockito.verify(telegramBot).execute(argumentCaptor.capture());
		SendMessage actual = argumentCaptor.getValue();

		Assertions.assertThat(actual.getParameters().get("chat_id")).isEqualTo(123L);
		Assertions.assertThat(actual.getParameters().get("text")).isEqualTo(
				"привет");
//		Assertions.assertThat(actual.getParameters().get("parse_mode"))
//				.isEqualTo(ParseMode.Markdown.name());
	}

	@Test
	public void handleInvalidMessage() throws URISyntaxException, IOException {
		String json = Files.readString(
				Paths.get(TelegramBotUpdatesListenerTests.class.getResource("text_update.json").toURI()));
		Update update = getUpdate(json, "hello world");
		telegramBotUpdatesListener.process(Collections.singletonList(update));

		ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
		Mockito.verify(telegramBot).execute(argumentCaptor.capture());
		SendMessage actual = argumentCaptor.getValue();

		Assertions.assertThat(actual.getParameters().get("chat_id")).isEqualTo(123L);
		Assertions.assertThat(actual.getParameters().get("text")).isEqualTo(
				"ошибка в вводимых данных");
	}

	@Test
	public void handleInvalidDateFormat() throws URISyntaxException, IOException {
		String json = Files.readString(
				Paths.get(TelegramBotUpdatesListenerTests.class.getResource("text_update.json").toURI()));
		Update update = getUpdate(json, "32.12.2022 20:00 hello world");
		telegramBotUpdatesListener.process(Collections.singletonList(update));

		ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
		Mockito.verify(telegramBot).execute(argumentCaptor.capture());
		SendMessage actual = argumentCaptor.getValue();

		Assertions.assertThat(actual.getParameters().get("chat_id")).isEqualTo(123L);
		Assertions.assertThat(actual.getParameters().get("text")).isEqualTo(
				"Некорректный формат даты и/или времени!");
	}

	@Test
	public void handleValidMessage() throws URISyntaxException, IOException {
		String json = Files.readString(
				Paths.get(TelegramBotUpdatesListenerTests.class.getResource("text_update.json").toURI()));
		Update update = getUpdate(json, "31.12.2022 20:00 hello world");
		telegramBotUpdatesListener.process(Collections.singletonList(update));   // тут закинули сообщение в бота, где он должен его записать

		ArgumentCaptor<SendMessage> sendMessageArgumentCaptor = ArgumentCaptor.forClass(SendMessage.class); // создаем капу(ловца) для получения сообщения о записи в бд
		Mockito.verify(telegramBot).execute(sendMessageArgumentCaptor.capture()); // проверяем что метод вызывался
		SendMessage actualSendMessage = sendMessageArgumentCaptor.getValue(); // ловит сообщения о записи в бд

		ArgumentCaptor<LocalDateTime> localDateTimeArgumentCaptor = ArgumentCaptor.forClass(LocalDateTime.class); //создаем капу(ловца) для получения параметров записанного обьекта
		ArgumentCaptor<String> stringTimeArgumentCaptor = ArgumentCaptor.forClass(String.class);//создаем капу(ловца) для получения параметров записанного обьекта
		ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);//создаем капу(ловца) для получения параметров записанного обьекта
		Mockito.verify(telegramBotUpdatesListener).Что тут писать??? (
				localDateTimeArgumentCaptor.capture(),
				stringTimeArgumentCaptor.capture(),
				longArgumentCaptor.capture()
		);
//        addNotificationTask(localDateTimeArgumentCaptor.capture(),
//                stringTimeArgumentCaptor.capture(),
//                longArgumentCaptor.capture());

		//  что это?
		LocalDateTime actualLocalDateTime = localDateTimeArgumentCaptor.getValue();
		String actualString = stringTimeArgumentCaptor.getValue();
		Long actualLong = longArgumentCaptor.getValue();

		Assertions.assertThat(actualLocalDateTime)
				.isEqualTo(LocalDateTime.of(2022, Month.DECEMBER, 31, 20, 0));
		Assertions.assertThat(actualString).isEqualTo("hello world");
		Assertions.assertThat(actualLong).isEqualTo(123L);

		Assertions.assertThat(actualSendMessage.getParameters().get("chat_id")).isEqualTo(123L);
		Assertions.assertThat(actualSendMessage.getParameters().get("text")).isEqualTo(
				"Ваша задача запланирована!");
	}

	private Update getUpdate(String json, String replaced) {
		return BotUtils.fromJson(json.replace("%command%", replaced), Update.class);
	}
//    private void addNotificationTask(LocalDateTime localDateTime,
//                                    String message,
//                                    Long userId) {
//        notificationTask notificationTask = new notificationTask();
//        notificationTask.setDateTime(localDateTime);
//        notificationTask.setMessage(message);
//        notificationTask.setChatId(userId);
//        Mockito.when(notificationTaskRepository.save(any())).thenReturn(notificationTask);
//    }

}
