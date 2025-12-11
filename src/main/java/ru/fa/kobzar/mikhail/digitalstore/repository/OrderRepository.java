package ru.fa.kobzar.mikhail.digitalstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.fa.kobzar.mikhail.digitalstore.entity.Order;
import ru.fa.kobzar.mikhail.digitalstore.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Order}.
 * <p>
 * Предоставляет CRUD операции и сложные фильтры для поиска заказов.
 * Включает методы для администраторов и пользователей с разными уровнями доступа.
 * </p>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see Order
 * @see User
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * Получение всех заказов конкретного пользователя.
     *
     * @param user пользователь, чьи заказы нужно получить
     * @return список заказов пользователя
     */
    List<Order> findByUser(User user);

    /**
     * Поиск заказов с применением множественных фильтров.
     * <p>
     * Метод поддерживает фильтрацию по:
     * <ul>
     *   <li>Email пользователя (частичное совпадение)</li>
     *   <li>ID пользователя (точное совпадение)</li>
     *   <li>Названию товара (частичное совпадение)</li>
     *   <li>ID товара (точное совпадение)</li>
     *   <li>Количеству товара (точное совпадение)</li>
     *   <li>Общей стоимости (точное совпадение)</li>
     *   <li>Статусу заказа</li>
     *   <li>Диапазону дат создания</li>
     *   <li>Диапазону дат обновления</li>
     * </ul>
     * Все параметры опциональны. Если параметр null, он не участвует в фильтрации.
     * </p>
     *
     * @param email фильтр по email пользователя (частичное совпадение, игнорирование регистра)
     * @param userId фильтр по ID пользователя (точное совпадение)
     * @param productName фильтр по названию товара (частичное совпадение, игнорирование регистра)
     * @param productId фильтр по ID товара (точное совпадение)
     * @param quantity фильтр по количеству товара (точное совпадение)
     * @param totalPrice фильтр по общей стоимости (точное совпадение)
     * @param status фильтр по статусу заказа
     * @param createdFrom начальная дата создания заказа (включительно)
     * @param createdTo конечная дата создания заказа (включительно)
     * @param updatedFrom начальная дата обновления заказа (включительно)
     * @param updatedTo конечная дата обновления заказа (включительно)
     * @return список заказов, соответствующих всем примененным фильтрам
     */
    @Query(
        "SELECT o FROM Order o "
            + "JOIN o.user u "
            + "JOIN o.product p "
            + "WHERE (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) "
            + "AND (:userId IS NULL OR u.id = :userId) "
            + "AND (:productName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))) "
            + "AND (:productId IS NULL OR p.id = :productId) "
            + "AND (:quantity IS NULL OR o.quantity = :quantity) "
            + "AND (:totalPrice IS NULL OR o.totalPrice = :totalPrice) "
            + "AND (:status IS NULL OR o.status = :status) "
            + "AND (:createdFrom IS NULL OR o.createdAt >= :createdFrom) "
            + "AND (:createdTo IS NULL OR o.createdAt <= :createdTo) "
            + "AND (:updatedFrom IS NULL OR o.updatedAt >= :updatedFrom) "
            + "AND (:updatedTo IS NULL OR o.updatedAt <= :updatedTo)"
    ) List<Order> findByFilters(
        @Param("email") String email,
        @Param("userId") Long userId,
        @Param("productName") String productName,
        @Param("productId") Long productId,
        @Param("quantity") Integer quantity,
        @Param("totalPrice") BigDecimal totalPrice,
        @Param("status") Order.Status status,
        @Param("createdFrom") LocalDateTime createdFrom,
        @Param("createdTo") LocalDateTime createdTo,
        @Param("updatedFrom") LocalDateTime updatedFrom,
        @Param("updatedTo") LocalDateTime updatedTo
    );
}
