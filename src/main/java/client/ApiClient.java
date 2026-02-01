package client;

import io.restassured.response.Response;
import model.User;
import model.UserCredentials;

import static io.restassured.RestAssured.given;

public class ApiClient {

    public static final String BASE_URL = "https://stellarburgers.education-services.ru/";


    // Создание нового пользователя
    public Response createUser(User user) {
        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(user)
                .when()
                .post("api/auth/register");
    }

    // Удаление пользователя
    public Response deleteUser(String accessToken) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", accessToken)
                .when()
                .delete("api/auth/user");
    }

    // Логин пользователя
    public Response loginUser(UserCredentials credentials) {
        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(credentials)
                .when()
                .post("api/auth/login");
    }

    // Обновление данных пользователя
    public Response updateUser(String accessToken, User user) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", accessToken)
                .contentType("application/json")
                .body(user)
                .when()
                .patch("api/auth/user");
    }

    // Получение данных пользователя
    public Response getUser(String accessToken) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", accessToken)
                .when()
                .get("api/auth/user");
    }

    // Создание заказа
    public Response createOrder(String accessToken, String requestBody) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", accessToken)
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("api/orders");
    }

    // Получение заказов пользователя
    public Response getUserOrders(String accessToken) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", accessToken)
                .when()
                .get("api/orders");
    }



}
