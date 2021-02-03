package com.lzr.service;

import com.lzr.Entity.EachRequest;
import com.lzr.Entity.Success;
import com.lzr.Expction.FailExp;
import com.lzr.Expction.NoGoodExp;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SecKillService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     *单机无需使用锁，因为redis是单线程的，不存在线程安全问题
     * @param userid 下单用户id
     * @param goodid 秒杀商品id
     */
    public Object CheckInventory(Long userid,Long goodid) throws NoGoodExp, FailExp {
        String Inv=redisTemplate.opsForValue().get("good");
        if (Integer.parseInt(Inv)<=0)//无库存直接返回0优化速度
            return null;
        String Bought=redisTemplate.opsForValue().get((userid).toString());//可能返回nil
        if(Bought==null)
            Bought="0";
        else
            return null;//已下单直接返回优化速度
            if(Bought.equals("0"))
        {
            redisTemplate.opsForValue().increment("good",-1);
            redisTemplate.convertAndSend("success",new Success(userid,1));//进入队列
            rabbitTemplate.convertAndSend("work",new EachRequest(userid,goodid));//向队列发送下单请求
        }
        else {
            throw new NoGoodExp("重复下单");
        }
        return null;//TODO
    }

    /**
     * 基于redis的乐观锁实现，速度稍慢
     * @param userid
     * @param goodid
     * @return
     * @throws NoGoodExp
     * @throws FailExp
     */
    public Object CheckInventory1(Long userid,Long goodid) throws NoGoodExp, FailExp {
        String Bought=redisTemplate.opsForValue().get((userid).toString());//可能返回nil
        if(Bought==null)
            Bought="0";
        else
            return null;//已下单直接返回优化速度
        redisTemplate.watch("good");//乐观锁减库存防止数据错误
        String Inv=redisTemplate.opsForValue().get("good");
        if (Integer.parseInt(Inv)<=0)//无库存直接返回0优化速度
            return null;

        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.multi();
        if(Bought.equals("0"))
        {

            redisTemplate.opsForValue().increment("good",-1);
             List list = redisTemplate.exec();

             if (list.size()!=0){//事务执行完毕且无错误
            rabbitTemplate.convertAndSend("work",new EachRequest(userid,goodid));//向队列发送下单请求
             }
            else {
              throw new FailExp("秒杀失败");
              }
        }
        else {

            List list1=redisTemplate.exec();
            throw new NoGoodExp("秒杀已结束");
        }
        return null;//TODO
    }
}
