package ru.fa.kobzar.mikhail.digitalstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Сущность, представляющая товар в электронном магазине.
 * <p>
 * Класс содержит полную информацию о товаре, включая категорию, цену,
 * количество на складе и бренд производителя.
 * </p>
 *
 * <p><strong>Бизнес-правила:</strong></p>
 * <ul>
 *   <li>Название товара является обязательным полем</li>
 *   <li>Товар должен принадлежать к одной из существующих категорий</li>
 *   <li>Цена товара не может быть отрицательной</li>
 *   <li>Количество товара на складе не может быть отрицательным</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see Category
 * @see Order
 */
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    /**
     * Уникальный идентификатор товара.
     * <p>
     * Генерируется автоматически при создании записи в БД.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название товара.
     * <p>
     * Обязательное поле, не может быть пустым или содержать только пробелы.
     * </p>
     */
    @NotBlank(message = "Название товара обязательно")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Бренд производителя товара.
     * <p>
     * Необязательное поле, может быть пустым.
     * </p>
     */
    @Column(length = 100)
    private String brand;

    /**
     * Категория, к которой относится товар.
     * <p>
     * Обязательное поле. Ссылка на сущность {@link Category}.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Цена товара.
     * <p>
     * Обязательное поле. Хранится в формате DECIMAL(10,2).
     * Должна быть неотрицательной.
     * </p>
     */
    @NotNull(message = "Цена товара обязательна")
    @Min(value = 0, message = "Цена товара не может быть отрицательной")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Количество товара на складе.
     * <p>
     * Обязательное поле. Должно быть неотрицательным.
     * </p>
     */
    @NotNull(message = "Количество товара обязательно")
    @Min(value = 0, message = "Количество товара не может быть отрицательным")
    @Column(nullable = false)
    private Integer quantity;
}
