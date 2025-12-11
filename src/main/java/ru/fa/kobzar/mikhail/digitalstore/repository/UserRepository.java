package ru.fa.kobzar.mikhail.digitalstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.fa.kobzar.mikhail.digitalstore.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * <p>
 * Предоставляет CRUD операции и методы поиска пользователей с различными фильтрами.
 * Реализует интеграцию с Spring Security через {@link org.springframework.security.core.userdetails.UserDetailsService}.
 * </p>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see User
 * @see org.springframework.security.core.userdetails.UserDetailsService
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Поиск пользователя по email.
     * <p>
     * Используется для аутентификации в Spring Security.
     * </p>
     *
     * @param email email пользователя
     * @return Optional с найденным пользователем или пустой, если не найден
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверка существования пользователя с указанным email.
     *
     * @param email email для проверки
     * @return true, если пользователь с таким email существует
     */
    boolean existsByEmail(String email);

    /**
     * Поиск пользователей с применением множественных фильтров.
     * <p>
     * Метод поддерживает фильтрацию по:
     * <ul>
     *   <li>Email (частичное совпадение)</li>
     *   <li>Имени (частичное совпадение)</li>
     *   <li>Фамилии (частичное совпадение)</li>
     *   <li>Роли</li>
     *   <li>Диапазону дат создания</li>
     *   <li>Диапазону дат обновления</li>
     * </ul>
     * Все параметры опциональны. Если параметр null, он не участвует в фильтрации.
     * </p>
     *
     * @param email фильтр по email (частичное совпадение, игнорирование регистра)
     * @param firstName фильтр по имени (частичное совпадение, игнорирование регистра)
     * @param lastName фильтр по фамилии (частичное совпадение, игнорирование регистра)
     * @param role фильтр по роли пользователя
     * @param createdFrom начальная дата создания учетной записи (включительно)
     * @param createdTo конечная дата создания учетной записи (включительно)
     * @param updatedFrom начальная дата обновления учетной записи (включительно)
     * @param updatedTo конечная дата обновления учетной записи (включительно)
     * @return список пользователей, соответствующих всем примененным фильтрам
     */
    @Query(
        "SELECT u FROM User u WHERE "
            + "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND "
            + "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND "
            + "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND "
            + "(:role IS NULL OR u.role = :role) AND "
            + "(:createdFrom IS NULL OR u.createdAt >= :createdFrom) AND "
            + "(:createdTo IS NULL OR u.createdAt <= :createdTo) AND "
            + "(:updatedFrom IS NULL OR u.updatedAt >= :updatedFrom) AND "
            + "(:updatedTo IS NULL OR u.updatedAt <= :updatedTo)"
    ) List<User> findByFilters(
        @Param("email") String email,
        @Param("firstName") String firstName,
        @Param("lastName") String lastName,
        @Param("role") User.Role role,
        @Param("createdFrom") LocalDateTime createdFrom,
        @Param("createdTo") LocalDateTime createdTo,
        @Param("updatedFrom") LocalDateTime updatedFrom,
        @Param("updatedTo") LocalDateTime updatedTo
    );
}
