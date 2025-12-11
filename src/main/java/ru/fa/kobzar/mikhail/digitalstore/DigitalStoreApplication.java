package ru.fa.kobzar.mikhail.digitalstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс Spring Boot приложения "Digital Store".
 * <p>
 * Точка входа в электронную коммерческую систему, реализующую управление
 * товарами, категориями, заказами и пользователями с ролевой моделью доступа.
 * </p>
 *
 * <p><strong>Основные возможности:</strong></p>
 * <ul>
 *   <li>Аутентификация и авторизация на базе Spring Security</li>
 *   <li>Ролевой доступ (CLIENT, MANAGER, ADMIN)</li>
 *   <li>Управление каталогом товаров и категориями</li>
 *   <li>Обработка заказов с контролем жизненного цикла</li>
 *   <li>Панель администратора для управления пользователями</li>
 *   <li>Фильтрация данных на всех уровнях</li>
 * </ul>
 *
 * <p><strong>Технологии:</strong></p>
 * <ul>
 *   <li>Spring Boot — основа приложения</li>
 *   <li>Spring Security — безопасность</li>
 *   <li>Spring Data JPA — доступ к данным</li>
 *   <li>Thymeleaf — шаблонизатор представлений</li>
 *   <li>BCrypt — шифрование паролей</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class DigitalStoreApplication {
    /**
     * Точка входа в приложение.
     * <p>
     * Запускает Spring Boot приложение, инициализирует контекст,
     * настраивает все компоненты и запускает встроенный сервер.
     * </p>
     *
     * @param args аргументы командной строки
     */
	public static void main(String[] args) {
		SpringApplication.run(DigitalStoreApplication.class, args);
	}
}
