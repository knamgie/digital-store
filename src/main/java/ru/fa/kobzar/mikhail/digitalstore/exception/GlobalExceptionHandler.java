package ru.fa.kobzar.mikhail.digitalstore.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Глобальный обработчик исключений для всего Spring MVC приложения.
 * <p>
 * Перехватывает исключения на уровне контроллеров и преобразует их
 * в удобочитаемые страницы ошибок с соответствующими HTTP-статусами.
 * Обеспечивает централизованную обработку ошибок и улучшает UX.
 * </p>
 *
 * <p><strong>Обрабатываемые исключения:</strong></p>
 * <ul>
 *   <li>{@link AccessDeniedException} → HTTP 403 (Forbidden)</li>
 *   <li>{@link RuntimeException} → HTTP 500 (Internal Server Error)</li>
 * </ul>
 *
 * <p><strong>Возвращаемые модели:</strong></p>
 * <ul>
 *   <li>errorCode — HTTP-статус ошибки</li>
 *   <li>errorMessage — понятное сообщение для пользователя</li>
 *   <li>errorDetails — технические детали для отладки</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see ControllerAdvice
 * @see ExceptionHandler
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Обрабатывает исключения недостатка прав доступа.
     * <p>
     * Вызывается при попытке доступа к ресурсу без необходимых прав.
     * Возвращает страницу ошибки 403 с понятным объяснением.
     * </p>
     *
     * @param ex исключение AccessDeniedException
     * @return ModelAndView с данными ошибки и шаблоном "error"
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied(AccessDeniedException ex) {
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("errorCode", 403);
        mav.addObject("errorMessage", "У вас недостаточно прав для доступа к этой странице.");
        mav.addObject("errorDetails", "Доступ запрещен. Требуется роль ADMIN.");
        return mav;
    }

    /**
     * Обрабатывает общие исключения времени выполнения.
     * <p>
     * Перехватывает RuntimeException и его наследников,
     * возвращая страницу ошибки 500 с сообщением исключения.
     * </p>
     *
     * @param ex исключение RuntimeException
     * @return ModelAndView с данными ошибки и шаблоном "error"
     */
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeException(RuntimeException ex) {
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("errorCode", 500);
        mav.addObject("errorMessage", "На сервере произошла ошибка: " + ex.getMessage());
        mav.addObject("errorDetails", "Исключение: " + ex.getClass().getSimpleName());
        return mav;
    }
}
