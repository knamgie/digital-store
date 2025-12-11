package ru.fa.kobzar.mikhail.digitalstore.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для обработки HTTP-ошибок на уровне приложения.
 * <p>
 * Перехватывает ошибки, сгенерированные сервером (404, 403, 500 и др.),
 * и отображает кастомную страницу ошибки с понятным сообщением.
 * Реализует интерфейс ErrorController для интеграции с Spring Boot.
 * </p>
 *
 * <p><strong>Обрабатываемые ошибки:</strong></p>
 * <ul>
 *   <li>404 — Страница не найдена</li>
 *   <li>403 — Доступ запрещен</li>
 *   <li>401 — Требуется аутентификация</li>
 *   <li>400 — Некорректный запрос</li>
 *   <li>500 — Внутренняя ошибка сервера</li>
 * </ul>
 *
 * <p><strong>Безопасность:</strong></p>
 * <p>
 * Обрабатывает ошибки доступа и предоставляет понятные сообщения,
 * не раскрывая чувствительную информацию о системе.
 * </p>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see ErrorController
 * @see RequestDispatcher
 */
@Controller
public class CustomErrorController implements ErrorController {
    private static final String ERROR_PATH = "/error";

    /**
     * Обрабатывает HTTP-ошибки и формирует модель для страницы ошибки.
     * <p>
     * Извлекает атрибуты ошибки из запроса:
     * <ul>
     *   <li>ERROR_STATUS_CODE — HTTP-статус</li>
     *   <li>ERROR_REQUEST_URI — запрашиваемый путь</li>
     *   <li>ERROR_MESSAGE — техническое сообщение</li>
     * </ul>
     * Формирует понятное сообщение в зависимости от статуса и
     * подготавливает безопасные для отображения детали ошибки.
     * </p>
     *
     * @param request HTTP-запрос с атрибутами ошибки
     * @param model модель для передачи данных в шаблон
     * @return имя шаблон "error"
     */
    @RequestMapping(ERROR_PATH)
    public String handleError(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        int code = statusCode != null ? Integer.parseInt(statusCode.toString()) : 500;
        String uri = requestUri != null ? requestUri.toString() : "Неизвестный путь";
        String message = errorMessage != null ? errorMessage.toString() : "Произошла неизвестная ошибка";

        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", getFriendlyErrorMessage(code, uri, message));
        model.addAttribute("errorDetails", String.format("Ошибка %d: %s", code, message));

        return "error/error";
    }

    /**
     * Возвращает понятное сообщение об ошибке на основе HTTP-статуса.
     * <p>
     * Преобразует технический статус в локализованное сообщение,
     * безопасно экранируя спецсимволы в URI.
     * </p>
     *
     * @param code HTTP-статус ошибки
     * @param uri запрашиваемый путь
     * @param technicalMessage техническое сообщение
     * @return понятное сообщение для отображения пользователю
     */
    private String getFriendlyErrorMessage(int code, String uri, String technicalMessage) {
        return switch (code) {
            case 404 -> String.format("Страница <strong>%s</strong> не найдена.", escapeHtml(uri));
            case 403 -> "У вас недостаточно прав для доступа к этой странице.";
            case 401 -> "Для доступа к этой странице необходимо войти в систему.";
            case 400 -> "Некорректный запрос.";
            case 500 -> "На сервере произошла ошибка.";
            default -> "Произошла непредвиденная ошибка.";
        };
    }

    /**
     * Экранирует HTML-спецсимволы для предотвращения XSS-атак.
     * <p>
     * Заменяет &, <, >, ", ' на соответствующие HTML-сущности.
     * </p>
     *
     * @param input строка для экранирования
     * @return экранированная строка
     */
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }

    /**
     * Возвращает путь к странице ошибки.
     * <p>
     * Используется Spring Boot для маппинга ошибок.
     * </p>
     *
     * @return путь "/error"
     */
    public String getErrorPath() {
        return ERROR_PATH;
    }
}