package ru.fa.kobzar.mikhail.digitalstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для обработки базовых путей приложения.
 * <p>
 * Обрабатывает запросы к корневому пути и странице информации об авторе.
 * Выполняет перенаправление на основную функциональную страницу.
 * </p>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 */
@Controller
public class HomeController {
    /**
     * Обрабатывает GET-запрос к корневому пути "/".
     * <p>
     * Выполняет автоматическое перенаправление на страницу со списком товаров,
     * которая является основной страницей приложения.
     * </p>
     *
     * @return строку перенаправления на /products
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/products";
    }

    /**
     * Отображает информационную страницу об авторе приложения.
     * <p>
     * Обрабатывает GET-запрос по пути /author и возвращает соответствующее представление.
     * </p>
     *
     * @return имя представления "author"
     */
    @GetMapping("/author")
    public String author() {
        return "static/author";
    }
}
