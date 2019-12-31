package com.leelovejava.rocketmq.consumer;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.io.UnsupportedEncodingException;

/**
 * 消费消息
 * @author leelovejava
 */
public class ConsumerDemo {

    public static void main(String[] args) throws Exception {
        // DefaultMQPushConsumer实现了自动保存offset值以及实现多个consumer的负载均衡
        // 设置组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("haoke-consumer");
        consumer.setNamesrvAddr("127.0.0.1:9876");

        // 订阅方式:

        // 1. 订阅消息,接收此Topic下所有消息
        /// consumer.subscribe("my-topic", "*");

        // 2. 完整匹配
        ///consumer.subscribe("haoke_im_topic", "SEND_MSG");

        // 3. 或匹配
        consumer.subscribe("my-topic", "add || update");

        // 消息模式:
        // 1. 集群模式: 同一个 ConsumerGroup(GroupName相同) 里的每 个 Consumer 只消费所订阅消息的一部分内容， 同一个 ConsumerGroup 里所有的 Consumer消费的内容合起来才是所订阅 Topic 内容的整体， 从而达到负载均衡的目的
        consumer.setMessageModel(MessageModel.CLUSTERING);

        // 2. 广播模式: 同一个 ConsumerGroup里的每个 Consumer都 能消费到所订阅 Topic 的全部消息，也就是一个消息会被多次分发，被多个 Consumer消费
        /// consumer.setMessageModel(MessageModel.BROADCASTING);

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
