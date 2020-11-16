package space.mmty.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import space.mmty.exception.Message;
import space.mmty.exception.Thrower;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class HttpUtil {
    private static Logger logger = Logger.getLogger(HttpUtil.class);
    private static HttpClient httpClient;
    private static HttpClient httpsClient;
    /**
     * 如果请求响应比较快，则选用这个超时设定
     */
    private static RequestConfig requestFastConfig;

    /**
     * 如果请求响应比较慢，则选用这个超时设定
     */
    private static RequestConfig requestSlowConfig;


    private static final Function<Supplier<PoolingHttpClientConnectionManager>, HttpClient> clientBuilder = managerGetter -> {
        // PoolingHttpClientConnectionManager manager = managerGetter.get();
        // if (manager == null) return null;
        // manager.setMaxTotal(128);
        // manager.setDefaultMaxPerRoute(128);
        // return HttpClients.custom().setConnectionManager(manager).build();
        return HttpClients.createDefault();
    };

    public HttpClient getClient() {
        return HttpClients.createDefault();
    }

    private static final Function<String, HttpClient> clientGetter = url -> {
        if (httpClient != null) return httpClient;
        return httpClient = clientBuilder.apply(PoolingHttpClientConnectionManager::new);
        // 下面是自动切换https方式请求的功能，因为duitku的请求会出现异常，所以暂时取消掉
        // if (url.startsWith("https://")) {
        // 	if (httpsClient != null) return httpsClient;
        // 	Supplier<PoolingHttpClientConnectionManager> managerSupplier = () -> {
        // 		try {
        // 			//采用绕过验证的方式处理https请求
        // 			SSLContext sslcontext = createIgnoreVerifySSL();
        //
        // 			//设置协议http和https对应的处理socket链接工厂的对象
        // 			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        // 					.register("http", PlainConnectionSocketFactory.INSTANCE)
        // 					.register("https", new SSLConnectionSocketFactory(sslcontext))
        // 					.build();
        // 			return new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 		} catch (NoSuchAlgorithmException | KeyManagementException e) {
//					logger.error("构建httpsClientConnectionManager失败", e);
        // 			return null;
        // 		}
        // 	};
        // 	return httpsClient = clientBuilder.apply(managerSupplier);
        // } else {
        // 	if (httpClient != null) return httpClient;
        // 	return httpClient = clientBuilder.apply(PoolingHttpClientConnectionManager::new);
        // }
    };

    /**
     * 测试环境调用印尼提供的接口，多数情况下会比较慢，
     * 所以这里会根据环境变量来设定超时时间
     */
    private static RequestConfig getRequestFastConfig() {
        if (requestFastConfig == null) {
            requestFastConfig = EnvUtils.envToBuild(
                    RequestConfig.custom(),

                    // Pro环境
                    custom -> custom.setConnectTimeout(10000)
                            .setConnectionRequestTimeout(30000)
                            .setSocketTimeout(30000),

                    // Test环境，Dev环境
                    custom -> custom.setConnectTimeout(9000)
                            .setConnectionRequestTimeout(9000)
                            .setSocketTimeout(9000)
                            .setProxy(proxy),

                    RequestConfig.Builder::build
            );
        }

        return requestFastConfig;
    }

    private static RequestConfig getRequestSlowConfig() {
        if (requestSlowConfig == null) {
            requestSlowConfig = RequestConfig.custom()
                    .setConnectTimeout(10000)
                    .setConnectionRequestTimeout(30000)
                    .setSocketTimeout(30000)
                    .build();
        }
        return requestSlowConfig;
    }

    private static HttpHost proxy;

    public static void setProxy(HttpHost proxy) {
        if (!EnvUtils.isPro()) {
            HttpUtil.proxy = proxy;
            return;
        }
    }

    public static String fastGet(String url, List<? extends Header> headers, List<NameValuePair> params) {
        return apacheGet(url, headers, params, HttpUtil::getRequestSlowConfig);
    }

    public static String slowGet(String url, List<? extends Header> headers, List<NameValuePair> params) {
        return apacheGet(url, headers, params, HttpUtil::getRequestSlowConfig);
    }

    public static String fastGet(String url, List<? extends Header> headers) {
        return apacheGet(url, headers, null, HttpUtil::getRequestSlowConfig);
    }

    public static String slowGet(String url, List<? extends Header> headers) {
        return apacheGet(url, headers, null, HttpUtil::getRequestSlowConfig);
    }

    public static String fastGet(String url) {
        return apacheGet(url, HttpUtil::getRequestSlowConfig);
    }

    public static String slowGet(String url) {
        return apacheGet(url, HttpUtil::getRequestSlowConfig);
    }

    public static String apacheGet(String url, Supplier<RequestConfig> configGetter) {
        return apacheGet(url, null, null, configGetter);
    }

    public static String apacheGet(String url, List<? extends Header> headers, List<NameValuePair> params, Supplier<RequestConfig> configGetter) {
        String result = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            if (params != null) {
                uriBuilder.setParameters(params);
            }

            URI uri = uriBuilder.build();

            logger.info(uri.toString());

            HttpGet httpGet = new HttpGet(uri);

            if (headers != null) {
                headers.forEach(httpGet::addHeader);
            } else {
                httpGet.addHeader(new BasicHeader("Content-Type", "application/json"));
            }

            httpGet.setConfig(configGetter.get());

            HttpClient requestClient = clientGetter.apply(url);

            if (requestClient == null) throw Thrower.msg("构建requestClient失败");

            HttpResponse response = requestClient.execute(httpGet);
            result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            logger.info(result);
        } catch (SocketTimeoutException e) {
            logger.error("apacheGet请求超时, {}", e);
        } catch (IOException | Message | URISyntaxException e) {
            logger.error("apacheGet请求失败, {}", e);
        }
        return result;
    }

    public static String fastPost(String url, List<? extends Header> headers, JSONObject body) {
        return apachePost(url, headers, body, HttpUtil::getRequestSlowConfig);
    }

    public static String slowPost(String url, List<? extends Header> headers, JSONObject body) {
        return apachePost(url, headers, body, HttpUtil::getRequestSlowConfig);
    }

    public static String fastPost(String url, JSONObject body) {
        return apachePost(url, body, HttpUtil::getRequestSlowConfig);
    }

    public static String slowPost(String url, JSONObject body) {
        return apachePost(url, body, HttpUtil::getRequestSlowConfig);
    }

    public static String apachePost(String url, JSONObject body, Supplier<RequestConfig> configGetter) {
        return apachePost(url, null, body, configGetter);
    }

    public static String apachePost(String url, List<? extends Header> headers, JSONObject body, Supplier<RequestConfig> configGetter) {
        String result = null;
        try {

            HttpPost httpPost = new HttpPost(url);

            if (headers != null) {
                headers.forEach(httpPost::addHeader);
            } else {
                httpPost.addHeader(new BasicHeader("Content-Type", "application/json"));
            }

            if (body != null) {
                String paramsStr = body.toJSONString();
                logger.info("apachePost: " + paramsStr);
                StringEntity stringEntity = new StringEntity(paramsStr, StandardCharsets.UTF_8);
                httpPost.setEntity(stringEntity);
            }
            httpPost.setConfig(configGetter.get());


            HttpClient requestClient = clientGetter.apply(url);

            if (requestClient == null) throw Thrower.msg("构建requestClient失败");

            HttpResponse response = requestClient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        } catch (SocketTimeoutException e) {
            logger.error("apachePost请求超时, {}", e);
        } catch (IOException e) {
            logger.error("apachePost请求失败, {}", e);
        } catch (Message message) {
            logger.error(message.getMessage());
        }
        return result;
    }

    /**
     * 绕过验证
     */
    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSLv3");
        sslContext.init(null, new TrustManager[]{new IgnoreVerifySSLManager()}, null);
        return sslContext;
    }

    // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
    static class IgnoreVerifySSLManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
