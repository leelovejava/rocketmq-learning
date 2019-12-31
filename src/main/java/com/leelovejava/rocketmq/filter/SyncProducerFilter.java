package com.leelovejava.rocketmq.filter;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * 生产者 消息过滤器
 *
 * RocketMQ支持根据用户自定义属性进行过滤，过滤表达式类似于SQL的where，如：a> 5 AND b ='abc
 * @author leelovejava
 */
public class SyncProducerFilter {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("haoke");

        producer.setNamesrvAddr("127.0.0.1:9876");

        producer.start();

        // 发送消息
        String msg = "这是一个用户的消息, id = 1003";
        Message message = new Message("my-topic-filter", "delete", msg.getBytes("UTF-8"));
        message.putUserProperty("sex","男");
        message.putUserProperty("age","20");

        SendResult sendResult = producer.send(message);
        System.out.println("消息id：" + sendResult.getMsgId());
        System.out.println("消息队列：" + sendResult.getMessageQueue());
        System.out.println("消息offset值：" + sendResult.getQueueOffset());
        System.out.println(sendResult);

        producer.shutdown();
    }
}
