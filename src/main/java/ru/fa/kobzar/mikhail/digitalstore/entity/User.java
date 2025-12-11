package ru.fa.kobzar.mikhail.digitalstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Сущность, представляющая пользователя системы электронного магазина.
 * <p>
 * Класс реализует интерфейс {@link UserDetails} для интеграции с Spring Security.
 * Поддерживает аутентификацию и авторизацию на основе ролей.
 * </p>
 *
 * <p><strong>Безопасность:</strong></p>
 * <ul>
 *   <li>Email используется в качестве уникального имени пользователя</li>
 *   <li>Пароль хранится в зашифрованном виде (минимум 6 символов)</li>
 *   <li>Роли: CLIENT, MANAGER, ADMIN</li>
 *   <li>Аккаунт всегда активен, не блокируется и не истекает</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see UserDetails
 * @see Role
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {
    /**
     * Уникальный идентификатор пользователя.
     * <p>
     * Генерируется автоматически при создании записи в БД.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email пользователя, используется как логин.
     * <p>
     * Обязательное поле. Должно быть валидным email-адресом и уникальным в системе.
     * </p>
     */
    @Email(message = "Email должен быть валидным")
    @NotBlank(message = "Email обязателен")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Имя пользователя.
     * <p>
     * Необязательное поле. Максимальная длина — 50 символов.
     * </p>
     */
    @Column(name = "first_name", length = 50)
    private String firstName;

    /**
     * Фамилия пользователя.
     * <p>
     * Необязательное поле. Максимальная длина — 50 символов.
     * </p>
     */
    @Column(name = "last_name", length = 50)
    private String lastName;

    /**
     * Роль пользователя в системе.
     * <p>
     * Определяет права доступа. По умолчанию устанавливается {@link Role#CLIENT}.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.CLIENT;

    /**
     * Хэш пароля пользователя.
     * <p>
     * Обязательное поле. Минимальная длина — 6 символов в открытом виде.
     * Хранится в зашифрованном виде (BCrypt).
     * </p>
     */
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    @Column(nullable = false)
    private String password;

    /**
     * Дата и время создания учетной записи.
     * <p>
     * Устанавливается автоматически при создании записи и не обновляется.
     * </p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления учетной записи.
     * <p>
     * Изменяется при каждом изменении данных пользователя.
     * </p>
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Перечисление ролей пользователей в системе.
     * <p>
     * Используется для реализации RBAC (Role-Based Access Control).
     * </p>
     */
    public enum Role {
        /** Обычный клиент магазина */
        CLIENT,

        /** Менеджер магазина, может управлять заказами и товарами */
        MANAGER,

        /** Администратор системы, имеет полный доступ */
        ADMIN,
    }

    /**
     * Возвращает список прав (ролей) пользователя для Spring Security.
     *
     * @return коллекция объектов {@link GrantedAuthority}
     * @see SimpleGrantedAuthority
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Возвращает имя пользователя, используемое для аутентификации.
     * <p>
     * В данной реализации возвращает email.
     * </p>
     *
     * @return email пользователя
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Указывает, не истек ли срок действия аккаунта.
     * <p>
     * Всегда возвращает {@code true} (аккаунт не истекает).
     * </p>
     *
     * @return {@code true} — аккаунт активен
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Указывает, не заблокирован ли аккаунт.
     * <p>
     * Всегда возвращает {@code true} (аккаунт не блокируется).
     * </p>
     *
     * @return {@code true} — аккаунт не заблокирован
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Указывает, не истек ли срок действия учетных данных (пароля).
     * <p>
     * Всегда возвращает {@code true} (пароль не истекает).
     * </p>
     *
     * @return {@code true} — учетные данные действительны
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Указывает, включен ли аккаунт.
     * <p>
     * Всегда возвращает {@code true} (аккаунт всегда активен).
     * </p>
     *
     * @return {@code true} — аккаунт включен
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
