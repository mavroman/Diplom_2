import client.ApiClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("API тесты на получение заказов конкретного пользователя")
public class GetUserOrdersTest {

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
    @DisplayName("Получение заказов авторизованного пользователя")
    public void testGetUserOrdersWithAuth() {
        createTestOrder();

        Response response = getUserOrdersWithAuth();

        validateSuccessOrdersResponse(response);
    }

    @Step("Создание тестового заказа")
    private void createTestOrder() {
        String orderBody = "{\"ingredients\": [\"61c0c5a71d1f82001bdaaa6d\", \"61c0c5a71d1f82001bdaaa6f\"]}";
        Response createOrderResponse = apiClient.createOrder(accessToken, orderBody);
        createOrderResponse.then().assertThat().statusCode(200);
    }

    @Step("Получение заказов пользователя")
    private Response getUserOrdersWithAuth() {
        return apiClient.getUserOrders(accessToken);
    }

    @Step("Проверка успешного ответа на запрос заказов")
    private void validateSuccessOrdersResponse(Response response) {
        response.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("orders", hasSize(lessThanOrEqualTo(50))) // максимум 50 заказов
                .body("total", greaterThanOrEqualTo(0))
                .body("totalToday", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Получение заказов неавторизованного пользователя")
    public void testGetUserOrdersWithoutAuth() {
        Response response = apiClient.getUserOrders("");

        validateNoAuthResponse(response);
    }

    @Step("Проверка ответа на неавторизованный запрос")
    private void validateNoAuthResponse(Response response) {
        response.then()
                .assertThat()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @AfterEach
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            apiClient.deleteUser(accessToken);
        }
    }



}
