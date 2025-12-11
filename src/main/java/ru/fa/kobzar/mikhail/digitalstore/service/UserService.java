package ru.fa.kobzar.mikhail.digitalstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.kobzar.mikhail.digitalstore.dto.UserDto;
import ru.fa.kobzar.mikhail.digitalstore.entity.User;
import ru.fa.kobzar.mikhail.digitalstore.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервисный слой для управления пользователями.
 * <p>
 * Реализует {@link UserDetailsService} для интеграции с Spring Security.
 * Обеспечивает бизнес-логику аутентификации, регистрации и управления учетными записями.
 * </p>
 *
 * <p><strong>Особенности безопасности:</strong></p>
 * <ul>
 *   <li>Email используется как уникальный логин</li>
 *   <li>Пароли хранятся в виде BCrypt-хэша</li>
 *   <li>Автоматическая установка роли CLIENT при регистрации</li>
 *   <li>Специальная логика для первого администратора (email admin@digital.store)</li>
 *   <li>Перезагрузка контекста безопасности при изменении email/роли</li>
 * </ul>
 *
 * <p><strong>Бизнес-правила:</strong></p>
 * <ul>
 *   <li>Email должен быть уникальным</li>
 *   <li>Минимальная длина пароля — 6 символов</li>
 *   <li>Клиент может редактировать только свой профиль</li>
 *   <li>Изменение email/роли требует перезагрузки аутентификации</li>
 * </ul>
 *
 * @author Mikhail Kobzar
 * @version 1.0
 * @see User
 * @see UserDto
 * @see UserRepository
 * @see UserDetailsService
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Возвращает список всех пользователей.
     *
     * @return список всех пользователей
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Находит пользователя по ID.
     *
     * @param id уникальный идентификатор пользователя
     * @return пользователь с указанным ID
     * @throws RuntimeException если пользователь не найден
     */
    public User getUserById(Long id) {
        return userRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    /**
     * Находит пользователя по email.
     * <p>
     * Используется для аутентификации в Spring Security.
     * </p>
     *
     * @param email email пользователя
     * @return пользователь с указанным email
     * @throws RuntimeException если пользователь не найден
     */
    public User getUserByEmail(String email) {
        return userRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    /**
     * Создает нового пользователя (админом).
     * <p>
     * Проверяет уникальность email, кодирует пароль.
     * Устанавливает даты создания и обновления.
     * </p>
     *
     * @param userDto DTO с данными нового пользователя
     * @return созданный пользователь
     * @throws RuntimeException если email уже существует
     */
    @Transactional
    public User createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Такой Email уже существует");
        }

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setRole(userDto.getRole());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Обновляет существующего пользователя (админом).
     * <p>
     * Проверяет уникальность email, если он изменяется.
     * При необходимости перекодирует пароль.
     * Обновляет дату изменения.
     * </p>
     *
     * @param id ID пользователя
     * @param userDto DTO с новыми данными
     * @return обновленный пользователь
     * @throws RuntimeException если пользователь не найден или email занят
     */
    @Transactional
    public User updateUser(Long id, UserDto userDto) {
        User user = getUserById(id);

        if (!user.getEmail().equals(userDto.getEmail()) && userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Такой Email уже существует");
        }

        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setRole(userDto.getRole());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Обновляет профиль текущего пользователя.
     * <p>
     * Позволяет пользователю изменить только свой профиль.
     * Проверяет уникальность нового email.
     * При изменении email перезагружает контекст безопасности.
     * </p>
     *
     * @param id ID профиля
     * @param userDto DTO с новыми данными
     * @param currentUserEmail email текущего пользователя
     * @return обновленный пользователь
     * @throws RuntimeException если попытка изменить чужой профиль или email занят
     */
    @Transactional
    public User updateProfile(Long id, UserDto userDto, String currentUserEmail) {
        User user = getUserById(id);

        if (!user.getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("Вы можете редактировать только свой профиль");
        }

        String newEmail = userDto.getEmail();
        if (!user.getEmail().equals(newEmail)) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Этот Email уже используется другим пользователем");
            }
            user.setEmail(newEmail);
        }

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Регистрирует нового пользователя (самостоятельно).
     * <p>
     * Устанавливает роль CLIENT по умолчанию, кроме случая с email admin@digital.store.
     * Кодирует пароль, устанавливает даты создания и обновления.
     * </p>
     *
     * @param userDto DTO с данными регистрации
     * @return созданный пользователь
     * @throws RuntimeException если email уже зарегистрирован
     */
    @Transactional
    public User registerUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Пользователь с таким Email уже зарегистрирован");
        }

        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        if ("admin@digital.store".equalsIgnoreCase(userDto.getEmail())) {
            user.setRole(User.Role.ADMIN);
        } else {
            user.setRole(User.Role.CLIENT);
        }

        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Удаляет пользователя по ID.
     *
     * @param id ID пользователя для удаления
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Загружает пользователя по email для Spring Security.
     *
     * @param email email пользователя
     * @return объект UserDetails для аутентификации
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + email + " не найден"));
    }

    /**
     * Перезагружает контекст безопасности Spring Security.
     * <p>
     * Используется после изменения email или роли пользователя,
     * чтобы изменения вступили в силу немедленно.
     * </p>
     *
     * @param email email пользователя для перезагрузки
     */
    public void reloadUserAuthentication(String email) {
        UserDetails userDetails = loadUserByUsername(email);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Выполняет поиск пользователей с применением множественных фильтров.
     * <p>
     * Поддерживает фильтрацию по:
     * <ul>
     *   <li>Email (частичное совпадение, без учета регистра)</li>
     *   <li>Имени (частичное совпадение, без учета регистра)</li>
     *   <li>Фамилии (частичное совпадение, без учета регистра)</li>
     *   <li>Роли</li>
     *   <li>Диапазону дат создания</li>
     *   <li>Диапазону дат обновления</li>
     * </ul>
     * </p>
     *
     * @param email фильтр по email
     * @param firstName фильтр по имени
     * @param lastName фильтр по фамилии
     * @param role фильтр по роли
     * @param createdFrom начало диапазона даты создания
     * @param createdTo конец диапазона даты создания
     * @param updatedFrom начало диапазона даты обновления
     * @param updatedTo конец диапазона даты обновления
     * @return отфильтрованный список пользователей
     */
    public List<User> searchUsers(
        String email,
        String firstName,
        String lastName,
        User.Role role,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        LocalDateTime updatedFrom,
        LocalDateTime updatedTo
    ) {
        String emailFilter = (email == null || email.trim().isEmpty()) ? null : email;
        String firstNameFilter = (firstName == null || firstName.trim().isEmpty()) ? null : firstName;
        String lastNameFilter = (lastName == null || lastName.trim().isEmpty()) ? null : lastName;

        return userRepository.findByFilters(
            emailFilter,
            firstNameFilter,
            lastNameFilter,
            role,
            createdFrom,
            createdTo,
            updatedFrom,
            updatedTo
        );
    }
}
