package space.mmty.util;

import java.util.*;

public class RequestUtil {
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
}
