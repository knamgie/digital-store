package ru.fa.kobzar.mikhail.digitalstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности Spring Security для электронного магазина.
 * <p>
 * Класс определяет основные правила аутентификации и авторизации,
 * настройки формы входа, шифрования паролей и доступа к ресурсам.
 * Реализует Role-Based Access Control (RBAC) с тремя уровнями доступа:
 * CLIENT, MANAGER и ADMIN.
 * </p>
 *
 * <p><strong>Основные возможности:</strong></p>
 * <ul>
 *   <li>Хеширование паролей с помощью BCrypt</li>
 *   <li>Форма аутентификации с перенаправлением</li>
 *   <li>Разграничение доступа по URL и HTTP-методам</li>
 *   <li>Публичный доступ к статическим ресурсам (CSS, JS)</li>
 *   <li>Автоматический logout с перенаправлением</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see org.springframework.security.web.SecurityFilterChain
 * @see org.springframework.security.crypto.password.PasswordEncoder
 */
@Configuration
public class SecurityConfig {
    /**
     * Создает бин для хеширования паролей с использованием алгоритма BCrypt.
     * <p>
     * Используется при создании и обновлении пользователей, а также
     * при аутентификации в Spring Security.
     * </p>
     *
     * @return экземпляр BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Конфигурирует цепочку фильтров безопасности для HTTP-запросов.
     * <p>
     * Определяет следующие правила авторизации:
     * </p>
     * <ul>
     *   <li><strong>Публичный доступ:</strong> главная страница, логин, регистрация,
     *   страница автора, статические ресурсы</li>
     *   <li><strong>Чтение категорий:</strong> GET-запросы к /categories доступны всем</li>
     *   <li><strong>Чтение товаров:</strong> GET-запросы к /products доступны всем</li>
     *   <li><strong>Профиль пользователя:</strong> аутентифицированным пользователям</li>
     *   <li><strong>Управление пользователями:</strong> только ADMIN</li>
     *   <li><strong>Управление категориями:</strong> MANAGER и ADMIN</li>
     *   <li><strong>Управление товарами:</strong> MANAGER и ADMIN (POST, PUT, DELETE)</li>
     *   <li><strong>Управление заказами:</strong> аутентифицированные пользователи
     *   могут создавать заказы; MANAGER и ADMIN имеют полный доступ</li>
     * </ul>
     *
     * <p><strong>Аутентификация:</strong></p>
     * <ul>
     *   <li>Кастомная страница логина по пути /login</li>
     *   <li>Перенаправление на /products после успешного входа</li>
     *   <li>Автоматический logout с перенаправлением на /login?logout</li>
     * </ul>
     *
     * @param http объект для настройки безопасности HTTP-запросов
     * @return сконфигурированная цепочка фильтров безопасности
     * @throws Exception если конфигурация не удалась
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/registration", "/author", "/css/**", "/js/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/categories", "/categories/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
                .requestMatchers("/users/profile", "/users/profile/update").authenticated()
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers("/categories/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/products/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/products/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/products/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/orders", "/orders/create", "/orders/*/edit-status", "/orders/*/update-status").authenticated()
                .requestMatchers("/orders/**").hasAnyRole("MANAGER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/products", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
}