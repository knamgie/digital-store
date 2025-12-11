package ru.fa.kobzar.mikhail.digitalstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность, представляющая заказ пользователя в электронном магазине.
 * <p>
 * Класс содержит полную информацию о заказе, включая данные о покупателе,
 * товаре, количестве, стоимости и статусе выполнения заказа.
 * </p>
 *
 * <p><strong>Бизнес-правила:</strong></p>
 * <ul>
 *   <li>Каждый заказ привязан к конкретному пользователю и товару</li>
 *   <li>Количество товара в заказе должно быть строго больше 0</li>
 *   <li>Общая стоимость рассчитывается как цена товара × количество</li>
 *   <li>Статус заказа изменяется по мере обработки</li>
 *   <li>Время создания фиксируется автоматически и не изменяется</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see User
 * @see Product
 */
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    /**
     * Уникальный идентификатор заказа.
     * <p>
     * Генерируется автоматически при создании записи в БД.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Пользователь, оформивший заказ.
     * <p>
     * Обязательное поле. Ссылка на сущность {@link User}.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Товар, включенный в заказ.
     * <p>
     * Обязательное поле. Ссылка на сущность {@link Product}.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Количество единиц товара в заказе.
     * <p>
     * Обязательное поле. Должно быть больше 0.
     * </p>
     */
    @NotNull(message = "Количество товара обязательно")
    @Min(value = 1, message = "Количество должно быть больше 0")
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Общая стоимость заказа.
     * <p>
     * Хранится в формате DECIMAL(10,2). Рассчитывается как цена товара × количество.
     * </p>
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Текущий статус заказа.
     * <p>
     * По умолчанию при создании устанавливается {@link Status#NEW}.
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.NEW;

    /**
     * Дата и время создания заказа.
     * <p>
     * Устанавливается автоматически при создании записи и не обновляется.
     * </p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления заказа.
     * <p>
     * Изменяется при каждом изменении статуса или других параметров заказа.
     * </p>
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Перечисление возможных статусов заказа.
     * <p>
     * Определяет жизненный цикл заказа от создания до завершения.
     * </p>
     */
    public enum Status {
        /** Новый заказ, ожидает обработки */
        NEW,

        /** Заказ принят в работу менеджером */
        ACCEPTED,

        /** Товар передан в службу доставки */
        IN_TRANSIT,

        /** Заказ доставлен клиенту */
        DELIVERED,

        /** Заказ отменен */
        CANCELLED
    }
}