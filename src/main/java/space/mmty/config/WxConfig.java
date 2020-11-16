package space.mmty.config;

public interface WxConfig {
    // 微信公众号APP_ID
    String APP_ID = "wx_APP_ID";
    // 微信公众号APP_Secret
    String APP_SECRET = "wx_APP_SECRET";

    // 普通获取access_token方式的URL
    String GET_COMMON_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?";
    // 通过access_token获取jsapi_ticket（公众号用于调用微信JS接口的临时票据）
    String JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?";
}
