package com.lzr.Entity;

import java.io.Serializable;

/**
 * 一次秒杀请求
 */
public class EachRequest implements Serializable {  //序列化后才可被MQ发送
    private static final long serialVersionUID = 1L;
    private long userid;
    private long goodid;

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public long getGoodid() {
        return goodid;
    }

    public void setGoodid(long goodid) {
        this.goodid = goodid;
    }

    public EachRequest(long userid, long goodid) {
        this.userid = userid;
        this.goodid = goodid;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
