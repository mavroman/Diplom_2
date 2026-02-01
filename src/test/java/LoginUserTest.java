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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("API тесты для авторизации пользователя")
public class LoginUserTest {
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
        Response response = apiClient.createUser(testUser);
        assertEquals(200, response.statusCode(), "Не удалось создать тестового пользователя");
    }

    @Test
    @DisplayName("Успешная авторизация пользователя с обязательными полями")
    public void userAuthSuccessTest() {
        UserCredentials userCreds = new UserCredentials(testEmail, "12345");
        Response loginResponse = apiClient.loginUser(userCreds);
        verifySuccessResponse(loginResponse);
        saveAccessToken(loginResponse);
    }

    @Step("Проверка успешного ответа")
    private void verifySuccessResponse(Response response) {
        assertEquals(200, response.statusCode(), "Пользователь должен успешно авторизоваться");
        response.then()
                .body("success", equalTo(true))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(testEmail));
    }

    @Step("Сохранение access token")
    private void saveAccessToken(Response response) {
        accessToken = response.body().jsonPath().getString("accessToken");
        assertNotNull(accessToken, "Access token должен быть возвращен");
        assertTrue(accessToken.startsWith("Bearer "), "Access token должен начинаться с 'Bearer '");
    }

    @Test
    @DisplayName("Авторизация с неверным email возвращает ошибку 401")
    public void userAuthWithWrongEmailFailsTest() {
        testAuthFail("wrong_romatest@yandex.ru", "12345", 401, "email or password are incorrect");
    }

    @Test
    @DisplayName("Авторизация с неверным паролем возвращает ошибку 401")
    public void userAuthWithWrongPasswordFailsTest() {
        testAuthFail(testEmail, "wrong_pass", 401, "email or password are incorrect");
    }

    @Test
    @DisplayName("Авторизация без email возвращает ошибку")
    public void userAuthWithoutEmailFailsTest() {
        testAuthFail(null, "12345", 401, "email or password are incorrect");
    }

    @Test
    @DisplayName("Авторизация без пароля возвращает ошибку")
    public void userAuthWithoutPasswordFailsTest() {
        testAuthFail(testEmail, null, 401, "email or password are incorrect");
    }

    @Test
    @DisplayName("Авторизация с пустым email")
    public void userAuthWithEmptyEmailFailsTest() {
        testAuthFail("", "12345", 401, "email or password are incorrect");
    }

    @Test
    @DisplayName("Авторизация с пустым паролем")
    public void userAuthWithEmptyPasswordFailsTest() {
        testAuthFail(testEmail, "", 401, "email or password are incorrect");
    }


    @Step("Тестирование неуспешной авторизации")
    private void testAuthFail(String email, String password, int expectedStatus, String expectedMessage) {
        UserCredentials credentials = new UserCredentials(email, password);
        Response response = apiClient.loginUser(credentials);

        assertEquals(expectedStatus, response.statusCode(),
                "Неверный статус код при неуспешной авторизации");
        response.then()
                .body("success", equalTo(false))
                .body("message", equalTo(expectedMessage));
    }

    @AfterEach
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            apiClient.deleteUser(accessToken);
        }
    }


}
