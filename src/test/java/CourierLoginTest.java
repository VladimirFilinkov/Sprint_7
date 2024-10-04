import courier.*;
import io.qameta.allure.junit4.DisplayName;
import org.junit.*;
import io.restassured.response.Response;


public class CourierLoginTest {
    private CourierClient courierClient;
    private Courier courier;
    private int courierId;


    @Before
    public void setUp() {
        courierClient = new CourierClient();
        courier = Courier.getRandom();  // создаём случайного курьера
    }

    @Test
    @DisplayName("Проверка входа с валидными данными")
    public void checkCourierCanLoginWithValidCredsResponse200() {
        courierClient.postCreateToCourier(courier);
        CourierCredentials courierCredentials = CourierCredentials.from(courier);
        Response response = courierClient.postToCourierLogin(courierCredentials);
        courierId = response.then().extract().path("id"); // сохраняем ID

        // Проверка, что id не null
        courierClient.compareLoginResponseAndBodyIdNotNull(response);
    }

    @Test
    @DisplayName("Проверка входа с пустыми логином и паролем")
    public void checkLoginCourierWithInvalidCredsResponse400() {
        CourierCredentials courierCredentialsIncorrect = new CourierCredentials("", "");
        Response response = courierClient.postToCourierLogin(courierCredentialsIncorrect);
        courierClient.compareLoginResponseCodeAndBody400Message(response);
    }

    @Test
    @DisplayName("Проверка входа с пустым логином")
    public void checkLoginCourierWithOutLoginResponse400() {
        courierClient.postCreateToCourier(courier);  // создаём курьера
        CourierCredentials courierCredentialsIncorrect = new CourierCredentials("", courier.getPassword());
        Response response = courierClient.postToCourierLogin(courierCredentialsIncorrect);
        courierClient.compareLoginResponseCodeAndBody400Message(response);
    }

    @Test
    @DisplayName("Проверка входа с пустым паролем")
    public void checkLoginCourierWithOutPasswordResponse400() {
        courierClient.postCreateToCourier(courier);  // создаём курьера
        CourierCredentials courierCredentialsIncorrect = new CourierCredentials(courier.getLogin(), "");
        Response response = courierClient.postToCourierLogin(courierCredentialsIncorrect);
        courierClient.compareLoginResponseCodeAndBody400Message(response);
    }

    @Test
    @DisplayName("Проверка входа с невалидным логином")
    public void checkLoginCourierIncorrectLoginNameResponse404() {
        courierClient.postCreateToCourier(courier);  // создаём курьера
        CourierCredentials courierCredentialsIncorrect = new CourierCredentials("VladFilin", courier.getPassword());
        Response response = courierClient.postToCourierLogin(courierCredentialsIncorrect);
        courierClient.compareLoginResponseCodeAndBody404Message(response);
    }

    @Test
    @DisplayName("Проверка входа с невалидным паролем")
    public void checkLoginCourierIncorrectPasswordResponse404() {
        courierClient.postCreateToCourier(courier);  // создаём курьера
        CourierCredentials courierCredentialsIncorrect = new CourierCredentials(courier.getLogin(), "FilinVlad");
        Response response = courierClient.postToCourierLogin(courierCredentialsIncorrect);
        courierClient.compareLoginResponseCodeAndBody404Message(response);
    }

    @After
    public void tearDown() {
        // Удаляем курьера, только если его ID был создан
        if (courierId != 0) {
            Response response = courierClient.deleteCourier(courierId);
            courierClient.compareDeleteResponseCodeAndBodyOk(response);
        }
    }
}