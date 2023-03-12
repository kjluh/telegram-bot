package pro.sky.telegrambot.listener.repositorty;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.telegrambot.listener.model.notificationTask;

import java.time.LocalDateTime;

public interface notificationTaskRepository extends JpaRepository<notificationTask,Long> {

    notificationTask findByDateTime(LocalDateTime localDate);

}
