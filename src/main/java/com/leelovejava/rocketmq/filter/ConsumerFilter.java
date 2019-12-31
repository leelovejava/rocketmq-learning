package com.leelovejava.rocketmq.filter;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 消费者 过滤器
 *
 * 默认配置下，不支持自定义属性，需要设置开启
 * # 加入到broker的配置文件中
 * enablePropertyFilter=true
 * 重启broker
 * @author leelovejava
 */
public class ConsumerFilter {

    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("haoke-consumer");
        consumer.setNamesrvAddr("127.0.0.1:9876");

        // 订阅消息，接收的是所有消息
        /// consumer.subscribe("my-topic", "*");

        // 过滤器
        consumer.subscribe("my-topic-filter", MessageSelector.bySql("sex='女' AND age>=18"));

        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {

            try {
                for (MessageExt msg : msgs) {
                    System.out.println("消息:" + new String(msg.getBody(), "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            System.out.println("接收到消息 -> " + msgs);

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        // 启动消费者
        consumer.start();

    }
}
