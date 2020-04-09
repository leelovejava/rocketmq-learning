package com.leelovejava.rocketmq.filter.sql;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * 消费者 消息过滤
 *
 * @author leelovejava
 */
public class Consumer {

    /**
     * 消息过滤
     * 1) 数值比较，比如：>，>=，<，<=，BETWEEN，=
     * 2) 字符比较，比如：**=，<>，IN
     * 3) IS NULL 或者 IS NOT NULL
     * 4) 逻辑符号 AND，OR，NOT
     * <p>
     * 常量支持类型为：
     * 1) 数值,比如：123,3.1415
     * 2) 字符,比如：abc,必须用单引号包裹起来
     * 3) NULL,特殊的常量
     * 4) 布尔值,TRUE或 FALSE
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 1.创建消费者Consumer，制定消费者组名
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group1");
        // 2.指定Nameserver地址
        consumer.setNamesrvAddr("192.168.25.135:9876;192.168.25.138:9876");
        // 3.订阅主题Topic和Tag
        // 只有使用push模式的消费者才能用使用SQL92标准的sql语句
        // public void subscribe(finalString topic, final MessageSelector messageSelector)
        consumer.subscribe("FilterSQLTopic", MessageSelector.bySql("i>5"));
        /// consumer.subscribe("FilterSQLTopic",MessageSelector.bySql("sex='女' AND age>=18"));

        // 4.设置回调函数，处理消息
        // 接受消息内容
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                System.out.println("consumeThread=" + Thread.currentThread().getName() + "," + new String(msg.getBody()));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        //5.启动消费者consumer
        consumer.start();
        System.out.println("消费者启动");
    }
}
