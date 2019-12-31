package com.leelovejava.rocketmq.sendmsg;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * 发送消息(异步)
 * @author leelovejava
 */
public class AsyncProducer {

    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("haoke");

        producer.setNamesrvAddr("127.0.0.1:9876");
        // 发送失败的重试次数
        producer.setRetryTimesWhenSendAsyncFailed(0);

        producer.start();

        // 发送消息
        String msg = "我的第一个异步发送消息!";
        Message message = new Message("my-topic", msg.getBytes("UTF-8"));
        producer.send(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                System.out.println("发送成功了!" + sendResult);
                System.out.println("消息id：" + sendResult.getMsgId());
                System.out.println("消息队列：" + sendResult.getMessageQueue());
                System.out.println("消息offset值：" + sendResult.getQueueOffset());
            }

            @Override
            public void onException(Throwable e) {
                System.out.println("消息发送失败!" + e);
            }
        });

        System.out.println("发送成功!");

        // 注意： producer.shutdown()要注释掉，否则发送失败。原因是，异步发送，还未来得及发送就被关闭了
        // producer.shutdown();
    }
}
