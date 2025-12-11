package ru.fa.kobzar.mikhail.digitalstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Сущность, представляющая категорию товаров в электронном магазине.
 * <p>
 * Класс определяет структуру данных для хранения информации о категориях
 * продуктов, включая уникальное название и описание.
 * </p>
 *
 * <p><strong>Основные правила:</strong></p>
 * <ul>
 *   <li>Название категории является обязательным полем</li>
 *   <li>Название должно быть уникальным в системе</li>
 *   <li>Максимальная длина названия — 100 символов</li>
 *   <li>Описание может содержать произвольный текст</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see Product
 */
@Setter
@Getter
@Entity
@Table(name = "categories")
public class Category {
    /**
     * Уникальный идентификатор категории.
     * <p>
     * Генерируется автоматически при создании записи в БД.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название категории товара.
     * <p>
     * Обязательное поле, не может быть пустым или содержать только пробелы.
     * Должно быть уникальным в системе.
     * </p>
     */
    @NotBlank(message = "Название категории обязательно")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Описание категории товара.
     * <p>
     * Может содержать форматированный текст с подробной информацией о категории.
     * </p>
     */
    @Column(columnDefinition = "TEXT")
    private String description;
}
