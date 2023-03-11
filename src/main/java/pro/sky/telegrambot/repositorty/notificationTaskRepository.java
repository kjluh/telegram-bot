package pro.sky.telegrambot.repositorty;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.telegrambot.model.notificationTask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface notificationTaskRepository extends JpaRepository<notificationTask,Long> {

    notificationTask findByDateTime(LocalDateTime localDate);

}
