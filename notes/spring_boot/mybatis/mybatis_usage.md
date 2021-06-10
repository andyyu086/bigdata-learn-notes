## Mybatis相关使用

### 1. Mybatis XML编写用法步骤
- 配置文件定义：config xml文件可以写一些类型别名；mapper xml文件写SQL语句实现；
- 编写Mapper接口，定义方法即可，具体方法名和mapper xml中定义的id一致；
- 具体使用可以使用autowired获取mapper之后再service层或者controller层调用具体方法即可。

具体代码sample举例：

- controller层代码如下：
```java
package com.patsnap.spt.smt.contorller;

import com.patsnap.spt.smt.po.User;
import com.patsnap.spt.smt.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "用户模块")
@RequestMapping("/user")
public class DBUserController {
    @Autowired
    private UserService userService;

    @ApiOperation(value = "获取所有用户", notes = "查找所有")
    @GetMapping("/findAll")
    public List<User> findAll(){
        List<User> allUser = userService.getAllUser();
        return allUser;
    }

    @ApiOperation(value = "删除特定用户", notes = "用id删除特定用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户的唯一标识", required = true, dataType = "int", paramType = "path")
    })
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id){
        this.userService.deleteUser(id);
    }
}
```
- service层具体的实现：(主要进行mapper代码的调取)
```java
package com.patsnap.spt.smt.service.impl;

import com.patsnap.spt.smt.mapper.UserMapper;
import com.patsnap.spt.smt.po.User;
import com.patsnap.spt.smt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> getAllUser() {
        return this.userMapper.findAll();
    }

    @Override
    public void deleteUser(Integer id) {
        this.userMapper.delete(id);
    }
}
```
- mapper的接口和XML定义
```java
package com.patsnap.spt.smt.mapper;

import com.patsnap.spt.smt.po.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserMapper {
    List<User> findAll();

    void delete(Integer id);
}
```
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.patsnap.spt.smt.mapper.UserMapper">
    <select id="findAll" resultType="User">
        SELECT * FROM tb_user
    </select>

    <select id="delete" parameterType="int">
        delete from tb_user where id = #{id}
    </select>
</mapper>
```
- Entity层的定义：
```java
package com.patsnap.spt.smt.po;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 8655851615465363473L;

    private Integer id;
    private String username;
    private String address;
}
```

### 2. Mybatis 注解编写用法步骤
- 无需xml的SQL定义，直接在Mapper接口使用Select，Results，Insert，Update，Delete注解将SQL编写传入即可；
- 具体mapper调用和XML的用法一样的。
- 具体Mapper实现注解的代码举例如下：
```java
package com.patsnap.spt.smt.mapper;

import com.patsnap.spt.smt.po.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserAnnotationMapper {
    @Select("SELECT * FROM tb_user")
    List<User> findAll();

    @Delete("DELETE FROM tb_user WHERE id = #{id}")
    void delete(Integer id);
}
```

### 3. mapper-spring-boot-starter的用法
- mybatis的一个通用mapper的插件，继承BaseMapper类既可以使用CRUD相关的方法；
- 此外Enitity类需要加入Table Id等注解
- 其他方面，比如：mapper xml定义等都是一样的。
- 具体的集成Mapper实现代码举例如下：

```java
package com.patsnap.spt.smt.tmapper;

import com.patsnap.spt.smt.po.MUser;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

@Mapper
public interface MUserMapper extends BaseMapper<MUser> {
    /**
     * 计算特定人名的个数
     * @param username
     * @return
     */
    int countByUserName(String username);
}
```

### 4. 多个数据源的使用
- 对于多个库的使用需要DataSourceConfig类配置对应数据的url信息，此外需要使用Primary注解指定主库；
- 此外不同库的Mapper定义在不同的basePackages内，这样可以很好的进去配置区分。
