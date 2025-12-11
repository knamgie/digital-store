package ru.fa.kobzar.mikhail.digitalstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO (Data Transfer Object) для передачи данных категории товаров.
 * <p>
 * Используется для валидации входных данных при создании и обновлении категорий.
 * Содержит основную информацию о категории без дополнительных связей.
 * </p>
 *
 * <p><strong>Валидация:</strong></p>
 * <ul>
 *   <li>Название категории - обязательное поле, максимум 100 символов</li>
 *   <li>Описание - необязательное поле, максимум 1000 символов</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see ru.fa.kobzar.mikhail.digitalstore.entity.Category
 * @see ru.fa.kobzar.mikhail.digitalstore.service.CategoryService
 */
@Data
public class CategoryDto {
    /**
     * Уникальный идентификатор категории.
     * <p>
     * Может быть null при создании новой категории.
     * </p>
     */
    private Long id;

    /**
     * Название категории товара.
     * <p>
     * Обязательное поле. Должно быть уникальным в системе.
     * </p>
     */
    @NotBlank(message = "Название категории обязательно")
    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    /**
     * Описание категории товара.
     * <p>
     * Необязательное поле. Может содержать форматированный текст.
     * </p>
     */
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;
}
