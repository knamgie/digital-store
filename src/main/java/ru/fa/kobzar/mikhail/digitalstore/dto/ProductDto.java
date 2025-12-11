package ru.fa.kobzar.mikhail.digitalstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO для передачи данных товара.
 * <p>
 * Используется для валидации и передачи информации о товарах.
 * Содержит основные характеристики товара и ссылку на категорию.
 * </p>
 *
 * <p><strong>Валидация:</strong></p>
 * <ul>
 *   <li>name - обязательное, максимум 100 символов</li>
 *   <li>brand - необязательное, максимум 100 символов</li>
 *   <li>categoryId - обязательное поле</li>
 *   <li>price - обязательное, неотрицательное</li>
 *   <li>quantity - обязательное, неотрицательное</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see ru.fa.kobzar.mikhail.digitalstore.entity.Product
 * @see ru.fa.kobzar.mikhail.digitalstore.service.ProductService
 */
@Data
public class ProductDto {
    /**
     * Уникальный идентификатор товара.
     */
    private Long id;

    /**
     * Название товара.
     * <p>
     * Обязательное поле. Должно быть уникальным в системе.
     * </p>
     */
    @NotBlank(message = "Название товара обязательно")
    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    /**
     * Бренд производителя товара.
     * <p>
     * Необязательное поле.
     * </p>
     */
    @Size(max = 100, message = "Бренд не должен превышать 100 символов")
    private String brand;

    /**
     * Идентификатор категории товара.
     * <p>
     * Обязательное поле. Должен ссылаться на существующую категорию.
     * </p>
     */
    @NotNull(message = "Категория обязательна")
    private Long categoryId;

    /**
     * Название категории (денормализованное поле для UI).
     */
    private String categoryName;

    /**
     * Цена товара.
     * <p>
     * Обязательное поле. Должна быть неотрицательной.
     * </p>
     */
    @NotNull(message = "Цена товара обязательна")
    @Min(value = 0, message = "Цена товара не может быть отрицательной")
    private BigDecimal price;

    /**
     * Количество товара на складе.
     * <p>
     * Обязательное поле. Должно быть неотрицательным.
     * </p>
     */
    @NotNull(message = "Количество товара обязательно")
    @Min(value = 0, message = "Количество товара не может быть отрицательным")
    private Integer quantity;
}
