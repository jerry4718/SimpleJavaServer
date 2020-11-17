package space.mmty.controller;

import com.sun.net.httpserver.HttpExchange;
import space.mmty.annotation.Controller;
import space.mmty.constant.HttpMethod;

@Controller()
public class RestController {

    @Controller.Control(api = @Controller.Api(url = "/rest", methods = HttpMethod.GET))
    public String restGet(HttpExchange exchange) {
        StringBuilder response = new StringBuilder("restGet");

        for (int i = 0; i <= 9; i++) {
            if (response.length() > 0) {
                response.append("\n");
            }
            response.append(Math.random());
        }

        return response.toString();
    }

    @Controller.Control(api = @Controller.Api(url = "/rest", methods = HttpMethod.POST))
    public String restPost(HttpExchange exchange) {
        StringBuilder response = new StringBuilder("restPost");

        for (int i = 0; i <= 9; i++) {
            if (response.length() > 0) {
                response.append("\n");
            }
            response.append(Math.random());
        }

        return response.toString();
    }

    @Controller.Control(api = @Controller.Api(url = "/rest", methods = HttpMethod.PUT))
    public String restPut(HttpExchange exchange) {
        StringBuilder response = new StringBuilder("restPut");

        for (int i = 0; i <= 9; i++) {
            if (response.length() > 0) {
                response.append("\n");
            }
            response.append(Math.random());
        }

        return response.toString();
    }

    @Controller.Control(api = @Controller.Api(url = "/rest", methods = HttpMethod.PATCH))
    public String restPatch(HttpExchange exchange) {
        StringBuilder response = new StringBuilder("restPatch");

        for (int i = 0; i <= 9; i++) {
            if (response.length() > 0) {
                response.append("\n");
            }
            response.append(Math.random());
        }

        return response.toString();
    }

    @Controller.Control(api = @Controller.Api(url = "/rest", methods = HttpMethod.DELETE))
    public String restDelete(HttpExchange exchange) {
        StringBuilder response = new StringBuilder("restDelete");

        for (int i = 0; i <= 9; i++) {
            if (response.length() > 0) {
                response.append("\n");
            }
            response.append(Math.random());
        }

        return response.toString();
    }
}
