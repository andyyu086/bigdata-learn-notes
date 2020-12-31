## Mybatis Plus使用记录

### 1. 简述
- 拓展mybatis的使用，类似结合JPA的用法，继承使用编写完成的多个查询或更新方法；
- 基本用法和mybatis类似，
在Application类加入MapperScan进行mapper的扫描 
-> Controller使用autowired的service，直接使用继承的查询方法 
-> service层直接继承ServiceImpl，其中引入Mapper这一DAO层 
-> DAO可以直接继承BaseMapper方法 
-> 调取具体model或者entity层的数据库表和字段定义。

- 此外，兼容mybatis的原生XML用法，对于一些复杂的查询语句可以直接编写语句。

### 2. 实现步骤
#### 2.1 Controller的具体实现代码
```java
package com.neo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neo.model.User;
import com.neo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserInfoController {
    @Autowired
    private UserService userService;

    /**
     * 传入id，进行查询
     * @param id
     * @return
     */
    @RequestMapping(value = "/get/{id}",method = RequestMethod.GET)
    public User getId(@PathVariable String id){
        User byId = userService.getById(id);
        return byId;
    }

    @RequestMapping(value = "/getall",method = RequestMethod.GET)
    public List<User> getAll(){
        List<User> list = userService.list();
        return list;
    }

    /**
     * 分页查询
     * @return
     */
    @RequestMapping(value = "/getpage",method = RequestMethod.GET)
    public IPage<User> getPage(){
        Page<User> objectPage = new Page<User>();
        objectPage.setCurrent(2);
        objectPage.setSize(2);
        IPage<User> page = userService.page(objectPage);
        return page;
    }

    /**
     * 使用QueryWrapper lambda功能进行WHERE等的操作
     * @return
     */
    @RequestMapping(value = "/querymap",method = RequestMethod.GET)
    public List<User> queryMapper(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Map<String,Object> resultMap = new HashMap<>();
        queryWrapper.lambda().eq(User::getAge, 18);
        List<User> list = userService.list(queryWrapper);
        return list;
    }

    /**
     * 支持使用mybatis原生定义的XML的查询
     * @param faction
     * @return
     */
    @RequestMapping(value = "/getfraction/{fact}",method = RequestMethod.GET)
    public IPage<User> getUserByFraction(@PathVariable("fact") long faction){
        Page<User> objectPage = new Page<User>();
        objectPage.setCurrent(1);
        objectPage.setSize(5);
        IPage<User> page = userService.userByFraction(objectPage,faction);
        return page;
    }
}
```
- 注意QueryWrapper和getUserByFraction编写的代码方法，其他都是可以直接调取已经实现的方法的。

#### 2.2 Service 层的具体实现例子
```java
package com.neo.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neo.mapper.UserMapper;
import com.neo.model.User;
import com.neo.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {
    @Override
    public IPage<User> userByFraction(IPage<User> page, Long fraction) {
        return this.baseMapper.userByFraction(page,fraction);
    }
}
```
- 除了实现mybatis原生XML的定义方法userByFraction，其他的方法都直接继承ServiceImpl，什么也没有实现具体的代码，很简单。

#### 2.3 DAO 层 Mapper的具体例子
```java
package com.neo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neo.model.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper extends BaseMapper<User> {
    IPage<User> userByFraction(IPage<User> page, @Param("fraction") Long fraction);
}
```
- 也是很简单，除了userByFraction，就是继承BaseMapper即可。
- 注意：Mapper传参大于1个时，需要加@Param主键进行定义。

#### 2.4 Entity 层 表schema的定义层
```java
package com.neo.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("user_info")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer age;
    private String skill;
    private String evaluate;
    private Long fraction;
}
```
- 其中的getter和setter方法省略了。

#### 2.5 原生mybatis XML配置文件的定义
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.neo.mapper.UserMapper">
    <select id="userByFraction" resultType="com.neo.model.User" parameterType="long">
        SELECT * FROM user_info WHERE fraction > #{fraction}
    </select>
</mapper>
```
- 和原生的定义一样

### YML文件的配置内容
> 
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.neo.model