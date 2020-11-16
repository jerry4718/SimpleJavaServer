package space.mmty.controller;

import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import space.mmty.annotation.Controller;
import space.mmty.config.WxConfig;
import space.mmty.constant.HttpMethods;
import space.mmty.exception.Message;
import space.mmty.util.HttpUtil;
import space.mmty.util.RequestUtil;
import space.mmty.util.SecuritySHA1Utils;

import java.text.MessageFormat;
import java.util.*;

@Controller(api = {
        @Controller.Api(url = "/wxSignature", methods = {HttpMethods.GET, HttpMethods.POST})
})
public class WxSignatureController extends BaseController {
    private static final Logger logger = Logger.getLogger(WxSignatureController.class);

    @Override
    public String control(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        logger.info(query);

        Map<String, List<String>> params = RequestUtil.parseParams(query);

        // url参数
        String url = params.get("url").get(0);


        // 拼接上传参数 获取access_token
        String tokenParams = MessageFormat.format("grant_type=client_credential&appid={0}&secret={1}", WxConfig.APP_ID, WxConfig.APP_SECRET);
        String tokenResultStr = HttpUtil.fastGet(WxConfig.GET_COMMON_TOKEN_URL + tokenParams);

        logger.info(tokenResultStr);

        if (tokenResultStr == null) {
            throw new Message("access_token 请求失败。");
        }

        JSONObject jsonObject = JSONObject.parseObject(tokenResultStr);

        String accessToken = jsonObject.getString("access_token");

        // 判断获取access_token是否成功！
        if (StringUtils.isEmpty(accessToken)) {
            logger.error("获取公众号普通access_token失败，微信返回数据：" + tokenResultStr);
            throw new Message("获取微信数据失败。");
        }

        String ticketParams = MessageFormat.format("access_token={0}&type=jsapi", accessToken);
        String ticketResultStr = HttpUtil.fastGet(WxConfig.JSAPI_TICKET_URL + ticketParams);

        logger.info(ticketResultStr);

        if (ticketResultStr == null) {
            throw new Message("jsapi_ticket 请求失败。");
        }

        String ticket = jsonObject.getString("ticket");

        if (StringUtils.isEmpty(ticket)) {
            logger.error("获取jsapi_ticket临时票据，返回值为 空。url：" + WxConfig.JSAPI_TICKET_URL + " 参数：" + tokenParams);
            throw new Message("获取微信信息异常，请稍后重试。");
        }

        // 生成签名
        SortedMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.put("noncestr", UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32));
        sortedMap.put("timestamp", (System.currentTimeMillis() + "").substring(0, 10));

        String inStr = "jsapi_ticket=" + ticket +
                "&noncestr=" + sortedMap.get("noncestr") +
                "&timestamp=" + sortedMap.get("timestamp") +
                "&url=" + url;

        String signature = SecuritySHA1Utils.shaEncode(inStr);

        sortedMap.put("signature", signature);

        return JSONObject.toJSONString(sortedMap);
    }
}
