package ru.fa.kobzar.mikhail.digitalstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.fa.kobzar.mikhail.digitalstore.entity.Category;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Category}.
 * <p>
 * Предоставляет CRUD операции и специализированные методы поиска категорий.
 * Расширяет {@link JpaRepository} для стандартных операций с БД.
 * </p>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see Category
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /**
     * Поиск категорий по части названия без учета регистра.
     *
     * @param namePart часть названия категории для поиска
     * @return список категорий, содержащих указанную строку в названии
     */
    List<Category> findByNameContainingIgnoreCase(String namePart);

    /**
     * Поиск категории по точному названию.
     *
     * @param name название категории
     * @return Optional с найденной категорией или пустой, если не найдена
     */
    Optional<Category> findByName(String name);

    /**
     * Проверка существования категории с указанным названием.
     *
     * @param name название категории для проверки
     * @return true, если категория с таким названием существует
     */
    boolean existsByName(String name);
}
