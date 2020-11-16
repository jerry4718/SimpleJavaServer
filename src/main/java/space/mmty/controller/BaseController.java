package space.mmty.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import space.mmty.exception.Message;
import space.mmty.util.HandlerUtil;
import space.mmty.util.ResponseUtil;

import java.io.IOException;

public abstract class BaseController implements HttpHandler {

    @Override
    public final void handle(HttpExchange httpExchange) throws IOException {
        try {
            if (HandlerUtil.crosPack(httpExchange)) {
                return;
            }

            String response =  this.control(httpExchange);
            ResponseUtil.end(httpExchange, 200, response);
        } catch (IOException e) {
            e.printStackTrace();
            ResponseUtil.end(httpExchange, 500, e.getMessage());
        } catch (Message e) {
            e.printStackTrace();
            ResponseUtil.end(httpExchange, 501, e.getMessage());
        }
    }

    public abstract String control(HttpExchange exchange);
}
