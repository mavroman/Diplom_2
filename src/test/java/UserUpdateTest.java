import client.ApiClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import model.UserCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("API тесты для обновления данных пользователя")
public class UserUpdateTest {
    private ApiClient apiClient;
    private User testUser;
    private String testEmail;
    private String accessToken;

    @BeforeEach
    public void setUp() {
        apiClient = new ApiClient();
        String timestamp = String.valueOf(System.currentTimeMillis());
        testEmail = "romatest-" + timestamp + "@yandex.ru"; // Уникальный email для каждого запуска
        testUser = new User(testEmail, "12345", "Roms");

        Response createResponse = apiClient.createUser(testUser);
        assertEquals(200, createResponse.statusCode(), "Не удалось создать тестового пользователя");

        UserCredentials credentials = new UserCredentials(testEmail, "12345");
        Response loginResponse = apiClient.loginUser(credentials);
        accessToken = loginResponse.body().jsonPath().getString("accessToken");
        assertNotNull(accessToken, "Access token не должен быть null");
    }

    @Test
    @DisplayName("Успешное обновление email пользователя с авторизацией")
    public void updateUserEmailSuccessTest() {
        // Arrange
        String newEmail = "romatest-update" + System.currentTimeMillis() + "@yandex.ru";
        User updateData = new User(newEmail, "12345", "Roms");

        // Act
        Response response = apiClient.updateUser(accessToken, updateData);

        // Assert
        verifySuccessUpdate(response, newEmail, "Roms");
    }

    @Test
    @DisplayName("Успешное обновление имени пользователя с авторизацией")
    public void updateUserNameSuccessTest() {
        String newName = "Roms" + System.currentTimeMillis();
        User updateData = new User(testEmail, "12345", newName);

        Response response = apiClient.updateUser(accessToken, updateData);

        verifySuccessUpdate(response, testEmail, newName);
    }

    @Test
    @DisplayName("Успешное обновление пароля пользователя с авторизацией")
    public void updateUserPasswordSuccessTest() {
        String newPassword = "newPass12345";
        User updateData = new User(testEmail, newPassword, "Roms");

        Response response = apiClient.updateUser(accessToken, updateData);

        verifySuccessUpdate(response, testEmail, "Roms");
    }

    @Test
    @DisplayName("Успешное обновление всех полей одновременно")
    public void updateAllUserFieldsSuccessTest() {
        String newEmail = "romatest-update" + System.currentTimeMillis() + "@yandex.ru";
        String newPassword = "newPass12345";
        String newName = "New_Roms";
        User updateData = new User(newEmail, newPassword, newName);

        Response response = apiClient.updateUser(accessToken, updateData);

        verifySuccessUpdate(response, newEmail, newName);
    }

    @Test
    @DisplayName("Обновление данных пользователя без авторизации возвращает ошибку 401")
    public void updateUserWithoutAuthFailTest() {
        String newEmail = "no-auth-update-roma" + System.currentTimeMillis() + "@yandex.ru";
        User updateData = new User(newEmail, "12345", "Roma");

        Response response = apiClient.updateUser("", updateData);

        verifyNoAuthResponse(response);
    }


    @Step("Проверка успешного обновления данных")
    private void verifySuccessUpdate(Response response, String expectedEmail, String expectedName) {

        assertEquals(200, response.statusCode(),
                "Обновление данных пользователя должно быть успешным");

        response.then()
                .body("success", equalTo(true))
                .body("user.email", equalTo(expectedEmail))
                .body("user.name", equalTo(expectedName));
    }

    @Step("Проверка ответа при отсутствии авторизации")
    private void verifyNoAuthResponse(Response response) {
        assertEquals(401, response.statusCode(),
                "Без авторизации должен возвращаться код 401");

        response.then()
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Обновление email на уже существующий возвращает ошибку 403")
    @Step("Тест обновления email на существующий")
    public void updateExistingEmailFailTest() {
        // Создаем второго пользователя
        String secondUserEmail = "second-romatest" + System.currentTimeMillis() + "@yandex.ru";
        User secondUser = new User(secondUserEmail, "12345", "SecondRoma");
        Response createResponse = apiClient.createUser(secondUser);
        assertEquals(200, createResponse.statusCode(), "Не удалось создать второго пользователя");

        // Пытаемся обновить email пользователя 1 на email пользователя 2
        User updateData = new User(secondUserEmail, "12345", "SecondRoma");

        Response response = apiClient.updateUser(accessToken, updateData);

        assertEquals(403, response.statusCode(),
                "При обновлении на существующий email должен возвращаться код 403");

        response.then()
                .body("success", equalTo(false))
                .body("message", equalTo("User with such email already exists"));
    }


    @AfterEach
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
                apiClient.deleteUser(accessToken);
        }
    }

}
