package space.mmty.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import space.mmty.exception.Message;
import space.mmty.util.HandlerUtil;
import space.mmty.util.ResponseUtil;

import java.io.IOException;

public abstract class BaseController {

    public final void handle(HttpExchange httpExchange) throws IOException {
            if (HandlerUtil.crosPack(httpExchange)) {
                return;
            }

            String response =  this.control(httpExchange);
    }

    public abstract String control(HttpExchange exchange);
}
