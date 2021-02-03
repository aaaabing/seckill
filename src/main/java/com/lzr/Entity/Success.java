package com.lzr.Entity;

import java.io.Serializable;

public class Success  implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long userid;
    private int cond;

    public Success(Long userid, int cond) {
        this.userid = userid;
        this.cond = cond;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }
}
