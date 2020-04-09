package com.leelovejava.rocketmq.delay;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.concurrent.TimeUnit;

/**
 * 生产者
 *
 * @author leelovejava
 */
public class Producer {

    /**
     * 发送延时消息
     * @param args
     * @throws InterruptedException
     * @throws RemotingException
     * @throws MQClientException
     * @throws MQBrokerException
     */
    public static void main(String[] args) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        //1.创建消息生产者producer，并制定生产者组名
        DefaultMQProducer producer = new DefaultMQProducer("group1");
        //2.指定Nameserver地址
        producer.setNamesrvAddr("192.168.25.135:9876;192.168.25.138:9876");
        //3.启动producer
        producer.start();

        for (int i = 0; i < 10; i++) {
            //4.创建消息对象，指定主题Topic、Tag和消息体
            /**
             * 参数一：消息主题Topic
             * 参数二：消息Tag
             * 参数三：消息内容
             */
            Message msg = new Message("DelayTopic", "Tag1", ("Hello World" + i).getBytes());

            // 设置延时等级3,这个消息将在10s之后发送(现在只支持固定的几个时间,详看delayTimeLevel)
            /// private String messageDelayLevel = "1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h";
            // 现在RocketMq并不支持任意时间的延时，需要设置几个固定的延时等级，从1s到2h分别对应着等级1到18
            msg.setDelayTimeLevel(3);

            // 5.发送消息
            SendResult result = producer.send(msg);
            // 发送状态
            /// SendStatus status = result.getSendStatus();

            System.out.println("发送结果:" + result);

            // 线程睡1秒
            TimeUnit.SECONDS.sleep(1);
        }

        // 6.关闭生产者producer
        producer.shutdown();
    }

}
