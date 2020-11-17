package space.mmty.controller;

import com.sun.net.httpserver.HttpExchange;
import space.mmty.annotation.Controller;
import space.mmty.constant.HttpMethod;

@Controller()
public class Test2Controller {

    @Controller.Control(api = {
            @Controller.Api(url = "/test/", methods = HttpMethod.GET),
            @Controller.Api(url = "/test/2", methods = {HttpMethod.GET, HttpMethod.POST})
    })
    public String control(HttpExchange exchange) {
        StringBuilder response = new StringBuilder();

        for (int i = 0; i <= 11; i++) {
            if (response.length() > 0) {
                response.append("\n");
            }
            response.append(Math.random());
        }

        return response.toString();
    }
}
