package space.mmty.server;

import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.*;
import space.mmty.annotation.Controller;
import space.mmty.annotation.ServerMain;
import space.mmty.constant.HttpMethod;
import space.mmty.exception.Message;
import space.mmty.module.HttpMethodInvokeOption;
import space.mmty.util.AnnotationUtil;
import space.mmty.util.HandlerUtil;
import space.mmty.util.PackageUtil;
import space.mmty.util.ResponseUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@ServerMain(
        port = 8001,
        controllerPackages = {"space.mmty.controller"}
)
public class ServerStarter {
    private static final Logger root_logger = Logger.getRootLogger();
    private static final Logger logger = Logger.getLogger(ServerStarter.class);

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    static {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss}|%-5p|%-30.40c] - %m%n")));
        root_logger.setLevel(Level.DEBUG);
    }

    public static void main(String[] args) {
        AnnotationUtil.execIfIsPresent(ServerStarter.class, ServerMain.class)
                .accept(serverMain -> {
                    try {
                        int port = serverMain.port();
                        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
                        server.setExecutor(executor);

                        initServerMain(server).accept(serverMain);

                        server.start();
                        logger.info("Server started on http://localhost:" + port);
                    } catch (IOException e) {
                        logger.error("Server started error", e);
                    }
                });
    }

    public static Consumer<ServerMain> initServerMain(HttpServer server) {
        return serverMain -> {
            String[] controllerPackages = serverMain.controllerPackages();

            logger.debug(JSONObject.toJSONString(controllerPackages));

            Map<String, Map<HttpMethod, HttpMethodInvokeOption>> restMapper = new HashMap<>();

            for (String controllerPackage : controllerPackages) {
                PackageUtil.scanPacket(
                        controllerPackage,
                        klass -> AnnotationUtil.execIfIsPresent(klass, Controller.class)
                                .accept(initController(server, klass, restMapper))
                );
            }

            for (Map.Entry<String, Map<HttpMethod, HttpMethodInvokeOption>> bindRest : restMapper.entrySet()) {
                String restUrl = bindRest.getKey();
                Map<HttpMethod, HttpMethodInvokeOption> restHolder = bindRest.getValue();

                server.createContext(restUrl, httpExchange -> {
                    // 处理跨域
                    if (HandlerUtil.crosPack(httpExchange)) {
                        return;
                    }

                    HttpMethod httpMethod = HttpMethod.valueOf(httpExchange.getRequestMethod());
                    logger.info(httpMethod.getMethod() + " " + restUrl + "");

                    // 过滤method
                    if (!restHolder.containsKey(httpMethod)) {
                        ResponseUtil.end(httpExchange, 404, "404 Not Found");
                        return;
                    }

                    HttpMethodInvokeOption option = restHolder.get(httpMethod);

                    try {
                        Object responseObj = option.invoke(httpExchange);
                        if (responseObj != null) {
                            ResponseUtil.end(httpExchange, 200, responseObj.toString());
                        } else {
                            ResponseUtil.end(httpExchange, 200);
                        }
                    } catch (IOException | IllegalAccessException | InvocationTargetException e) {
                        logger.error("handler execute error", e);
                        ResponseUtil.end(httpExchange, 500, e.getMessage());
                    } catch (Message e) {
                        logger.error(e.getMessage(), e);
                        ResponseUtil.end(httpExchange, 501, e.getMessage());
                    }
                });
            }
        };
    }

    public static Consumer<Controller> initController(HttpServer server, Class<?> klass, Map<String, Map<HttpMethod, HttpMethodInvokeOption>> restMapper) {
        return controller -> {
            logger.debug(controller.toString());
            logger.debug(klass.getTypeName());

            Object handler;
            try {
                handler = klass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("create controller instance error", e);
                return;
            }

            Method[] methods = klass.getMethods();

            for (Method method : methods) {
                AnnotationUtil.execIfIsPresent(method, Controller.Control.class)
                        .accept(initRestMapper(klass, restMapper, new HttpMethodInvokeOption(method, handler)));
            }
            // initControlMethod(server, klass, finalHandler, method)
        };
    }

    public static Consumer<Controller.Control> initRestMapper(Class<?> klass, Map<String, Map<HttpMethod, HttpMethodInvokeOption>> restMapper, HttpMethodInvokeOption option) {
        return control -> {
            Controller.Api[] apis = control.api();

            logger.debug(control.toString());
            logger.debug(klass.getTypeName() + "#" + option.getMethod().getName());

            for (Controller.Api api : apis) {
                String url = api.url();
                for (HttpMethod httpMethod : api.methods()) {
                    Map<HttpMethod, HttpMethodInvokeOption> restHolder;

                    if (restMapper.containsKey(url)) {
                        restHolder = restMapper.get(url);
                    } else {
                        restHolder = new HashMap<>();
                        restMapper.put(url, restHolder);
                    }

                    if (restHolder.containsKey(httpMethod)) {
                        logger.warn("method \"" + httpMethod.getMethod() + "\" " + url + " has already exist, that old one will be covered");
                    }

                    restHolder.put(httpMethod, option);
                }
            }
        };
    }

    /*public static Consumer<Controller.Control> initControlMethod(HttpServer server, Class<?> klass, Object handler, Method method) {
        return control -> {
            Controller.Api[] apis = control.api();

            logger.debug(control.toString());
            logger.debug(klass.getTypeName() + "#" + method.getName());

            for (Controller.Api api : apis) {
                List<String> httpMethods = Stream.of(api.methods())
                        .map(HttpMethod::getMethod)
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
                        logger.error("handler execute error", e);
                        ResponseUtil.end(httpExchange, 500, e.getMessage());
                    } catch (Message e) {
                        logger.error(e.getMessage(), e);
                        ResponseUtil.end(httpExchange, 501, e.getMessage());
                    }
                });
            }
        };
    }*/

}
