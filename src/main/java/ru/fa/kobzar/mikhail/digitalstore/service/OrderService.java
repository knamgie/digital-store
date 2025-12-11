package ru.fa.kobzar.mikhail.digitalstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.kobzar.mikhail.digitalstore.dto.OrderDto;
import ru.fa.kobzar.mikhail.digitalstore.entity.Order;
import ru.fa.kobzar.mikhail.digitalstore.entity.Product;
import ru.fa.kobzar.mikhail.digitalstore.entity.User;
import ru.fa.kobzar.mikhail.digitalstore.repository.OrderRepository;
import ru.fa.kobzar.mikhail.digitalstore.repository.ProductRepository;
import ru.fa.kobzar.mikhail.digitalstore.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервисный слой для управления заказами.
 * <p>
 * Реализует бизнес-логику жизненного цикла заказа: создание, обновление статуса,
 * поиск и фильтрацию. Обеспечивает целостность данных и проверку прав доступа.
 * </p>
 *
 * <p><strong>Жизненный цикл заказа:</strong></p>
 * <ul>
 *   <li>NEW → ACCEPTED → IN_TRANSIT → DELIVERED</li>
 *   <li>NEW → CANCELLED (только клиентом)</li>
 * </ul>
 *
 * <p><strong>Бизнес-правила:</strong></p>
 * <ul>
 *   <li>При создании заказа проверяется наличие товара на складе</li>
 *   <li>Общая стоимость рассчитывается автоматически</li>
 *   <li>Клиент может изменять статус только своих заказов</li>
 *   <li>Клиент может только отменить заказ</li>
 *   <li>При отмене заказа товар возвращается на склад</li>
 *   <li>Завершенные и отмененные заказы нельзя изменить</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see Order
 * @see OrderDto
 * @see OrderRepository
 */
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Возвращает список всех заказов в системе.
     * <p>
     * Денормализует данные для отображения в UI.
     * </p>
     *
     * @return список всех заказов как DTO
     */
    public List<OrderDto> getAllOrders() {
        return orderRepository
            .findAll()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Возвращает список заказов конкретного пользователя.
     *
     * @param userId ID пользователя
     * @return список заказов пользователя
     * @throws RuntimeException если пользователь не найден
     */
    public List<OrderDto> getOrdersByUser(Long userId) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return orderRepository
            .findByUser(user)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Находит заказ по ID.
     *
     * @param id ID заказа
     * @return DTO заказа
     * @throws RuntimeException если заказ не найден
     */
    public OrderDto getOrderById(Long id) {
        return orderRepository
            .findById(id)
            .map(this::convertToDto)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }

    /**
     * Создает новый заказ.
     * <p>
     * Выполняет проверки:
     * <ul>
     *   <li>Существование пользователя и товара</li>
     *   <li>Достаточное количество товара на складе</li>
     *   <li>Рассчитывает общую стоимость</li>
     * </ul>
     * При успешном создании уменьшает количество товара на складе.
     * </p>
     *
     * @param orderDto DTO с данными заказа (userId, productId, quantity)
     * @return DTO созданного заказа
     * @throws RuntimeException если пользователь/товар не найдены или недостаточно товара
     */
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        User user = userRepository
            .findById(orderDto.getUserId())
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Product product = productRepository
            .findById(orderDto.getProductId())
            .orElseThrow(() -> new RuntimeException("Товар не найден"));

        if (product.getQuantity() < orderDto.getQuantity()) {
            throw new RuntimeException("Недостаточно товара в наличии. Доступно: " + product.getQuantity());
        }

        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setQuantity(orderDto.getQuantity());
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(orderDto.getQuantity()));
        order.setTotalPrice(totalPrice);
        order.setStatus(Order.Status.NEW);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        product.setQuantity(product.getQuantity() - orderDto.getQuantity());
        productRepository.save(product);

        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    /**
     * Обновляет статус существующего заказа.
     * <p>
     * Проверяет права доступа:
     * <ul>
     *   <li>Клиент может изменять только свои заказы</li>
     *   <li>Клиент может только отменить заказ</li>
     *   <li>Менеджер/админ может устанавливать любой статус</li>
     *   <li>Запрещено изменять статус DELIVERED и CANCELLED</li>
     * </ul>
     * При отмене заказа товар возвращается на склад.
     * </p>
     *
     * @param id ID заказа
     * @param newStatus новый статус
     * @param currentUserEmail email текущего пользователя
     * @return DTO обновленного заказа
     * @throws RuntimeException если заказ не найден или статус не может быть изменен
     * @throws AccessDeniedException если недостаточно прав
     */
    @Transactional
    public OrderDto updateOrderStatus(Long id, Order.Status newStatus, String currentUserEmail) {
        Order order = orderRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        User currentUser = userRepository
            .findByEmail(currentUserEmail)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (order.getStatus() == Order.Status.CANCELLED) {
            throw new RuntimeException("Нельзя изменить статус отмененного заказа");
        }
        if (order.getStatus() == Order.Status.DELIVERED) {
            throw new RuntimeException("Нельзя изменить статус доставленного заказа");
        }

        if (currentUser.getRole() == User.Role.CLIENT) {
            if (!order.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Вы можете изменять только свои заказы");
            }
            if (newStatus != Order.Status.CANCELLED) {
                throw new RuntimeException("Клиент может только отменить заказ");
            }
        }

        if (newStatus == Order.Status.CANCELLED) {
            Product product = order.getProduct();
            product.setQuantity(product.getQuantity() + order.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);
        return convertToDto(updatedOrder);
    }

    /**
     * Выполняет поиск заказов с фильтрацией для менеджеров и админов.
     * <p>
     * Поддерживает фильтры: email пользователя, название товара, статус,
     * диапазоны дат создания и обновления.
     * </p>
     *
     * @param email фильтр по email
     * @param productName фильтр по названию товара
     * @param status фильтр по статусу
     * @param createdFrom начало диапазона даты создания
     * @param createdTo конец диапазона даты создания
     * @param updatedFrom начало диапазона даты обновления
     * @param updatedTo конец диапазона даты обновления
     * @return отфильтрованный список заказов
     */
    @Transactional(readOnly = true)
    public List<OrderDto> searchOrders(
        String email,
        String productName,
        Order.Status status,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        LocalDateTime updatedFrom,
        LocalDateTime updatedTo
    ) {
        String emailFilter = (email == null || email.trim().isEmpty()) ? null : email;
        String productNameFilter = (productName == null || productName.trim().isEmpty()) ? null : productName;

        List<Order> orders = orderRepository.findByFilters(
            emailFilter,
            null,
            productNameFilter,
            null,
            null,
            null,
            status,
            createdFrom,
            createdTo,
            updatedFrom,
            updatedTo
        );

        return orders
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Выполняет поиск заказов конкретного пользователя.
     * <p>
     * Используется клиентами для просмотра своих заказов.
     * Поддерживает те же фильтры, что и searchOrders, но ограниченные по userId.
     * </p>
     *
     * @param userId ID пользователя
     * @param productName фильтр по названию товара
     * @param status фильтр по статусу
     * @param createdFrom начало диапазона даты создания
     * @param createdTo конец диапазона даты создания
     * @param updatedFrom начало диапазона даты обновления
     * @param updatedTo конец диапазона даты обновления
     * @return отфильтрованный список заказов пользователя
     */
    @Transactional(readOnly = true)
    public List<OrderDto> searchUserOrders(
        Long userId,
        String productName,
        Order.Status status,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        LocalDateTime updatedFrom,
        LocalDateTime updatedTo
    ) {
        String productNameFilter = (productName == null || productName.trim().isEmpty()) ? null : productName;

        List<Order> orders = orderRepository.findByFilters(
            null,
            userId,
            productNameFilter,
            null,
            null,
            null,
            status,
            createdFrom,
            createdTo,
            updatedFrom,
            updatedTo
        );

        return orders
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Конвертирует сущность Order в DTO.
     * <p>
     * Денормализует связанные данные (email, имя пользователя,
     * название и бренд товара) для удобства отображения в UI.
     * </p>
     *
     * @param order сущность заказа
     * @return DTO заказа
     */
    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUserEmail(order.getUser().getEmail());
        dto.setUserFullName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
        dto.setProductId(order.getProduct().getId());
        dto.setProductName(order.getProduct().getName());
        dto.setProductBrand(order.getProduct().getBrand());
        dto.setQuantity(order.getQuantity());
        dto.setUnitPrice(order.getProduct().getPrice());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
}
