package ru.fa.kobzar.mikhail.digitalstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.fa.kobzar.mikhail.digitalstore.dto.CategoryDto;
import ru.fa.kobzar.mikhail.digitalstore.service.CategoryService;

import java.util.List;

/**
 * Контроллер для управления категориями товаров.
 * <p>
 * Обрабатывает HTTP-запросы для операций CRUD с категориями:
 * просмотр списка, создание, редактирование, удаление и поиск.
 * Реализует маршрутизацию и подготовку модели для отображения.
 * </p>
 *
 * <p><strong>Базовый путь:</strong> {@code /categories}</p>
 *
 * <p><strong>Доступ:</strong></p>
 * <ul>
 *   <li>Чтение (GET) — доступно всем пользователям</li>
 *   <li>Создание/Редактирование/Удаление — требуется роль MANAGER или ADMIN</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see CategoryService
 * @see CategoryDto
 */
@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * Отображает страницу со списком всех категорий.
     * <p>
     * Метод обрабатывает GET-запрос по корневому пути /categories,
     * получает все категории из сервиса и передает их в представление "categories".
     * </p>
     *
     * @return ModelAndView с моделью, содержащей список категорий и именем представления "categories"
     * @throws RuntimeException если произошла ошибка при загрузке категорий
     */
    @GetMapping
    public ModelAndView listCategories() {
        ModelAndView mav = new ModelAndView("categories/categories");
        List<CategoryDto> categories = categoryService.getAllCategories();
        mav.addObject("categories", categories);
        return mav;
    }

    /**
     * Отображает форму создания новой категории.
     * <p>
     * Подготавливает модель с пустым объектом CategoryDto,
     * заголовком формы и путем для отправки данных.
     * </p>
     *
     * @return ModelAndView с моделью формы создания категории и представлением "category-form"
     */
    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        ModelAndView mav = new ModelAndView("categories/category-form");
        mav.addObject("categoryDto", new CategoryDto());
        mav.addObject("title", "Создание категории");
        mav.addObject("action", "/categories");
        return mav;
    }

    /**
     * Обрабатывает создание новой категории.
     * <p>
     * Принимает данные из формы, валидирует их через сервис.
     * При успешном создании перенаправляет на страницу списка категорий.
     * При ошибке возвращает на форму с сообщением об ошибке.
     * </p>
     *
     * @param categoryDto DTO с данными новой категории
     * @param model модель для передачи данных в представление при ошибке
     * @return строку перенаправления на /categories или имя представления "category-form" при ошибке
     * @throws RuntimeException если категория с таким именем уже существует
     */
    @PostMapping
    public String createCategory(@ModelAttribute CategoryDto categoryDto, Model model) {
        try {
            categoryService.createCategory(categoryDto);
            return "redirect:/categories";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categoryDto", categoryDto);
            model.addAttribute("title", "Создание категории");
            model.addAttribute("action", "/categories");
            return "category-form";
        }
    }

    /**
     * Отображает форму редактирования категории.
     * <p>
     * Получает категорию по ID, конвертирует в DTO и подготавливает
     * модель для отображения формы редактирования.
     * </p>
     *
     * @param id идентификатор категории для редактирования
     * @return ModelAndView с данными категории и представлением "category-form"
     * @throws RuntimeException если категория не найдена
     */
    @GetMapping("/{id}/edit")
    public ModelAndView showEditForm(@PathVariable Long id) {
        CategoryDto categoryDto = categoryService.getCategoryById(id);
        ModelAndView mav = new ModelAndView("categories/category-form");
        mav.addObject("categoryDto", categoryDto);
        mav.addObject("title", "Редактирование категории");
        mav.addObject("action", "/categories/" + id + "/update");
        return mav;
    }

    /**
     * Обрабатывает обновление существующей категории.
     * <p>
     * Принимает ID категории и данные из формы, обновляет через сервис.
     * При успешном обновлении перенаправляет на список категорий.
     * При ошибке возвращает на форму с сообщением.
     * </p>
     *
     * @param id идентификатор обновляемой категории
     * @param categoryDto DTO с новыми данными категории
     * @param model модель для передачи данных при ошибке
     * @return строку перенаправления на /categories или имя представления "category-form" при ошибке
     * @throws RuntimeException если категория не найдена или новое название уже занято
     */
    @PostMapping("/{id}/update")
    public String updateCategory(@PathVariable Long id, @ModelAttribute CategoryDto categoryDto, Model model) {
        try {
            categoryService.updateCategory(id, categoryDto);
            return "redirect:/categories";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categoryDto", categoryDto);
            model.addAttribute("title", "Редактирование категории");
            model.addAttribute("action", "/categories/" + id + "/update");
            return "category-form";
        }
    }

    /**
     * Обрабатывает удаление категории.
     * <p>
     * Удаляет категорию по ID и перенаправляет на страницу списка.
     * </p>
     *
     * @param id идентификатор удаляемой категории
     * @return строку перенаправления на /categories
     * @throws RuntimeException если категория не найдена
     */
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories";
    }

    /**
     * Выполняет поиск категорий по названию.
     * <p>
     * Принимает параметр поиска, находит категории,
     * содержащие заданную строку в названии, и отображает результат.
     * </p>
     *
     * @param name строка поиска по названию категории
     * @return ModelAndView с результатами поиска и представлением "categories"
     */
    @GetMapping("/search")
    public ModelAndView searchCategories(@RequestParam String name) {
        ModelAndView mav = new ModelAndView("categories");
        List<CategoryDto> categories = categoryService.searchCategories(name);
        mav.addObject("categories", categories);
        mav.addObject("searchQuery", name);
        return mav;
    }
}
