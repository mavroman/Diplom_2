import client.ApiClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("API тесты на создание заказа")
public class CreateOrderTest {

    private ApiClient apiClient;
    private User user;
    private String accessToken;

    @BeforeEach
    public void setUp() {
        apiClient = new ApiClient();
        String timestamp = String.valueOf(System.currentTimeMillis());
        user = new User("romatest-" + timestamp + "@yandex.ru", "12345", "Roms");

        Response createResponse = apiClient.createUser(user);
        assertEquals(200, createResponse.statusCode(), "Не удалось создать тестового пользователя");

        accessToken = createResponse.body().jsonPath().getString("accessToken");
        assertNotNull(accessToken, "Access token не должен быть null");
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и валидными ингредиентами")
    public void createOrderWithAuthAndValidIngredients() {
        String requestBody = "{\"ingredients\": [\"61c0c5a71d1f82001bdaaa6d\", \"61c0c5a71d1f82001bdaaa6f\"]}";

        Response response = createOrderWithRequestBody(accessToken, requestBody);

        validateSuccessfulOrderResponse(response);
    }

    @Step("Создание заказа с access token = '{accessToken}' и телом запроса: {requestBody}")
    private Response createOrderWithRequestBody(String accessToken, String requestBody) {
        return apiClient.createOrder(accessToken, requestBody);
    }

    @Step("Проверка успешного ответа при создании заказа")
    private void validateSuccessfulOrderResponse(Response response) {
        assertThat("Статус код должен быть 200",
                response.statusCode(), equalTo(200));
        assertThat("Ответ должен содержать success: true",
                response.path("success"), equalTo(true));
        assertThat("Ответ должен содержать name",
                response.path("name"), notNullValue());
        assertThat("Ответ должен содержать order.number",
                response.path("order.number"), notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и невалидным хешем ингредиентов")
    public void createOrderWithAuthAndInvalidIngredientHash() {
        String requestBody = "{\"ingredients\": [\"invalid_hash_XXX\", \"609646e4dc916e00276b2870\"]}";

        Response response = createOrderWithRequestBody(accessToken, requestBody);

        validateResponseWithInvalidIngredientHash(response);
    }

    @Step("Проверка ответа при невалидном хеше ингредиентов")
    private void validateResponseWithInvalidIngredientHash(Response response) {
        assertThat("При невалидном хеше должен быть статус 500",
                response.statusCode(), equalTo(500));
    }

    @Test
    @DisplayName("Создание заказа с авторизацией без ингредиентов")
    public void createOrderWithAuthAndNoIngredients() {
        String requestBody = "{\"ingredients\": []}";

        Response response = createOrderWithRequestBody(accessToken, requestBody);

        validateResponseWithNoIngredients(response);
    }

    @Step("Проверка ответа при отсутствии ингредиентов")
    private void validateResponseWithNoIngredients(Response response) {
        assertThat("При отсутствии ингредиентов должен быть статус 400",
                response.statusCode(), equalTo(400));
        assertThat("Должно быть сообщение об ошибке",
                response.path("message"), equalTo("Ingredient ids must be provided"));
        assertThat("Должен быть success: false",
                response.path("success"), equalTo(false));
    }

    @Test
    @DisplayName("Создание заказа без авторизации с валидными ингредиентами")
    public void createOrderWithoutAuthAndValidIngredients() {
        String requestBody = "{\"ingredients\": [\"61c0c5a71d1f82001bdaaa6d\", \"61c0c5a71d1f82001bdaaa6f\"]}";

        Response response = createOrderWithRequestBody("", requestBody);

        validateSuccessfulOrderResponse(response);
    }

    @Test
    @DisplayName("Создание заказа без авторизации с невалидным хешем ингредиентов")
    public void createOrderWithoutAuthAndInvalidIngredientHash() {
        String requestBody = "{\"ingredients\": [\"invalid_hash_XXX\", \"609646e4dc916e00276b2870\"]}";

        Response response = createOrderWithRequestBody("", requestBody);

        validateResponseWithInvalidIngredientHash(response);
    }

    @Test
    @DisplayName("Создание заказа без авторизации без ингредиентов")
    public void createOrderWithoutAuthAndNoIngredients() {
        String requestBody = "{\"ingredients\": []}";

        Response response = createOrderWithRequestBody("", requestBody);

        validateResponseWithNoIngredients(response);
    }

    @Test
    @DisplayName("Создание заказа с пустым телом запроса")
    public void createOrderWithEmptyRequestBody() {
        String requestBody = "{}";

        Response response = createOrderWithRequestBody(accessToken, requestBody);

        validateResponseWithNoIngredients(response);
    }

    @AfterEach
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            apiClient.deleteUser(accessToken);
        }
    }
}