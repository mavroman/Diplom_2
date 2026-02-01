import client.ApiClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.*;

@DisplayName("API тесты для регистрации пользователя")
public class UserRegistrationTest {

    private ApiClient apiClient = new ApiClient();
    private String accessToken;
    private String testEmail;


    @Test
    @DisplayName("Проверка успешного создания пользователя")
    public void createUserTest() {
        createUniqueUser();
        validateSuccessUserCreation();
    }

    @Test
    @DisplayName("Проверка создания пользователя, который уже зарегистрирован")
    public void createAvailableUserFailTest() {
        createUniqueUser();
        attemptDuplicateRegistration();
        validateDuplicateRegistrationError();
    }

    @Test
    @DisplayName("Проверка создания пользователя без email")
    public void createUserWithoutEmailTest() {
        createUserWithoutEmail();
        validateRequiredFieldsError();
    }

    @Test
    @DisplayName("Проверка создания пользователя без password")
    public void createUserWithoutPasswordTest() {
        createUserWithoutPassword();
        validateRequiredFieldsError();
    }

    @Test
    @DisplayName("Проверка создания пользователя без name")
    public void createUserWithoutNameTest() {
        createUserWithoutName();
        validateRequiredFieldsError();
    }

    @Test
    @DisplayName("Проверка создания пользователя с пустым email")
    public void createUserWithEmptyEmailTest() {
        createUserWithEmptyEmail();
        validateRequiredFieldsError();
    }

    @Test
    @DisplayName("Проверка создания пользователя с пустым password")
    public void createUserWithEmptyPasswordTest() {
        createUserWithEmptyPassword();
        validateRequiredFieldsError();
    }

    @Test
    @DisplayName("Проверка создания пользователя с пустым user")
    public void createUserWithEmptyNameTest() {
        createUserWithEmptyName();
        validateRequiredFieldsError();
    }

    @Step("Создание уникального пользователя с email: {email}")
    private User createUniqueUser(String email, String password, String name) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String userEmail = email != null ? email : "roma-" + timestamp + "@yandex.ru";
        testEmail = userEmail;
        return new User(userEmail, password, name);
    }

    @Step("Создание уникального пользователя")
    private void createUniqueUser() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testEmail = "roma-" + timestamp + "@yandex.ru";
        User user = new User(testEmail, "12345", "Roms");

        Response response = apiClient.createUser(user);
        assertEquals(200, response.statusCode(), "Неверный код ответа при создании пользователя");

        accessToken = response.path("accessToken");
        validateUserCreationResponse(response, user);
    }

    @Step("Попытка дублирующей регистрации пользователя")
    private void attemptDuplicateRegistration() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        User duplicateUser = new User(testEmail, "12345", "Roms");
        Response duplicateResponse = apiClient.createUser(duplicateUser);

        assertEquals(403, duplicateResponse.statusCode(),
                "При создании пользователя, который уже зарегистрирован должен возвращаться 403 код");

        validateDuplicateUserResponse(duplicateResponse);
    }

    @Step("Создание пользователя без email")
    private void createUserWithoutEmail() {
        User user = new User(null, "1234", "Roms");
        Response response = apiClient.createUser(user);

        assertEquals(403, response.statusCode(),
                "Пользователь без email должен возвращать код 403");
    }

    @Step("Создание пользователя без пароля")
    private void createUserWithoutPassword() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testEmail = "roma-" + timestamp + "@yandex.ru";
        User user = new User(testEmail, null, "Roms");

        Response response = apiClient.createUser(user);

        assertEquals(403, response.statusCode(),
                "Пользователь без password должен возвращать код 403");
    }

    @Step("Создание пользователя без имени")
    private void createUserWithoutName() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testEmail = "roma-" + timestamp + "@yandex.ru";
        User user = new User(testEmail, "12345", null);

        Response response = apiClient.createUser(user);

        assertEquals(403, response.statusCode(),
                "Пользователь без name должен возвращать код 403");
    }

    @Step("Создание пользователя с пустым email")
    private void createUserWithEmptyEmail() {
        User user = new User("", "1234", "Roms");

        Response response = apiClient.createUser(user);

        assertEquals(403, response.statusCode(),
                "Пользователь с пустым email должен возвращать код 403");
    }

    @Step("Создание пользователя с пустым паролем")
    private void createUserWithEmptyPassword() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testEmail = "roma-" + timestamp + "@yandex.ru";
        User user = new User(testEmail, "", "Roms");

        Response response = apiClient.createUser(user);

        assertEquals(403, response.statusCode(),
                "Пользователь с пустым password должен возвращать код 403");
    }

    @Step("Создание пользователя с пустым именем")
    private void createUserWithEmptyName() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testEmail = "roma-" + timestamp + "@yandex.ru";
        User user = new User(testEmail, "12345", "");

        Response response = apiClient.createUser(user);

        assertEquals(403, response.statusCode(),
                "Пользователь с пустым user должен возвращать код 403");
    }

    @Step("Валидация успешного создания пользователя")
    private void validateSuccessUserCreation() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        User user = new User("roma-" + timestamp + "@yandex.ru", "12345", "Roms");

        Response response = apiClient.createUser(user);

        assertEquals(200, response.statusCode(), "Неверный код ответа при создании пользователя");

        accessToken = response.path("accessToken");
        validateUserCreationResponse(response, user);
    }

    @Step("Валидация ответа при создании пользователя")
    private void validateUserCreationResponse(Response response, User user) {
        response.then()
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name", equalTo(user.getName()))
                .body("accessToken", startsWith("Bearer "))
                .body("refreshToken", notNullValue());

        String accessTokenValue = response.path("accessToken");
        this.accessToken = accessTokenValue;
    }

    @Step("Валидация ошибки дублирующей регистрации")
    private void validateDuplicateRegistrationError() {
        User duplicateUser = new User(testEmail, "12345", "Roms");
        Response duplicateResponse = apiClient.createUser(duplicateUser);

        duplicateResponse.then()
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"))
                .body("accessToken", nullValue())
                .body("refreshToken", nullValue())
                .body("user", nullValue());
    }

    @Step("Валидация ответа при ошибке обязательных полей")
    private void validateRequiredFieldsError(Response response) {
        response.then()
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Валидация ответа при ошибке обязательных полей")
    private void validateRequiredFieldsError() {

    }

    @Step("Валидация ответа при дублировании пользователя")
    private void validateDuplicateUserResponse(Response response) {
        response.then()
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"))
                .body("accessToken", nullValue())
                .body("refreshToken", nullValue())
                .body("user", nullValue());
    }

    @AfterEach
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            apiClient.deleteUser(accessToken);
        }
    }
}