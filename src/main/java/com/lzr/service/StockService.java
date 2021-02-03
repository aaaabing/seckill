package com.lzr.service;

import com.lzr.Entity.EachRequest;
import com.lzr.Entity.Success;
import com.lzr.dao.StockDao;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;

@Service
public class StockService {
    @Autowired
    StockDao stockDao;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     * rabbitmq的消息默认配置是自动确认
     * 在方法内部如果抛出异常的话
     * 未确认消息会回到队列
     * 发送给其他消费者
     * 会导致死循环的发生
     * @Tranesactional注解保证了原子性，不会出现超卖现象
     */
    @Transactional
    @RabbitListener(queuesToDeclare = @Queue(value = "work"))
    public Object RedisStockRedece(EachRequest eachRequest,Channel channel,Message message) throws IOException {
        try {
            if(stockDao.find()>0) {
                stockDao.Insert(eachRequest.getGoodid(), eachRequest.getUserid());//生成订单
                stockDao.ReduceStock(eachRequest.getGoodid());//减库存
                stringRedisTemplate.opsForValue().set(String.valueOf(eachRequest.getUserid()), "1");//避免重复下单
            }else
                return null;
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {//扣减库存成功后返回下单成功消息,前端轮询接口
                        @Override
                        public void afterCommit() {
                            rabbitTemplate.convertAndSend("success",new Success(eachRequest.getUserid(),0));
                        }
                    }

            );
        }
        catch (BadSqlGrammarException e) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
            e.printStackTrace();
        }
        return null;
    }

    @Transactional(rollbackFor = {RuntimeException.class})//开启事务保持原子性
    @RabbitListener(queuesToDeclare = @Queue(value = "work"))
    public Object RedisStockRedece1(EachRequest eachRequest,Channel channel,Message message) throws IOException {
        try {
            if(stockDao.find()>0) {
                stockDao.Insert(eachRequest.getGoodid(), eachRequest.getUserid());//生成订单
                stockDao.ReduceStock(eachRequest.getGoodid());//减库存
                stringRedisTemplate.opsForValue().set(String.valueOf(eachRequest.getUserid()), "1");//避免重复下单
            }else
                return null;
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);//手动确认消息
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        //提交成功后传递下单成功的信息给接口使前端轮询
                        @Override
                        public void afterCommit() {
                            rabbitTemplate.convertAndSend("success",new Success(eachRequest.getUserid(),0));
                        }
                    }
            );
        }
        catch (BadSqlGrammarException e) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
            e.printStackTrace();
        }
        return null;
    }
}