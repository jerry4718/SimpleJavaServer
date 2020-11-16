package space.mmty.controller;

import com.sun.net.httpserver.HttpExchange;
import space.mmty.annotation.Controller;
import space.mmty.constant.HttpMethods;

@Controller()
public class TestController {

    @Controller.Control(api = {
            @Controller.Api(url = "/", methods = {HttpMethods.GET, HttpMethods.POST}),
            @Controller.Api(url = "/test", methods = {HttpMethods.GET, HttpMethods.POST})
    })
    public String control(HttpExchange exchange) {
        StringBuilder response = new StringBuilder();

        for (int i = 0; i <= 9; i++) {
            if (response.length() > 0) {
                response.append("\n");
            }
            response.append(Math.random());
        }

        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        return response.toString();
    }
}
