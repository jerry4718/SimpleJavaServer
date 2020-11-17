package space.mmty.util;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import space.mmty.constant.HttpMethod;

import java.io.IOException;

public class HandlerUtil {
    public static boolean crosPack(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();

        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Access-Control-Allow-Methods", "OPTIONS,GET,POST,PATCH,PUT,DELETE,COPY,HEAD,LINK,UNLINK,PURGE,LOCK,UNLOCK,PROPFIND,VIEW");
        responseHeaders.add("Access-Control-Allow-Credentials", "true");
        responseHeaders.add("Access-Control-Allow-Headers", "Postman-Token,User-Agent,Cache-Control,Origin,Content-Type,Accept,X-Requested-With");

        if (HttpMethod.OPTIONS == HttpMethod.valueOf(requestMethod)) {
            ResponseUtil.end(exchange);
            return true;
        }
        return false;
    }
}
