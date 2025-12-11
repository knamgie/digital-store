package ru.fa.kobzar.mikhail.digitalstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.fa.kobzar.mikhail.digitalstore.dto.UserDto;
import ru.fa.kobzar.mikhail.digitalstore.entity.User;
import ru.fa.kobzar.mikhail.digitalstore.service.UserService;

/**
 * Контроллер для регистрации новых пользователей.
 * <p>
 * Обрабатывает запросы на самостоятельную регистрацию клиентов в системе.
 * Реализует функциональность создания учетных записей с ролью CLIENT.
 * </p>
 *
 * <p><strong>Базовые пути:</strong> {@code /register}, {@code /registration}</p>
 *
 * <p><strong>Особенности:</strong></p>
 * <ul>
 *   <li>При регистрации с email admin@digital.store назначается роль ADMIN</li>
 *   <li>Пароль автоматически шифруется перед сохранением</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see UserService
 * @see UserDto
 */
@Controller
@RequiredArgsConstructor
public class RegisterController {
    private final UserService userService;

    /**
     * Отображает форму регистрации.
     * <p>
     * Подготавливает модель с пустым объектом UserDto
     * для отображения формы регистрации.
     * </p>
     *
     * @return ModelAndView с формой регистрации и представлением "registration"
     */
    @GetMapping("/register")
    public ModelAndView showRegistrationForm() {
        ModelAndView mav = new ModelAndView("auth/registration");
        mav.addObject("userDto", new UserDto());
        return mav;
    }

    /**
     * Обрабатывает регистрацию нового пользователя.
     * <p>
     * Принимает данные из формы, валидирует их через сервис.
     * При успешной регистрации перенаправляет на страницу входа с параметром success.
     * При ошибке возвращает на форму с сообщением об ошибке.
     * </p>
     *
     * @param userDto DTO с данными регистрации
     * @param model модель для передачи данных при ошибке
     * @return строку перенаправления на /login?registered или имя представления "registration" при ошибке
     * @throws RuntimeException если пользователь с таким email уже существует
     */
    @PostMapping("/registration")
    public String registerUser(@ModelAttribute UserDto userDto, Model model) {
        try {
            userService.registerUser(userDto);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userDto", userDto);
            return "registration";
        }
    }
}
