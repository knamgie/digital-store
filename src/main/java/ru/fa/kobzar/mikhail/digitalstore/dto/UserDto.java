package ru.fa.kobzar.mikhail.digitalstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.fa.kobzar.mikhail.digitalstore.entity.User;

/**
 * DTO для передачи данных пользователя.
 * <p>
 * Используется для валидации и передачи информации о пользователях.
 * Поддерживает операции регистрации, создания и обновения пользователей.
 * </p>
 *
 * <p><strong>Валидация:</strong></p>
 * <ul>
 *   <li>email - обязательное, валидный email-адрес</li>
 *   <li>firstName - необязательное, максимум 50 символов</li>
 *   <li>lastName - необязательное, максимум 50 символов</li>
 *   <li>role - роль пользователя в системе</li>
 *   <li>password - минимум 6 символов (только при создании/обновлении)</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see ru.fa.kobzar.mikhail.digitalstore.entity.User
 * @see ru.fa.kobzar.mikhail.digitalstore.service.UserService
 */
@Data
public class UserDto {
    /**
     * Уникальный идентификатор пользователя.
     */
    private Long id;

    /**
     * Email пользователя, используется как логин.
     * <p>
     * Обязательное поле. Должно быть уникальным в системе.
     * </p>
     */
    @Email(message = "Email должен быть валидным")
    @NotBlank(message = "Email обязателен")
    private String email;

    /**
     * Имя пользователя.
     * <p>
     * Необязательное поле.
     * </p>
     */
    private String firstName;

    /**
     * Фамилия пользователя.
     * <p>
     * Необязательное поле.
     * </p>
     */
    private String lastName;

    /**
     * Роль пользователя в системе.
     * <p>
     * Определяет права доступа. По умолчанию CLIENT.
     * </p>
     */
    private User.Role role;

    /**
     * Пароль пользователя.
     * <p>
     * Минимум 6 символов. Хранится в открытом виде только при передаче,
     * в БД сохраняется в зашифрованном виде (BCrypt).
     * </p>
     */
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;
}
