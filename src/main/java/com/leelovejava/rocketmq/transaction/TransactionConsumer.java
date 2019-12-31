package com.leelovejava.rocketmq.transaction;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 事务消费者
 *
 * @author leelovejava
 */
public class TransactionConsumer {
    /**
     * 分布式事务消息
     *
     * 回顾什么事务
     * 聊什么是事务，最经典的例子就是转账操作，用户A转账给用户B1000元的过程如下：
     * 用户A发起转账请求，用户A账户减去1000元
     * 用户B的账户增加1000元
     * 如果，用户A账户减去1000元后，出现了故障（如网络故障），那么需要将该操作回滚，用户A账户增加1000元
     *
     * 分布式事务
     * 随着项目越来越复杂，越来越服务化，就会导致系统间的事务问题，这个就是分布式事务问题。
     * 分布式事务分类有这几种：
     *  基于单个JVM，数据库分库分表了（跨多个数据库）。
     *  基于多JVM，服务拆分了（不跨数据库）。
     *  基于多JVM，服务拆分了 并且数据库分库分表了。
     * 解决分布式事务问题的方案有很多，使用消息实现只是其中的一种
     *
     * 执行流程
     * 1. 发送方向 MQ 服务端发送消息。
     * 2. MQ Server 将消息持久化成功之后，向发送方 ACK 确认消息已经发送成功，此时消息为半消息。
     * 3. 发送方开始执行本地事务逻辑。
     * 4. 发送方根据本地事务执行结果向 MQ Server 提交二次确认（Commit 或是 Rollback），MQ Server 收到
     *  Commit 状态则将半消息标记为可投递，订阅方最终将收到该消息；MQ Server 收到 Rollback 状态则删除半
     * 消息，订阅方将不会接受该消息。
     * 5. 在断网或者是应用重启的特殊情况下，上述步骤4提交的二次确认最终未到达 MQ Server，经过固定时间后
     *  MQ Server 将对该消息发起消息回查。
     * 6. 发送方收到消息回查后，需要检查对应消息的本地事务执行的最终结果。
     * 7. 发送方根据检查得到的本地事务的最终状态再次提交二次确认，MQ Server 仍按照步骤4对半消息进行操作
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer consumer = new
                DefaultMQPushConsumer("HAOKE_CONSUMER");
        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费<br>
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.setNamesrvAddr("127.0.0.1:9876");

        // 订阅topic，接收此Topic下的所有消息
        consumer.subscribe("pay_topic", "*");

        // 测试结果: 返回commit状态时，消费者能够接收到消息，返回rollback状态时，消费者接受不到消息
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                try {
                    System.out.println(new String(msg.getBody(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
    }
}