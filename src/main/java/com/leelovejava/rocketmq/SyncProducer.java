package com.leelovejava.rocketmq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * 同步生产者
 *
 * @author leelovejava
 */
public class SyncProducer {

    /**
     * message的结构
     * Topic 必填，线下环境不需要申请，线上环境需要申请后才能使用
     * Body  必填，二进制形式，序列化由应用决定，Producer 与 Consumer 要协商好序列化形式
     * Tags  选填，类似于 Gmail 为每封邮件设置的标签，方便服务器过滤使用。目前只支持每个消息设置一个 tag，所以也可以类比为 Notify 的 MessageType 概念
     * Keys  选填，代表这条消息的业务关键词，服务器会根据 keys 创建哈希索引，设置后，可以在 Console 系统根据 Topic、Keys 来查询消息，由于是哈希索引，请尽可能保证 key 唯一，例如订单号，商品 Id 等
     * Flag  选填，完全由应用来设置，RocketMQ 不做干预
     * DelayTimeLevel 选填，消息延时级别，0 表示不延时，大于 0 会延时特定的时间才会被消费 默认0
     * WaitStoreMsgOK 选填，表示消息是否在服务器落盘后才返回应答 默认TRUE
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        //Instantiate with a producer group name.
        DefaultMQProducer producer = new
                DefaultMQProducer("test-group");
        // Specify name server addresses.
        producer.setNamesrvAddr("127.0.0.1:9876");
        //Launch the instance.
        producer.start();
        for (int i = 0; i < 100; i++) {
            //Create a message instance, specifying topic, tag and message body.
            Message msg = new Message("TopicTest11",
                    "TagA",
                    ("Hello RocketMQ " +
                            i).getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            //Call send message to deliver message to one of brokers.
            SendResult sendResult = producer.send(msg);
            System.out.printf("%s%n", sendResult);


            System.out.println("消息状态：" + sendResult.getSendStatus());
            System.out.println("消息id：" + sendResult.getMsgId());
            System.out.println("消息queue：" + sendResult.getMessageQueue());
            System.out.println("消息offset：" + sendResult.getQueueOffset());
            producer.shutdown();

            /**
             * 消息状态：SEND_OK
             * 消息id：AC1037A0307418B4AAC2374062400000
             * 消息queue：MessageQueue [topic=haoke_im_topic, brokerName=broker_haoke_im, queueId=6]
             * 消息offset：0
             */
        }
    }
}