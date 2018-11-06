package com.asd.crawler;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {
        // 初始化spring容器
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring/*.xml");
        Map<String, Crawler> map = applicationContext.getBeansOfType(Crawler.class);

        for (Map.Entry<String, Crawler> entry : map.entrySet()) {
            try {
                entry.getValue().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
