package ru.fa.kobzar.mikhail.digitalstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.kobzar.mikhail.digitalstore.dto.CategoryDto;
import ru.fa.kobzar.mikhail.digitalstore.entity.Category;
import ru.fa.kobzar.mikhail.digitalstore.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервисный слой для управления категориями товаров.
 * <p>
 * Предоставляет бизнес-логику для операций CRUD с категориями,
 * включая валидацию уникальности названий и поиск.
 * Все операции записи выполняются в транзакциях.
 * </p>
 *
 * <p><strong>Бизнес-правила:</strong></p>
 * <ul>
 *   <li>Название категории должно быть уникальным</li>
 *   <li>Максимальная длина названия — 100 символов</li>
 *   <li>Максимальная длина описания — 1000 символов</li>
 *   <li>Удаление категории не проверяет наличие связанных товаров</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see Category
 * @see CategoryDto
 * @see CategoryRepository
 */
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /**
     * Возвращает список всех категорий в системе.
     * <p>
     * Метод выполняет поиск всех категорий, сортирует их по ID
     * и конвертирует в DTO для передачи на frontend.
     * </p>
     *
     * @return список CategoryDto, отсортированный по ID
     */
    public List<CategoryDto> getAllCategories() {
        return categoryRepository
            .findAll()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Находит категорию по ID.
     *
     * @param id уникальный идентификатор категории
     * @return DTO категории с указанным ID
     * @throws RuntimeException если категория не найдена
     */
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        return convertToDto(category);
    }

    /**
     * Создает новую категорию.
     * <p>
     * Перед созданием проверяет уникальность названия категории.
     * Если категория с таким названием уже существует, выбрасывает исключение.
     * </p>
     *
     * @param categoryDto DTO с данными новой категории
     * @return DTO созданной категории
     * @throws RuntimeException если категория с таким названием уже существует
     */
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new RuntimeException("Категория с таким названием уже существует");
        }

        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }

    /**
     * Обновляет существующую категорию.
     * <p>
     * Проверяет уникальность названия, если оно изменяется.
     * Обновляет имя и описание категории.
     * </p>
     *
     * @param id ID категории для обновления
     * @param categoryDto DTO с новыми данными
     * @return DTO обновленной категории
     * @throws RuntimeException если категория не найдена или новое название уже занято
     */
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        if (
            !category.getName().equals(categoryDto.getName())
                && categoryRepository.existsByName(categoryDto.getName())
        ) {
            throw new RuntimeException("Категория с таким названием уже существует");
        }

        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return convertToDto(updatedCategory);
    }

    /**
     * Удаляет категорию по ID.
     * <p>
     * Перед удалением проверяет существование категории.
     * Не проверяет наличие связанных товаров (каскадное удаление не настроено).
     * </p>
     *
     * @param id ID категории для удаления
     * @throws RuntimeException если категория не найдена
     */
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Категория не найдена");
        }
        categoryRepository.deleteById(id);
    }

    /**
     * Выполняет поиск категорий по части названия.
     * <p>
     * Поиск выполняется без учета регистра.
     * Если строка поиска пустая или null, возвращаются все категории.
     * </p>
     *
     * @param namePart часть названия для поиска
     * @return список найденных категорий, отсортированный по ID
     */
    public List<CategoryDto> searchCategories(String namePart) {
        String namePartFilter = (namePart == null || namePart.trim().isEmpty()) ? null : namePart;
        return categoryRepository
            .findByNameContainingIgnoreCase(namePartFilter)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Конвертирует сущность Category в DTO.
     * <p>
     * Используется для скрытия деталей реализации и передачи
     * только необходимых данных на frontend.
     * </p>
     *
     * @param category сущность категории
     * @return DTO категории
     */
    private CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}
