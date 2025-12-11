package ru.fa.kobzar.mikhail.digitalstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.fa.kobzar.mikhail.digitalstore.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для передачи данных заказа.
 * <p>
 * Используется для валидации и передачи информации о заказах между слоями приложения.
 * Содержит как идентификаторы связанных сущностей, так и денормализованные данные
 * для отображения в UI.
 * </p>
 *
 * <p><strong>Валидация:</strong></p>
 * <ul>
 *   <li>userId - обязательное поле</li>
 *   <li>productId - обязательное поле</li>
 *   <li>quantity - обязательное, минимум 1</li>
 *   <li>status - обязательное поле</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see ru.fa.kobzar.mikhail.digitalstore.entity.Order
 * @see ru.fa.kobzar.mikhail.digitalstore.service.OrderService
 */
@Data
public class OrderDto {
    /**
     * Уникальный идентификатор заказа.
     */
    private Long id;

    /**
     * Идентификатор пользователя, оформившего заказ.
     * <p>
     * Обязательное поле для создания заказа.
     * </p>
     */
    @NotNull(message = "Пользователь обязателен")
    private Long userId;

    /**
     * Email пользователя (денормализованное поле для UI).
     * <p>
     * Заполняется автоматически при получении данных из сервиса.
     * </p>
     */
    private String userEmail;

    /**
     * Полное имя пользователя (денормализованное поле для UI).
     */
    private String userFullName;

    /**
     * Идентификатор товара в заказе.
     * <p>
     * Обязательное поле для создания заказа.
     * </p>
     */
    @NotNull(message = "Товар обязателен")
    private Long productId;

    /**
     * Название товара (денормализованное поле для UI).
     */
    private String productName;

    /**
     * Бренд товара (денормализованное поле для UI).
     */
    private String productBrand;

    /**
     * Количество единиц товара в заказе.
     * <p>
     * Обязательное поле. Должно быть больше 0.
     * </p>
     */
    @NotNull(message = "Количество товара обязательно")
    @Min(value = 1, message = "Количество должно быть больше 0")
    private Integer quantity;

    /**
     * Цена за единицу товара (денормализованное поле для UI).
     */
    private BigDecimal unitPrice;

    /**
     * Общая стоимость заказа.
     * <p>
     * Рассчитывается как unitPrice × quantity.
     * </p>
     */
    private BigDecimal totalPrice;

    /**
     * Текущий статус заказа.
     * <p>
     * Обязательное поле. По умолчанию NEW.
     * </p>
     */
    @NotNull(message = "Статус обязателен")
    private Order.Status status;

    /**
     * Дата и время создания заказа.
     */
    private LocalDateTime createdAt;

    /**
     * Дата и время создания заказа.
     */
    private LocalDateTime updatedAt;
}
