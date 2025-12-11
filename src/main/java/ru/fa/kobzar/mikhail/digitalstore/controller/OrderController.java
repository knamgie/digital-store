package ru.fa.kobzar.mikhail.digitalstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.fa.kobzar.mikhail.digitalstore.dto.OrderDto;
import ru.fa.kobzar.mikhail.digitalstore.dto.ProductDto;
import ru.fa.kobzar.mikhail.digitalstore.entity.Order;
import ru.fa.kobzar.mikhail.digitalstore.entity.User;
import ru.fa.kobzar.mikhail.digitalstore.service.OrderService;
import ru.fa.kobzar.mikhail.digitalstore.service.ProductService;
import ru.fa.kobzar.mikhail.digitalstore.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Контроллер для управления заказами.
 * <p>
 * Обрабатывает все операции с заказами: просмотр списка, создание,
 * изменение статуса, фильтрацию. Реализует разграничение доступа
 * между клиентами и управляющим персоналом.
 * </p>
 *
 * <p><strong>Базовый путь:</strong> {@code /orders}</p>
 *
 * <p><strong>Доступ:</strong></p>
 * <ul>
 *   <li>Клиенты — могут видеть только свои заказы, создавать новые,
 *       отменять заказы в статусе NEW</li>
 *   <li>Менеджеры/Админы — полный доступ к управлению всеми заказами</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see OrderService
 * @see OrderDto
 */
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;

    /**
     * Отображает страницу со списком заказов с возможностью фильтрации.
     * <p>
     * Для клиентов отображает только их собственные заказы.
     * Для менеджеров/админов — все заказы с применением фильтров.
     * Поддерживает фильтры по email, товару, статусу и диапазонам дат.
     * </p>
     *
     * @param userEmail email пользователя для фильтрации (для менеджеров)
     * @param productName название товара для фильтрации
     * @param status статус заказа для фильтрации
     * @param createdFrom начальная дата создания заказа
     * @param createdTo конечная дата создания заказа
     * @param updatedFrom начальная дата обновления заказа
     * @param updatedTo конечная дата обновления заказа
     * @return ModelAndView с отфильтрованным списком заказов и представлением "orders"
     * @throws RuntimeException если пользователь не найден
     */
    @GetMapping
    public ModelAndView listOrders(
        @RequestParam(required = false) String userEmail,
        @RequestParam(required = false) String productName,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdFrom,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate createdTo,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate updatedFrom,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate updatedTo
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);

        ModelAndView mav = new ModelAndView("orders/orders");
        mav.addObject("statuses", Order.Status.values());

        List<OrderDto> orders;
        Order.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Order.Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                // ...
            }
        }

        LocalDateTime createdFromDt = createdFrom != null ? createdFrom.atStartOfDay() : null;
        LocalDateTime createdToDt = createdTo != null ? createdTo.plusDays(1).atStartOfDay().minusNanos(1) : null;
        LocalDateTime updatedFromDt = updatedFrom != null ? updatedFrom.atStartOfDay() : null;
        LocalDateTime updatedToDt = updatedTo != null ? updatedTo.plusDays(1).atStartOfDay().minusNanos(1) : null;

        if (user.getRole() == User.Role.CLIENT) {
            orders = orderService.searchUserOrders(
                user.getId(),
                productName,
                statusEnum,
                createdFromDt,
                createdToDt,
                updatedFromDt,
                updatedToDt
            );
        } else {
            orders = orderService.searchOrders(
                userEmail,
                productName,
                statusEnum,
                createdFromDt,
                createdToDt,
                updatedFromDt,
                updatedToDt
            );
        }

        mav.addObject("orders", orders);
        return mav;
    }

    /**
     * Отображает форму создания нового заказа.
     * <p>
     * Предварительно заполняет данные о товаре (если указан productId)
     * и email текущего пользователя (для клиентов).
     * </p>
     *
     * @param productId идентификатор товара (опциональный параметр)
     * @return ModelAndView с формой создания заказа и представлением "order-form",
     *         или перенаправление на /products, если productId не указан
     * @throws RuntimeException если товар не найден
     */
    @GetMapping("/create")
    public ModelAndView showCreateForm(@RequestParam(required = false) Long productId) {
        if (productId == null) {
            return new ModelAndView("redirect:/products");
        }

        ProductDto product = productService.getProductById(productId);

        OrderDto orderDto = new OrderDto();
        orderDto.setProductId(productId);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserByEmail(currentUserEmail);

        if (currentUser.getRole() == User.Role.CLIENT) {
            orderDto.setUserEmail(currentUserEmail);
        } else {
            orderDto.setUserEmail("");
        }

        ModelAndView mav = new ModelAndView("orders/order-form");
        mav.addObject("orderDto", orderDto);
        mav.addObject("product", product);
        mav.addObject("title", "Создание заказа");
        mav.addObject("action", "/orders");
        mav.addObject("isClient", currentUser.getRole() == User.Role.CLIENT);

        return mav;
    }

    /**
     * Обрабатывает создание нового заказа.
     * <p>
     * Выполняет проверку прав доступа (клиент может создавать только свои заказы),
     * валидирует данные и создает заказ через сервис. При успехе перенаправляет
     * на страницу товаров, при ошибке возвращает на форму с сообщением.
     * </p>
     *
     * @param orderDto DTO с данными заказа
     * @param model модель для передачи данных при ошибке
     * @return строку перенаправления на /products или имя представления "order-form" при ошибке
     * @throws AccessDeniedException если клиент пытается создать заказ от другого имени
     * @throws RuntimeException если пользователь или товар не найдены
     */
    @PostMapping
    public String createOrder(@ModelAttribute("orderDto") OrderDto orderDto, Model model) {
        try {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.getUserByEmail(currentUserEmail);

            if (currentUser.getRole() == User.Role.CLIENT) {
                if (!orderDto.getUserEmail().equals(currentUserEmail)) {
                    throw new AccessDeniedException("Вы можете создавать заказы только от своего имени");
                }
            }

            User user = userService.getUserByEmail(orderDto.getUserEmail());
            orderDto.setUserId(user.getId());

            orderService.createOrder(orderDto);
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("orderDto", orderDto);
            ProductDto product = productService.getProductById(orderDto.getProductId());
            model.addAttribute("product", product);
            model.addAttribute("title", "Создание заказа");
            model.addAttribute("action", "/orders");

            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.getUserByEmail(currentUserEmail);
            model.addAttribute("isClient", currentUser.getRole() == User.Role.CLIENT);

            return "order-form";
        }
    }

    /**
     * Отображает форму изменения статуса заказа.
     * <p>
     * Для клиентов показывает только возможность отмены заказа.
     * Для менеджеров/админов — полный список доступных статусов.
     * Запрещает изменение статуса для доставленных и отмененных заказов.
     * </p>
     *
     * @param id идентификатор заказа
     * @return ModelAndView с формой изменения статуса и представлением "order-status-form"
     * @throws RuntimeException если статус заказа нельзя изменить
     * @throws AccessDeniedException если клиент пытается изменить чужой заказ
     */
    @GetMapping("/{id}/edit-status")
    public ModelAndView showEditStatusForm(@PathVariable Long id) {
        OrderDto orderDto = orderService.getOrderById(id);

        if (orderDto.getStatus() == Order.Status.CANCELLED || orderDto.getStatus() == Order.Status.DELIVERED) {
            throw new RuntimeException("Нельзя изменить статус данного заказа");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserByEmail(email);

        ModelAndView mav = new ModelAndView("orders/order-status-form");
        mav.addObject("orderDto", orderDto);

        if (currentUser.getRole() == User.Role.CLIENT) {
            mav.addObject("statuses", new Order.Status[]{Order.Status.CANCELLED});
        } else {
            mav.addObject("statuses", Order.Status.values());
        }

        mav.addObject("currentUserRole", currentUser.getRole());
        mav.addObject("title", "Изменение статуса заказа");
        mav.addObject("action", "/orders/" + id + "/update-status");
        return mav;
    }

    /**
     * Обрабатывает изменение статуса заказа.
     * <p>
     * Вызывает сервис для обновления статуса с проверкой прав доступа.
     * После успешного изменения перенаправляет на страницу списка заказов.
     * </p>
     *
     * @param id идентификатор заказа
     * @param status новый статус заказа
     * @return строку перенаправления на /orders
     * @throws AccessDeniedException если недостаточно прав для изменения статуса
     * @throws RuntimeException если заказ не найден или статус нельзя изменить
     */
    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam Order.Status status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        orderService.updateOrderStatus(id, status, email);
        return "redirect:/orders";
    }
}