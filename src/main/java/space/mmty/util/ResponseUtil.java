package space.mmty.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class ResponseUtil {
    public static void end(HttpExchange httpExchange, Integer code, String message) throws IOException {
        httpExchange.sendResponseHeaders(code, 0);
        httpExchange.getResponseBody().write(message.getBytes());
        httpExchange.getResponseBody().close();
    }

    public static void end(HttpExchange httpExchange, Integer code) throws IOException {
        httpExchange.sendResponseHeaders(code, 0);
        httpExchange.getResponseBody().close();
    }

    public static void end(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(200, 0);
        httpExchange.getResponseBody().close();
    }
}
