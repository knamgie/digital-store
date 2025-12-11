package ru.fa.kobzar.mikhail.digitalstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для обработки страницы входа в систему.
 * <p>
 * Обрабатывает запросы к странице аутентификации пользователей.
 * Работает в связке с Spring Security для обеспечения процесса входа.
 * </p>
 *
 * <p><strong>Базовый путь:</strong> {@code /login}</p>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 */
@Controller
public class LoginController {
    /**
     * Отображает страницу входа в систему.
     * <p>
     * Обрабатывает GET-запрос по пути /login и возвращает представление
     * с формой аутентификации. Обработка данных формы выполняется Spring Security.
     * </p>
     *
     * @return имя представления "login"
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}
