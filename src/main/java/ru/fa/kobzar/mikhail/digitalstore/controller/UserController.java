package ru.fa.kobzar.mikhail.digitalstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.fa.kobzar.mikhail.digitalstore.dto.UserDto;
import ru.fa.kobzar.mikhail.digitalstore.entity.User;
import ru.fa.kobzar.mikhail.digitalstore.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Контроллер для управления пользователями.
 * <p>
 * Обрабатывает HTTP-запросы для операций CRUD с пользователями,
 * включая администрирование и управление персональным профилем.
 * Реализует разграничение доступа между обычными пользователями и администраторами.
 * </p>
 *
 * <p><strong>Базовый путь:</strong> {@code /users}</p>
 *
 * <p><strong>Доступ:</strong></p>
 * <ul>
 *   <li>Профиль (/profile) — доступен аутентифицированным пользователям (свой профиль)</li>
 *   <li>Управление пользователями — только роль ADMIN</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see UserService
 * @see UserDto
 */
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Отображает список всех пользователей с возможностью фильтрации.
     * <p>
     * Доступно только для администраторов. Поддерживает фильтрацию
     * по email, имени, фамилии, роли и диапазонам дат создания/обновления.
     * </p>
     *
     * @param email email для фильтрации
     * @param firstName имя для фильтрации
     * @param lastName фамилия для фильтрации
     * @param role роль для фильтрации
     * @param createdFrom начальная дата создания учетной записи
     * @param createdTo конечная дата создания учетной записи
     * @param updatedFrom начальная дата обновления учетной записи
     * @param updatedTo конечная дата обновления учетной записи
     * @return ModelAndView с отфильтрованным списком пользователей и представлением "users"
     * @throws RuntimeException если произошла ошибка при загрузке пользователей
     */
    @GetMapping
    public ModelAndView listUsers(
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String lastName,
        @RequestParam(required = false) User.Role role,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdFrom,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdTo,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate updatedFrom,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate updatedTo
    ) {
        ModelAndView mav = new ModelAndView("users/users");

        LocalDateTime createdFromDt = createdFrom != null ? createdFrom.atStartOfDay() : null;
        LocalDateTime createdToDt = createdTo != null ? createdTo.plusDays(1).atStartOfDay().minusNanos(1) : null;
        LocalDateTime updatedFromDt = updatedFrom != null ? updatedFrom.atStartOfDay() : null;
        LocalDateTime updatedToDt = updatedTo != null ? updatedTo.plusDays(1).atStartOfDay().minusNanos(1) : null;

        var users = userService.searchUsers(
            email,
            firstName,
            lastName,
            role,
            createdFromDt,
            createdToDt,
            updatedFromDt,
            updatedToDt
        );

        mav.addObject("users", users);
        mav.addObject("roles", User.Role.values());
        return mav;
    }

    /**
     * Обрабатывает создание нового пользователя (админом).
     * <p>
     * Принимает данные из формы, валидирует их через сервис.
     * При успешном создании перенаправляет на список пользователей.
     * При ошибке возвращает на форму с сообщением об ошибке.
     * </p>
     *
     * @param userDto DTO с данными нового пользователя
     * @param model модель для передачи данных при ошибке
     * @return строку перенаправления на /users или имя представления "user-form" при ошибке
     * @throws RuntimeException если email уже занят
     */
    @PostMapping
    public String createUser(@ModelAttribute UserDto userDto, Model model) {
        try {
            userService.createUser(userDto);
            return "redirect:/users";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userDto", userDto);
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("title", "Создание пользователя");
            model.addAttribute("action", "/users");
            return "user-form";
        }
    }

    /**
     * Отображает персональный профиль текущего пользователя.
     * <p>
     * Получает данные аутентифицированного пользователя, формирует DTO
     * и отображает страницу профиля. Каждый пользователь видит только свой профиль.
     * </p>
     *
     * @return ModelAndView с данными профиля и представлением "profile"
     * @throws RuntimeException если пользователь не найден
     */
    @GetMapping("/profile")
    public ModelAndView showProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setRole(user.getRole());

        ModelAndView mav = new ModelAndView("users/profile");
        mav.addObject("userDto", userDto);
        mav.addObject("user", user);
        return mav;
    }

    /**
     * Отображает форму создания нового пользователя.
     * <p>
     * Подготавливает модель с пустым объектом UserDto,
     * списком всех ролей и данными для формы.
     * </p>
     *
     * @return ModelAndView с формой создания пользователя и представлением "user-form"
     */
    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        ModelAndView mav = new ModelAndView("users/user-form");
        mav.addObject("userDto", new UserDto());
        mav.addObject("roles", User.Role.values());
        mav.addObject("title", "Создание пользователя");
        mav.addObject("action", "/users");
        return mav;
    }

    /**
     * Отображает форму редактирования пользователя.
     * <p>
     * Получает пользователя по ID, конвертирует в DTO и подготавливает
     * модель с данными пользователя для отображения формы редактирования.
     * </p>
     *
     * @param id идентификатор пользователя для редактирования
     * @return ModelAndView с данными пользователя и представлением "user-form"
     * @throws RuntimeException если пользователь не найден
     */
    @GetMapping("/{id}/edit")
    public ModelAndView showEditForm(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setRole(user.getRole());

        ModelAndView mav = new ModelAndView("users/user-form");
        mav.addObject("userDto", userDto);
        mav.addObject("roles", User.Role.values());
        mav.addObject("title", "Редактирование пользователя");
        mav.addObject("action", "/users/" + id + "/update");
        return mav;
    }

    /**
     * Обрабатывает обновление существующего пользователя (админом).
     * <p>
     * Проверяет уникальность email, если он изменяется. При необходимости
     * перекодирует пароль. После обновления перенаправляет на список пользователей.
     * Если изменен email текущего пользователя, перезагружает контекст безопасности.
     * </p>
     *
     * @param id идентификатор обновляемого пользователя
     * @param userDto DTO с новыми данными пользователя
     * @param model модель для передачи данных при ошибке
     * @return строку перенаправления на /users или имя представления "user-form" при ошибке
     * @throws RuntimeException если пользователь не найден или email уже занят
     */
    @PostMapping("/{id}/update")
    public String updateUser(@PathVariable Long id, @ModelAttribute UserDto userDto, Model model) {
        try {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User userToUpdate = userService.getUserById(id);
            userService.updateUser(id, userDto);
            if (currentUserEmail.equals(userToUpdate.getEmail())) {
                userService.reloadUserAuthentication(currentUserEmail);
            }
            return "redirect:/users";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userDto", userDto);
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("title", "Редактирование пользователя");
            model.addAttribute("action", "/users/" + id + "/update");
            return "user-form";
        }
    }

    /**
     * Обрабатывает обновление профиля текущего пользователя.
     * <p>
     * Позволяет пользователю изменить только свой профиль.
     * Проверяет уникальность нового email. При изменении email
     * перезагружает контекст безопасности для обновления сессии.
     * </p>
     *
     * @param userDto DTO с новыми данными профиля
     * @param model модель для передачи данных при ошибке
     * @return строку перенаправления на /users/profile?updated или имя представления "profile" при ошибке
     * @throws RuntimeException если попытка изменить чужой профиль или email уже занят
     */
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UserDto userDto, Model model) {
        try {
            String currentUserEmail = Objects
                .requireNonNull(SecurityContextHolder.getContext().getAuthentication())
                .getName();
            User userToUpdate = userService.getUserById(userDto.getId());

            if (!userToUpdate.getEmail().equals(currentUserEmail)) {
                throw new RuntimeException("Вы можете редактировать только свой профиль");
            }

            User updatedUser = userService.updateProfile(userDto.getId(), userDto, currentUserEmail);
            userService.reloadUserAuthentication(updatedUser.getEmail());
            return "redirect:/users/profile?updated";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userDto", userDto);
            User user = userService.getUserById(userDto.getId());
            model.addAttribute("user", user);
            return "profile";
        }
    }

    /**
     * Обрабатывает удаление пользователя.
     * <p>
     * Удаляет пользователя по ID и перенаправляет на список пользователей.
     * </p>
     *
     * @param id идентификатор удаляемого пользователя
     * @return строку перенаправления на /users
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }
}
