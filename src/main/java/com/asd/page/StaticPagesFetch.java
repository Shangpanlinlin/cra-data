package com.asd.page;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

public class StaticPagesFetch {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticPagesFetch.class);

    private static final String HOME_ADDR= "https://www.paramountmerchandise.co.nz/";

private HttpClient httpClient = HttpClientBuilder.create().build();

    public static void main(String[] args) {
        StaticPagesFetch staticPagesFetch = new StaticPagesFetch();
        try {
            String home = staticPagesFetch.doGet(HOME_ADDR, null, null);
            File file = new File("param_web_pages/home.html"); // relative path related to the project path
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(home.getBytes(),0, home.length());
            System.out.println(file.getPath());
            if(!file.exists())
                file.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String doGet(String url, Map<String, String> params, String encode) throws Exception {
        LOGGER.info("执行GET请求，URL = {}", url);
        if(null != params){
            URIBuilder builder = new URIBuilder(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.setParameter(entry.getKey(), entry.getValue());
            }
            url = builder.build().toString();
        }
        // 创建http GET请求
        HttpGet httpGet = new HttpGet(url);
       // httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = (CloseableHttpResponse) httpClient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                if(encode == null){
                    encode = "UTF-8";
                }
                return EntityUtils.toString(response.getEntity(), encode);
            }
        } finally {
            if (response != null) {
                response.close();
            }
            // 此处不能关闭httpClient，如果关闭httpClient，连接池也会销毁
        }
        return null;
    }
}
