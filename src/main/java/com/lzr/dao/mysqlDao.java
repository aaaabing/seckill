package com.lzr.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface mysqlDao {
    @Select(value ="select shengyu from good where id=1")
    public int select();
    @Update(value = "update good set shengyu=shengyu-1 where shengyu>=0;")
    public void update();
}
