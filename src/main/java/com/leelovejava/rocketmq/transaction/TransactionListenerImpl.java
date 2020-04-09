package com.leelovejava.rocketmq.transaction;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.HashMap;
import java.util.Map;

/**
 * 事务监听者
 * 本地事务处理
 * (2)Implement the TransactionListener interface
 * The “executeLocalTransaction” method is used to execute local transaction when send half message succeed.
 * It returns one of three transaction status mentioned in the previous section.
 * The “checkLocalTransaction” method is used to check the local transaction status and respond to MQ check requests.
 * It also returns one of three transaction status mentioned in the previous section.
 *
 * @author leelovejava
 */
public class TransactionListenerImpl implements TransactionListener {

    private static Map<String, LocalTransactionState> STATE_MAP = new HashMap<>();

    /**
     * 执行具体的业务逻辑(本地事务)
     *
     * @param msg 发送的消息对象
     * @param arg
     * @return
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            System.out.println("用户A账户减500元.");
            // 模拟调用服务
            Thread.sleep(500);
            // System.out.println(1/0);
            System.out.println("用户B账户加500元.");
            Thread.sleep(800);
            STATE_MAP.put(msg.getTransactionId(), LocalTransactionState.COMMIT_MESSAGE);
            // 二次提交确认
            // return LocalTransactionState.UNKNOW;
            return LocalTransactionState.COMMIT_MESSAGE;

        } catch (Exception e) {
            e.printStackTrace();
        }

        STATE_MAP.put(msg.getTransactionId(), LocalTransactionState.ROLLBACK_MESSAGE);
        // 回滚
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }

    /**
     * 消息回查
     *
     * @param msg
     * @return
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        // 由于RocketMQ迟迟没有收到消息的确认消息，因此主动询问这条prepare消息，是否正常？
        // 可以查询数据库看这条数据是否已经处理
        System.out.println("状态回查 ---> " + msg.getTransactionId() + " " + STATE_MAP.get(msg.getTransactionId()));
        return STATE_MAP.get(msg.getTransactionId());
    }
}
