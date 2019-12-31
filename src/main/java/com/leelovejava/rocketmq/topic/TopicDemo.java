package com.leelovejava.rocketmq.topic;

import org.apache.rocketmq.client.producer.DefaultMQProducer;

/**
 * 创建Topic
 *
 * @author leelovejava
 */
public class TopicDemo {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("haoke");

        // 设置nameserver的地址
        producer.setNamesrvAddr("127.0.0.1:9876");

        // 启动生产者
        producer.start();

        /**
         * 创建topic，参数分别是:
         * key：broker名称
         * newTopic：topic名称
         * queueNum：队列数（分区）
         *
         */
        producer.createTopic("broker_haoke_im", "my-topic", 8);

        System.out.println("topic创建成功!");

        producer.shutdown();
    }
}
