package com.leelovejava.rocketmq.filter.sql;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;

import java.util.concurrent.TimeUnit;

/**
 * 生产者
 *
 * @author leelovejava
 */
public class Producer {


    public static void main(String[] args) throws Exception {
        // 1.创建消息生产者producer，并制定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("group1");
        // 2.指定Nameserver地址
        producer.setNamesrvAddr("192.168.25.135:9876;192.168.25.138:9876");
        // 3.启动producer
        producer.start();

        for (int i = 0; i < 10; i++) {
            // 4.创建消息对象，指定主题Topic、Tag和消息体
            /**
             * 参数一：消息主题Topic
             * 参数二：消息Tag
             * 参数三：消息内容
             */
            Message msg = new Message("FilterSQLTopic", "Tag1", ("Hello World" + i).getBytes());

            // 设置属性,消费者用MessageSelector.bySql来使用sql筛选消息
            msg.putUserProperty("i", String.valueOf(i));
            /// msg.putUserProperty("sex","男");
            /// msg.putUserProperty("age","20");

            // 5.发送消息
            SendResult result = producer.send(msg);
            System.out.println("发送结果:" + result);
            System.out.println("消息发送状态:：" + result.getSendStatus());
            System.out.println("消息id：" + result.getMsgId());
            System.out.println("消息队列：" + result.getMessageQueue());
            System.out.println("消息offset值：" + result.getQueueOffset());


            // 线程睡1秒
            TimeUnit.SECONDS.sleep(2);
        }

        // 6.关闭生产者producer
        producer.shutdown();
    }

}
