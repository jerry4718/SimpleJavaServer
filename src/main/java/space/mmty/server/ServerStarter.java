package space.mmty.server;

import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.*;
import space.mmty.annotation.Controller;
import space.mmty.annotation.ServerMain;
import space.mmty.constant.HttpMethods;
import space.mmty.exception.Message;
import space.mmty.util.AnnotationUtil;
import space.mmty.util.HandlerUtil;
import space.mmty.util.PackageUtil;
import space.mmty.util.ResponseUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ServerMain(controllerPackages = {
        "space.mmty.controller"
})
public class ServerStarter {
    private static final Logger root_logger = Logger.getRootLogger();
    private static final Logger logger = Logger.getLogger(ServerStarter.class);
    private static final Integer port = 8001;

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    static {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss}|%-5p|%-30.40c] - %m%n")));
        root_logger.setLevel(Level.DEBUG);
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(executor);

        AnnotationUtil.execIfIsPresent(ServerStarter.class, ServerMain.class)
                .accept(initServerMain(server));

        server.start();
        logger.info("Server started on http://localhost:" + port);
    }

    public static Consumer<ServerMain> initServerMain(HttpServer server) {
        return serverMain -> {
            String[] controllerPackages = serverMain.controllerPackages();

            logger.debug(JSONObject.toJSONString(controllerPackages));

            for (String controllerPackage : controllerPackages) {
                PackageUtil.scanPacket(
                        controllerPackage,
                        klass -> AnnotationUtil.execIfIsPresent(klass, Controller.class)
                                .accept(initController(server, klass))
                );
            }
        };
    }

    public static Consumer<Controller> initController(HttpServer server, Class<?> klass) {
        return controller -> {
            logger.debug(controller.toString());
            logger.debug(klass.getTypeName());

            Object handler = null;
            try {
                handler = klass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            Object finalHandler = handler;

            Method[] methods = klass.getMethods();

            for (Method method : methods) {
                AnnotationUtil.execIfIsPresent(method, Controller.Control.class)
                        .accept(initControlMethod(server, klass, finalHandler, method));
            }
        };
    }

    public static Consumer<Controller.Control> initControlMethod(HttpServer server, Class<?> klass, Object handler, Method method) {
        return control -> {
            Controller.Api[] apis = control.api();

            logger.debug(control.toString());
            logger.debug(klass.getTypeName() + "#" + method.getName());

            for (Controller.Api api : apis) {
                List<String> httpMethods = Stream.of(api.methods())
                        .map(HttpMethods::getMethod)
                        .collect(Collectors.toList());

                server.createContext(api.url(), httpExchange -> {
                    // 处理跨域
                    if (HandlerUtil.crosPack(httpExchange)) {
                        return;
                    }

                    String requestMethod = httpExchange.getRequestMethod();
                    logger.info(requestMethod + " " + api.url() + "");
                    // 过滤method
                    if (!httpMethods.contains(requestMethod)) {
                        ResponseUtil.end(httpExchange, 404);
                        return;
                    }

                    try {
                        Object responseObj = method.invoke(handler, httpExchange);
                        if (responseObj != null) {
                            String response = responseObj.toString();
                            ResponseUtil.end(httpExchange, 200, response);
                        }
                    } catch (IOException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        ResponseUtil.end(httpExchange, 500, e.getMessage());
                    } catch (Message e) {
                        e.printStackTrace();
                        ResponseUtil.end(httpExchange, 501, e.getMessage());
                    }
                });
            }
        };
    }

}
