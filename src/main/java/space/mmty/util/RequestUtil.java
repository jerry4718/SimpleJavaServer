package space.mmty.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequestUtil {

    public static ParamGetter packParams(String query) {
        return new ParamGetter(parseParams(query));
    }

    public static Map<String, List<String>> parseParams(String query) {
        Map<String, List<String>> params = new HashMap<>();
        if (query == null) {
            return params;
        }
        String[] splits = query.replaceFirst("^\\?", "").split("&");
        // logger.info(JSONObject.toJSONString(splits));

        for (String split : splits) {
            List<String> pair = Arrays.asList(split.split("="));
            // logger.info(JSONObject.toJSONString(pair));

            String key = "", value = "";
            if (pair.size() >= 1) {
                key = pair.get(0);
            }
            if (pair.size() >= 2) {
                value = pair.get(1);
            }
            if (params.containsKey(key)) {
                params.get(key).add(value);
            } else {
                ArrayList<String> values = new ArrayList<>();
                values.add(value);
                params.put(key, values);
            }
        }
        return params;
    }

    public static String getParam(Map<String, List<String>> params, String paramName, String defaultValue) {
        if (params.containsKey(paramName)) {
            List<String> values = params.get(paramName);
            if (!values.isEmpty()) {
                return values.get(0);
            }
        }
        return defaultValue;
    }

    public static <T> T getParam(Map<String, List<String>> params, String paramName, Function<String, T> converter, Supplier<T> defaultValue) {
        if (params.containsKey(paramName)) {
            List<String> values = params.get(paramName);
            if (!values.isEmpty()) {
                return converter.apply(values.get(0));
            }
        }
        if (defaultValue == null) {
            return null;
        }
        return defaultValue.get();
    }

    public static class ParamGetter {
        private final Map<String, List<String>> params;

        private ParamGetter(Map<String, List<String>> params) {
            this.params = params;
        }

        public <T> T getParam(String paramName, Function<String, T> converter, Supplier<T> defaultValue) {
            return RequestUtil.getParam(params, paramName, converter, defaultValue);
        }

        public String getParam(String paramName, String defaultValue) {
            return RequestUtil.getParam(params, paramName, defaultValue);
        }

        public String getParam(String paramName) {
            return RequestUtil.getParam(params, paramName, null);
        }
    }
}
