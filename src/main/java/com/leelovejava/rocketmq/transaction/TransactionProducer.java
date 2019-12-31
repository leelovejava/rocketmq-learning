package com.leelovejava.rocketmq.transaction;

import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.util.concurrent.*;

/**
 * 事务生产者
 * (1)Create the transactional producer
 * Use TransactionMQProducer class to create producer client,
 * and specify a unique producerGroup, and you can set up a custom thread pool to process check requests.
 * After executing the local transaction,
 * you need to reply to MQ according to the execution result，
 * and the reply status is described in the above section
 *
 * @author leelovejava
 * @see 'http://rocketmq.apache.org/docs/transaction-example/'
 */
public class TransactionProducer {

    public static void main(String[] args) throws Exception {

        ///TransactionMQProducer producer = new TransactionMQProducer("transaction_producer");

        TransactionListener transactionListener = new TransactionListenerImpl();
        TransactionMQProducer producer = new TransactionMQProducer("please_rename_unique_group_name");
        // 当RocketMQ发现`Prepared消息`时，会根据这个Listener实现的策略来决断事务
        ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("client-transaction-msg-check-thread");
                    return thread;
                });
        producer.setExecutorService(executorService);

        producer.setNamesrvAddr("127.0.0.1:9876");

        // 设置事务监听器
        ///producer.setTransactionListener(new TransactionListenerImpl());
        producer.setTransactionListener(transactionListener);
        producer.start();

        // 发送消息
        Message message = new Message("pay_topic", "用户A给用户B转账500元".getBytes("UTF-8"));
        producer.sendMessageInTransaction(message, null);

        Thread.sleep(999999);
        producer.shutdown();
    }
}