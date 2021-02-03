package com.lzr.Handler;

import com.lzr.Entity.Success;
import com.lzr.dao.StockDao;
import com.lzr.dao.mysqlDao;
import com.lzr.service.SecKillService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.rabbitmq.client.Channel;

import java.util.HashMap;
import java.util.Set;

@Controller
public class adminHandler {
    HashMap<Long, Integer> hashMap=new HashMap<Long, Integer>();
    private   Long t=1L;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    SecKillService secKillService;
    @Autowired
    StockDao stockDao;
    @Autowired
    mysqlDao mysqldao;
    @ResponseBody
    @GetMapping("/kill")
    public void miaosha(){
        t=t+1L;
        secKillService.CheckInventory1(t,1L);
    }
    @ResponseBody
    @GetMapping("/load")
    public void load(){
        Set<String> keys = redisTemplate.keys("*");
        redisTemplate.delete(keys);
        redisTemplate.opsForValue().set("good","200");
        stockDao.Reload(1L);
    }
    @ResponseBody
    @GetMapping("/view")
    public void view() {
        int t = stockDao.find();
        System.out.println(redisTemplate.opsForValue().get("good") + " " + t);
    }
    @RabbitListener(queuesToDeclare =@Queue (value = "success"))
    public void success(Success success, Channel channel, Message message){
        hashMap.put(success.getUserid(),success.getCond());
    }

    /**
     *前端轮询该接口，获得是否秒杀成功的消息
     * @param userid
     * @return -1 秒杀失败
     *          0 秒杀成功
     *          1 队列中
     */
    @ResponseBody
    @GetMapping("/getSuc")
    public int getsuc(Long userid){
        return hashMap.get(userid)!=null?hashMap.get(userid):-1;
    }
}
