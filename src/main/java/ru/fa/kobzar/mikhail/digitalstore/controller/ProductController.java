package ru.fa.kobzar.mikhail.digitalstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.fa.kobzar.mikhail.digitalstore.dto.ProductDto;
import ru.fa.kobzar.mikhail.digitalstore.service.CategoryService;
import ru.fa.kobzar.mikhail.digitalstore.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Контроллер для управления товарами.
 * <p>
 * Обрабатывает HTTP-запросы для операций CRUD с товарами:
 * просмотр каталога, создание, редактирование, удаление и расширенный поиск.
 * Обеспечивает фильтрацию товаров по различным критериям.
 * </p>
 *
 * <p><strong>Базовый путь:</strong> {@code /products}</p>
 *
 * <p><strong>Доступ:</strong></p>
 * <ul>
 *   <li>Чтение (GET) — доступно всем пользователям</li>
 *   <li>Создание/Редактирование/Удаление — требуется роль MANAGER или ADMIN</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see ProductService
 * @see ProductDto
 */
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    /**
     * Отображает каталог товаров с возможностью фильтрации.
     * <p>
     * Поддерживает фильтрацию по названию, бренду, категории,
     * диапазону цен и количеству на складе.
     * </p>
     *
     * @param name название товара для фильтрации
     * @param brand бренд товара для фильтрации
     * @param category категория товара для фильтрации
     * @param minPrice минимальная цена для фильтрации
     * @param maxPrice максимальная цена для фильтрации
     * @param minQuantity минимальное количество на складе для фильтрации
     * @param maxQuantity максимальное количество на складе для фильтрации
     * @return ModelAndView с отфильтрованным списком товаров, списком категорий и представлением "products"
     */
    @GetMapping
    public ModelAndView listProducts(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String brand,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) Integer minQuantity,
        @RequestParam(required = false) Integer maxQuantity
    ) {
        ModelAndView mav = new ModelAndView("products/products");
        List<ProductDto> products = productService
            .searchProducts(name, brand, category, minPrice, maxPrice, minQuantity, maxQuantity);
        mav.addObject("products", products);
        mav.addObject("categories", categoryService.getAllCategories());
        return mav;
    }

    /**
     * Отображает форму создания нового товара.
     * <p>
     * Подготавливает модель с пустым объектом ProductDto,
     * списком всех категорий и данными для формы.
     * </p>
     *
     * @return ModelAndView с формой создания товара и представлением "product-form"
     */
    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        ModelAndView mav = new ModelAndView("products/product-form");
        mav.addObject("productDto", new ProductDto());
        mav.addObject("categories", categoryService.getAllCategories());
        mav.addObject("title", "Создание товара");
        mav.addObject("action", "/products");
        return mav;
    }

    /**
     * Обрабатывает создание нового товара.
     * <p>
     * Принимает данные из формы, валидирует их через сервис.
     * При успешном создании перенаправляет на каталог товаров.
     * При ошибке возвращает на форму с сообщением об ошибке.
     * </p>
     *
     * @param productDto DTO с данными нового товара
     * @param model модель для передачи данных в представление при ошибке
     * @return строку перенаправления на /products или имя представления "product-form" при ошибке
     * @throws RuntimeException если товар с таким названием уже существует или категория не найдена
     */
    @PostMapping
    public String createProduct(@ModelAttribute ProductDto productDto, Model model) {
        try {
            productService.createProduct(productDto);
            return "redirect:/products";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("productDto", productDto);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("title", "Создание товара");
            model.addAttribute("action", "/products");
            return "product-form";
        }
    }

    /**
     * Отображает форму редактирования товара.
     * <p>
     * Получает товар по ID, конвертирует в DTO и подготавливает
     * модель с данными товара и списком категорий для отображения формы редактирования.
     * </p>
     *
     * @param id идентификатор товара для редактирования
     * @return ModelAndView с данными товара и представлением "product-form"
     * @throws RuntimeException если товар не найден
     */
    @GetMapping("/{id}/edit")
    public ModelAndView showEditForm(@PathVariable Long id) {
        ProductDto productDto = productService.getProductById(id);
        ModelAndView mav = new ModelAndView("products/product-form");
        mav.addObject("productDto", productDto);
        mav.addObject("categories", categoryService.getAllCategories());
        mav.addObject("title", "Редактирование товара");
        mav.addObject("action", "/products/" + id + "/update");
        return mav;
    }

    /**
     * Обрабатывает обновление существующего товара.
     * <p>
     * Принимает ID товара и данные из формы, обновляет через сервис.
     * При успешном обновлении перенаправляет на каталог товаров.
     * При ошибке возвращает на форму с сообщением.
     * </p>
     *
     * @param id идентификатор обновляемого товара
     * @param productDto DTO с новыми данными товара
     * @param model модель для передачи данных при ошибке
     * @return строку перенаправления на /products или имя представления "product-form" при ошибке
     * @throws RuntimeException если товар не найден, категория не существует или новое название уже занято
     */
    @PostMapping("/{id}/update")
    public String updateProduct(@PathVariable Long id, @ModelAttribute ProductDto productDto, Model model) {
        try {
            productService.updateProduct(id, productDto);
            return "redirect:/products";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("productDto", productDto);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("title", "Редактирование товара");
            model.addAttribute("action", "/products/" + id + "/update");
            return "product-form";
        }
    }

    /**
     * Обрабатывает удаление товара.
     * <p>
     * Удаляет товар по ID и перенаправляет на каталог товаров.
     * </p>
     *
     * @param id идентификатор удаляемого товара
     * @return строку перенаправления на /products
     * @throws RuntimeException если товар не найден
     */
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }
}
