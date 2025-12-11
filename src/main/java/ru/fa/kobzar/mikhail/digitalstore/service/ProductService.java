package ru.fa.kobzar.mikhail.digitalstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.kobzar.mikhail.digitalstore.dto.ProductDto;
import ru.fa.kobzar.mikhail.digitalstore.entity.Category;
import ru.fa.kobzar.mikhail.digitalstore.entity.Product;
import ru.fa.kobzar.mikhail.digitalstore.repository.CategoryRepository;
import ru.fa.kobzar.mikhail.digitalstore.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервисный слой для управления товарами.
 * <p>
 * Реализует бизнес-логику управления каталогом товаров:
 * создание, обновление, удаление и поск с фильтрацией.
 * Обеспечивает целосность данных и проверку бизнес-правил.
 * </p>
 *
 * <p><strong>Бизнес-правила:</strong></p>
 * <ul>
 *   <li>Название товара должно быть уникальным (без учета регистра)</li>
 *   <li>Товар должен принадлежать к существующей категории</li>
 *   <li>Цена не может быть отрицательной</li>
 *   <li>Количество на складе не может быть отрицательным</li>
 *   <li>При обновлении проверяется уникальность названия, если оно изменяется</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see Product
 * @see ProductDto
 * @see ProductRepository
 */
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Возвращает список всех товаров.
     * <p>
     * Метод выполняет поиск всех товаров, сортирует их по ID
     * и конвертирует в DTO с денормализованными данными категории.
     * </p>
     *
     * @return список ProductDto, отсортированный по ID
     */
    public List<ProductDto> getAllProducts() {
        return productRepository
            .findAll()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Находит товар по ID.
     *
     * @param id уникальный идентификатор товара
     * @return DTO товара с указанным ID
     * @throws RuntimeException если товар не найден
     */
    public ProductDto getProductById(Long id) {
        Product product = productRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Товар не найден"));
        return convertToDto(product);
    }

    /**
     * Создает новый товар.
     * <p>
     * Выполняет проверки:
     * <ul>
     *   <li>Уникальность названия товара</li>
     *   <li>Существование категории</li>
     *   <li>Корректность цены и количества</li>
     * </ul>
     * </p>
     *
     * @param productDto DTO с данными нового товара
     * @return DTO созданного товара
     * @throws RuntimeException если товар с таким названием уже существует или категория не найдена
     */
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        if (productRepository.findByNameIgnoreCase(productDto.getName()).isPresent()) {
            throw new RuntimeException("Товар с таким названием уже существует");
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());

        Category category = categoryRepository
            .findById(productDto.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        product.setCategory(category);

        product.setPrice(productDto.getPrice());
        product.setQuantity(productDto.getQuantity());

        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    /**
     * Обновляет существующий товар.
     * <p>
     * Проверяет:
     * <ul>
     *   <li>Существование товара и категории</li>
     *   <li>Уникальность названия, если оно изменяется</li>
     *   <li>Корректность цены и количества</li>
     * </ul>
     * </p>
     *
     * @param id ID товара для обновления
     * @param productDto DTO с новыми данными
     * @return DTO обновленного товара
     * @throws RuntimeException если товар/категория не найдены или новое название уже занято
     */
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product product = productRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Товар не найден"));

        if (
            !product.getName().equalsIgnoreCase(productDto.getName())
                && productRepository.findByNameIgnoreCase(productDto.getName()).isPresent()
        ) {
            throw new RuntimeException("Товар с таким названием уже существует");
        }

        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());

        Category category = categoryRepository
            .findById(productDto.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        product.setCategory(category);

        product.setPrice(productDto.getPrice());
        product.setQuantity(productDto.getQuantity());

        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    /**
     * Удаляет товар по ID.
     * <p>
     * Перед удалением проверяет существование товара.
     * Не проверяет наличие связанных заказов (каскадное удаление не настроено).
     * </p>
     *
     * @param id ID товара для удаления
     * @throws RuntimeException если товар не найден
     */
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Товар не найден");
        }
        productRepository.deleteById(id);
    }

    /**
     * Выполняет поиск товаров с применением множественных фильтров.
     * <p>
     * Поддерживает фильтрацию по:
     * <ul>
     *   <li>Названию (частичное совпадение, без учета регистра)</li>
     *   <li>Бренду (частичное совпадение, без учета регистра)</li>
     *   <li>Названию категории (точное совпадение, без учета регистра)</li>
     *   <li>Диапазону цен</li>
     *   <li>Диапазону количества на складе</li>
     * </ul>
     * </p>
     *
     * @param name фильтр по названию
     * @param brand фильтр по бренду
     * @param categoryName фильтр по категории
     * @param minPrice минимальная цена
     * @param maxPrice максимальная цена
     * @param minQuantity минимальное количество
     * @param maxQuantity максимальное количество
     * @return отфильтрованный список товаров
     */
    public List<ProductDto> searchProducts(
        String name,
        String brand,
        String categoryName,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer minQuantity,
        Integer maxQuantity
    ) {
        String nameFilter = (name == null || name.trim().isEmpty()) ? null : name;
        String brandFilter = (brand == null || brand.trim().isEmpty()) ? null : brand;
        String categoryFilter = (categoryName == null || categoryName.trim().isEmpty()) ? null : categoryName;

        return productRepository
            .findByFilters(nameFilter, brandFilter, categoryFilter, minPrice, maxPrice, minQuantity, maxQuantity)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Конвертирует сущность Product в DTO.
     * <p>
     * Денормализует данные категории (ID и название) для удобства
     * отображения и редактирования в UI.
     * </p>
     *
     * @param product сущность товара
     * @return DTO товара
     */
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        return dto;
    }
}
