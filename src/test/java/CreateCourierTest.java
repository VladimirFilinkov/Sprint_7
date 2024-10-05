import courier.Courier;
import courier.CourierCredentials;
import courier.CourierClient;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CreateCourierTest {

    private CourierClient courierClient;
    private Courier courier;
    private int courierId = 0;  // Переменная для хранения ID курьера

    @Before
    public void setUp() {
        courierClient = new CourierClient();
    }

    @Test
    @DisplayName("Создаём курьера, заполнив все обязательные поля")
    public void createCourierWithRequiredFieldsResponse201() {
        courier = Courier.getRandomRequiredField();
        Response response = courierClient.postCreateToCourier(courier);
        courierClient.compareResponseCodeAndBodyAboutCreation(response);

        // Авторизация для получения ID курьера
        CourierCredentials courierCredentials = CourierCredentials.from(courier);
        Response responseLogin = courierClient.postToCourierLogin(courierCredentials);
        courierId = responseLogin.then().extract().path("id");  // Сохраняем ID курьера
        courierClient.compareLoginResponseAndBodyIdNotNull(responseLogin);
    }

    @Test
    @DisplayName("Создаём двух курьеров с одинаковыми данными")
    public void createTwoCouriersWithDoubleCredResponse409() {
        courier = Courier.getRandom();
        Response response = courierClient.postCreateToCourier(courier);
        courierClient.compareResponseCodeAndBodyAboutCreation(response);

        Response responseDuplicate = courierClient.postCreateToCourier(courier);
        courierClient.compareResponseCodeAndMessageWithError409(responseDuplicate);
    }

    @Test
    @DisplayName("Создаём курьера с существующим логином")
    public void createCourierWithLoginBusyResponse409() {
        String password = "Filinkov";
        String firstName = "Vladimir";
        courier = Courier.getRandom();
        Courier courierDuplicate = new Courier(courier.getLogin(), password, firstName);

        Response response = courierClient.postCreateToCourier(courier);
        courierClient.compareResponseCodeAndBodyAboutCreation(response);

        Response responseDuplicateLogin = courierClient.postCreateToCourier(courierDuplicate);
        courierClient.compareResponseCodeAndMessageWithError409(responseDuplicateLogin);
    }

    @Test
    @DisplayName("Создаем курьера с пустым логином")
    public void createCourierWithOutLoginResponse400() {
        String login = "";
        String password = "Filinkov";
        String firstName = "Vladimir";
        courier = new Courier(login, password, firstName);

        Response response = courierClient.postCreateToCourier(courier);
        courierClient.compareCodeAndMessageWithError400(response);
    }

    @Test
    @DisplayName("Создаем курьера с пустым паролем")
    public void createCourierWithOutPasswordResponse400() {
        String login = "Filinkov";
        String password = "";
        String firstName = "Vladimir";
        courier = new Courier(login, password, firstName);

        Response response = courierClient.postCreateToCourier(courier);
        courierClient.compareCodeAndMessageWithError400(response);
    }

    @After
    @Step("Удаляем курьера по id, если он был создан")
    public void deleteCourierWithIdResponse200() {
        if (courierId != 0) {  // Если ID курьера был получен, удаляем курьера
            Response responseDelete = courierClient.deleteCourier(courierId);
            courierClient.compareDeleteResponseCodeAndBodyOk(responseDelete);
        }
    }
}