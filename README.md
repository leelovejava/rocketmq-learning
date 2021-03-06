# RocketMQ

## 面试题

[你了解RocketMQ对分布式事务支持的底层实现原理吗？](https://github.com/shishan100/Java-Interview-Advanced/blob/master/docs/distributed-system/eventual-consistency.md)

[在搭建好的电商系统里，如何基于RocketMQ最终一致性事务进行落地开发？](https://github.com/shishan100/Java-Interview-Advanced/blob/master/docs/distributed-system/rocketmq-eventual-consistency.md)

[如果公司没有RocketMQ中间件，那你们如何实现最终一致性事务？](https://github.com/shishan100/Java-Interview-Advanced/blob/master/docs/distributed-system/eventual-consistency.md)

## 课程介绍
1. 理解消息中间件MQ的优势和应用场景
2. 掌握RocketMQ的核心功能，以及各种消息发送案例
3. 通过电商项目深刻理解RocketMQ在使用项目中的落地应用
4. 通过RocketMQ高级功能和源码学习，对RocketMQ的技术细节和原理有更加透彻的理解

* 了解什么RocketMQ
* 了解RocketMQ的核心概念
* 动手安装RocketMQ服务
* 快速入门，掌握RocketMQ的api使用
* 对producer、consumer进行详解
* 了解RocketMQ的存储特点

## 1、RocketMQ简介与安装

### 1.1、RocketMQ简介
Apache RocketMQ是一个采用Java语言开发的分布式的消息系统，由阿里巴巴团队开发，与2016年底贡献给
Apache，成为了Apache的一个顶级项目。
在阿里内部，RocketMQ 很好地服务了 集 团大大小小上千个应用，在每年的双十一当天，更有不可思议的万亿级
消息通过 RocketMQ 流转(在 2017 年的双十一当天，整个阿里巴巴集团通过 RocketMQ 流转的线上消息达到了 万
亿级，峰值 TPS 达到 5600 万)，在阿里大中台策略上发挥着举足轻重的作用

### 1.3、核心概念说明

* Producer
    消息生产者，负责产生消息，一般由业务系统负责产生消息。
 > Producer Group
    一类 Producer 的集合名称，这类 Producer 通常发送一类消息，且发送逻辑一致。

* Consumer
    
    消息费者，负责消费消息，一般是后台系统负责异步消费。
    
    Push Consumer
        
        服务端向消费者端推送消息
    
    Pull Consumer
        消费者端向服务定时拉取消息

    Consumer Group
        一类 Consumer 的集合名称，这类 Consumer 通常消费一类消息，且消费逻辑一致。

* NameServer
    集群架构中的组织协调员
    
    收集broker的工作情况
    
    不负责消息的处理
    
* Broker
    
   是RocketMQ的核心负责消息的发送、接收、高可用等（真正干活的）
    
   需要定时发送自身情况到NameServer，默认10秒发送一次，超时2分钟会认为该broker失效。

* Topic
    不同类型的消息以不同的Topic名称进行区分，如User、Order等
    
    是逻辑概念

    Message Queue
        消息队列，用于存储消息
        

## 2、快速入门

#### 2.2.1、Message数据结构
| 字段名         | 默认 值 | 说明                                                         |
| -------------- | ------- | ------------------------------------------------------------ |
| Topic          | null    | 必填，线下环境不需要申请，线上环境需要申请后才能使用         |
| Body           | null    | 必填，二进制形式，序列化由应用决定，Producer 与 Consumer 要协商好 序列化形式。 |
| Tags           | null    | 选填，类似于 Gmail 为每封邮件设置的标签，方便服务器过滤使用。目前只 支持每个消息设置一个 tag，所以也可以类比为 Notify 的 MessageType 概 念 |
| Keys           | null    | 选填，代表这条消息的业务关键词，服务器会根据 keys 创建哈希索引，设置 后，可以在 Console 系统根据 Topic、Keys 来查询消息，由于是哈希索引， 请尽可能保证 key 唯一，例如订单号，商品 Id 等。 |
| Flag           | 0       | 选填，完全由应用来设置，RocketMQ 不做干预                    |
| DelayTimeLevel | 0       | 选填，消息延时级别，0 表示不延时，大于 0 会延时特定的时间才会被消费 |
| WaitStoreMsgOK | TRUE    | 选填，表示消息是否在服务器落盘后才返回应答。                 |

### 3.2、分布式事务消息

#### 3.2.1、回顾什么事务

聊什么是事务，最经典的例子就是转账操作，用户A转账给用户B1000元的过程如下：
* 用户A发起转账请求，用户A账户减去1000元
* 用户B的账户增加1000元
如果，用户A账户减去1000元后，出现了故障（如网络故障），那么需要将该操作回滚，用户A账户增加1000元
这就是事务。

#### 3.2.2、分布式事务
随着项目越来越复杂，越来越服务化，就会导致系统间的事务问题，这个就是分布式事务问题。

分布式事务分类有这几种：

   基于单个JVM，数据库分库分表了（跨多个数据库）。
    
   基于多JVM，服务拆分了（不跨数据库）。
    
   基于多JVM，服务拆分了 并且数据库分库分表了。
   
解决分布式事务问题的方案有很多，使用消息实现只是其中的一种


#### 3.2.3、原理
> Half(Prepare) Message

指的是暂不能投递的消息，发送方已经将消息成功发送到了 MQ 服务端，但是服务端未收到生产者对该消息的二次
确认，此时该消息被标记成“暂不能投递”状态，处于该种状态下的消息即半消息

> Message Status Check

由于网络闪断、生产者应用重启等原因，导致某条事务消息的二次确认丢失，MQ 服务端通过扫描发现某条消息长
期处于“半消息”时，需要主动向消息生产者询问该消息的最终状态（Commit 或是 Rollback），该过程即消息回
查

#### 3.3.4、执行流程
* 1. 发送方向 MQ 服务端发送消息。

* 2. MQ Server 将消息持久化成功之后，向发送方 ACK 确认消息已经发送成功，此时消息为半消息。

* 3. 发送方开始执行本地事务逻辑。

* 4. 发送方根据本地事务执行结果向 MQ Server 提交二次确认（Commit 或是 Rollback），MQ Server 收到
Commit 状态则将半消息标记为可投递，订阅方最终将收到该消息；MQ Server 收到 Rollback 状态则删除半
消息，订阅方将不会接受该消息。

* 5. 在断网或者是应用重启的特殊情况下，上述步骤4提交的二次确认最终未到达 MQ Server，经过固定时间后
MQ Server 将对该消息发起消息回查。

* 6. 发送方收到消息回查后，需要检查对应消息的本地事务执行的最终结果。

* 7. 发送方根据检查得到的本地事务的最终状态再次提交二次确认，MQ Server 仍按照步骤4对半消息进行操作

### 4、consumer详解

#### 4.1、push和pull模式

在RocketMQ中，消费者有两种模式，一种是push模式，另一种是pull模式。

push模式：客户端与服务端建立连接后，当服务端有消息时，将消息推送到客户端。

pull模式：客户端不断的轮询请求服务端，来获取新的消息。

但在具体实现时，Push和Pull模式都是采用消费端主动拉取的方式，即consumer轮询从broker拉取消息

区别：

* Push方式里，consumer把轮询过程封装了，并注册MessageListener监听器，取到消息后，唤醒MessageListener的consumeMessage()来消费，对用户而言，感觉消息是被推送过来的。

* Pull方式里，取消息的过程需要用户自己写，首先通过打算消费的Topic拿到MessageQueue的集合，遍历MessageQueue集合，然后针对每个MessageQueue批量取消息，一次取完后，记录该队列下一次要取的开始offset，直到取完了，再换另一个MessageQueue。

疑问：既然是采用pull方式实现，RocketMQ如何保证消息的实时性呢？

#### 4.1.1、长轮询

RocketMQ中采用了长轮询的方式实现，什么是长轮询呢？

长轮询即是在请求的过程中，若是服务器端数据并没有更新，那么则将这个连接挂起，直到服务器推送新的数据，再返回，然后进入循环周期。

客户端像传统轮询一样从服务端请求数据，服务端会阻塞请求不会立刻返回，直到有数据或超时才返回给客户端，然后关闭连接，客户端处理完响应信息后再向服务器发送新的请求

#### 4.2、消息模式
DefaultMQPushConsumer实现了自动保存offset值以及实现多个consumer的负载均衡。
```
//设置组名
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("HAOKE_IM");
```

通过groupName将多个consumer组合在一起，那么就会存在一个问题，消息发送到这个组后，消息怎么分配呢？

这个时候，就需要指定消息模式，分别有集群和广播模式。

集群模式

   同一个 ConsumerGroup(GroupName相同) 里的每 个 Consumer 只消费所订阅消息的一部分内容， 同一个 ConsumerGroup 里所有的 Consumer消费的内容合起来才是所订阅 Topic 内容的整体， 从而达到负载均衡的目的 。

广播模式

   同一个 ConsumerGroup里的每个 Consumer都 能消费到所订阅 Topic 的全部消息，也就是一个消息会被多次分发，被多个 Consumer消费
   
```
// 集群模式
consumer.setMessageModel(MessageModel.CLUSTERING);
// 广播模式
consumer.setMessageModel(MessageModel.BROADCASTING);
```

#### 4.3、重复消息的解决方案

造成消息重复的根本原因是：网络不可达。只要通过网络交换数据，就无法避免这个问题。所以解决这个问题的办法就是绕过这个问题。那么问题就变成了：如果消费端收到两条一样的消息，应该怎样处理？
    
   1. 消费端处理消息的业务逻辑保持幂等性
    
   2. 保证每条消息都有唯一编号且保证消息处理成功与去重表的日志同时出现

第1条很好理解，只要保持幂等性，不管来多少条重复消息，最后处理的结果都一样。

第2条原理就是利用一张日志表来记录已经处理成功的消息的ID，如果新到的消息ID已经在日志表中，那么就不再处理这条消息。

第1条解决方案，很明显应该在消费端实现，不属于消息系统要实现的功能。

第2条可以消息系统实现，也可以业务端实现。正常情况下出现重复消息的概率其实很小，如果由消息系统来实现的话，肯定会对消息系统的吞吐量和高可用有影响，所以最好还是由业务端自己处理消息重复的问题，这也是RocketMQ不解决消息重复的问题的原因。

**RocketMQ不保证消息不重复，如果你的业务需要保证严格的不重复消息，需要你自己在业务端去重。**

### 5、RocketMQ存储

RocketMQ中的消息数据存储，采用了零拷贝技术（使用 mmap + write 方式），文件系统采用 Linux Ext4 文件系统进行存储。

#### 5.1、消息数据的存储
在RocketMQ中，消息数据是保存在磁盘文件中，为了保证写入的性能，RocketMQ尽可能保证顺序写入，顺序写入的效率比随机写入的效率高很多。

RocketMQ消息的存储是由ConsumeQueue和CommitLog配合完成的，CommitLog是真正存储数据的文件，

ConsumeQueue是索引文件，存储数据指向到物理文件的配置。

* 消息主体以及元数据都存储在`CommitLog`当中

* Consume Queue相当于kafka中的partition，是一个逻辑队列，存储了这个Queue在`CommitLog`中的起始

offset，log大小和`MessageTag`的hashCode。

* 每次读取消息队列先读取consumerQueue,然后再通过consumerQueue去commitLog中拿到消息主体。


#### 5.2、同步刷盘与异步刷盘

RocketMQ 为了提高性能，会尽可能地保证 磁盘的顺序写。消息在通过 Producer 写入 RocketMQ 的时候，有两种写磁盘方式，分别是`同步刷盘`与`异步刷盘`。

同步刷盘

   在返回写成功状态时，消息已经被写入磁盘 。
    
   具体流程是：消息写入内存的 PAGECACHE 后，立刻通知刷盘线程刷盘，然后等待刷盘完成，刷盘线程
    
   执行完成后唤醒等待的线程，返回消息写成功的状态 。
   
异步刷盘

   在返回写成功状态时，消息可能只是被写入了内存的 PAGECACHE，写操作的返回快，吞吐量大
    
   当内存里的消息量积累到一定程度时，统一触发写磁盘动作，快速写入。
    
   broker配置文件中指定刷盘方式
    
       `flushDiskType=ASYNC_FLUSH` -- 异步
        
       `flushDiskType=SYNC_FLUSH` -- 同步
