package space.mmty.util;

public class SysEnv {
    private static final String pro = "pro";
    private static final String test = "test";
    private static final String dev = "dev";

    private static String environment = null;

    static <T> T envCaller(T onPro, T onTest, T onDev) {
        switch (getEnvironment()) {
            case pro:
                return onPro;
            case test:
                return onTest;
            case dev:
                return onDev;
        }
        return onDev;
    }

    public static String getEnvironment() {
        if (environment == null) {
            environment = "pro";
        }
        return environment;
    }
}
