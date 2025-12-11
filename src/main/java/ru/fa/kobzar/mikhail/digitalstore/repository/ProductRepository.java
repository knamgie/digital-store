package ru.fa.kobzar.mikhail.digitalstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.fa.kobzar.mikhail.digitalstore.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Product}.
 * <p>
 * Предоставляет CRUD операции и сложные фильтры для поиска товаров.
 * Расширяет {@link JpaSpecificationExecutor} для поддержки спецификаций.
 * </p>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see Product
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    /**
     * Поиск товаров с применением множественных фильтров.
     * <p>
     * Метод поддерживает фильтрацию по:
     * <ul>
     *   <li>Названию товара (частичное совпадение)</li>
     *   <li>Бренду (частичное совпадение)</li>
     *   <li>Названию категории (точное совпадение)</li>
     *   <li>Диапазону цен</li>
     *   <li>Диапазону количества на складе</li>
     * </ul>
     * Все параметры опциональны. Если параметр null, он не участвует в фильтрации.
     * </p>
     *
     * @param name фильтр по названию товара (частичное совпадение, игнорирование регистра)
     * @param brand фильтр по бренду (частичное совпадение, игнорирование регистра)
     * @param categoryName фильтр по названию категории (точное совпадение, игнорирование регистра)
     * @param minPrice минимальная цена товара (включительно)
     * @param maxPrice максимальная цена товара (включительно)
     * @param minQuantity минимальное количество на складе (включительно)
     * @param maxQuantity максимальное количество на складе (включительно)
     * @return список товаров, соответствующих всем примененным фильтрам
     */
    @Query(
        "SELECT p FROM Product p "
            + "JOIN p.category c WHERE "
            + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
            + "(:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND "
            + "(:categoryName IS NULL OR LOWER(c.name) = LOWER(:categoryName)) AND "
            + "(:minPrice IS NULL OR p.price >= :minPrice) AND "
            + "(:maxPrice IS NULL OR p.price <= :maxPrice) AND "
            + "(:minQuantity IS NULL OR p.quantity >= :minQuantity) AND "
            + "(:maxQuantity IS NULL OR p.quantity <= :maxQuantity)"
    ) List<Product> findByFilters(
        @Param("name") String name,
        @Param("brand") String brand,
        @Param("categoryName") String categoryName,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minQuantity") Integer minQuantity,
        @Param("maxQuantity") Integer maxQuantity
    );

    /**
     * Поиск товара по названию без учета регистра.
     *
     * @param name название товара для поиска
     * @return Optional с найденным товаром или пустой, если не найден
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) = LOWER(:name)")
    Optional<Product> findByNameIgnoreCase(@Param("name") String name);
}
