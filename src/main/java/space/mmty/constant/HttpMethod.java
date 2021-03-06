package space.mmty.constant;

public enum HttpMethod {
    OPTIONS("OPTIONS"),
    GET("GET"),
    POST("POST"),
    PATCH("PATCH"),
    PUT("PUT"),
    DELETE("DELETE"),
    COPY("COPY"),
    HEAD("HEAD"),
    LINK("LINK"),
    UNLINK("UNLINK"),
    PURGE("PURGE"),
    LOCK("LOCK"),
    UNLOCK("UNLOCK"),
    PROPFIND("PROPFIND"),
    VIEW("VIEW");

    private final String method;
    HttpMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
