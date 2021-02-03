package com.lzr.dao;

import org.apache.ibatis.annotations.*;

@Mapper
public interface StockDao {
    @Update(value = "update good set shengyu=shengyu-1 where id=#{goodid};")
    public void ReduceStock(Long goodid);
    @Update(value = "update good set shengyu=200 where id=#{goodid};")
    public void Reload(Long goodid);
    @Insert(value = "insert into consumerorder values(#{userid},#{goodid});")
    public void Insert(@Param("userid") Long userid, @Param("goodid") Long goodid);
    @Select(value = "select shengyu from good where id=1")
    public int find();
}
