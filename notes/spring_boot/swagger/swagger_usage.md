## Swagger2使用示例记录

### 1. 在config表内加入具体的文档描述等配置：
```java
package com.patsnap.spt.smt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket createApi(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.patsnap.spt.smt.contorller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Demo项目文档")
                .description("用于controller api文档生成和html页面api测试")
                .contact(new Contact("Spring_learn","xxx.com","xxx@qq.com"))
                .version("1.0")
                .build();
    }
}
```
简单解释几点：
- 加入Configuration和EnableSwagger2注解
- 实现一个返回Docket的Bean注解，重点配置文档描述和Api文档扫描包路径

### 2. 实现Contorller里面实现的Api的描述定义
```java
package com.patsnap.spt.smt.contorller;

import com.patsnap.spt.smt.exception.CustomException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/exp")
@Api(tags = "异常判断日志模块")
public class ExceptionController {
    @ApiOperation(value = "除法null异常", notes = "除法为null下定义异常")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "num",value = "传入的除数",dataType = "Integer",required = true,paramType = "path")
    })
    @RequestMapping(value = "/{num}",method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code=400,message = "传参为空或0")
    })
    public String dvideByTen(@PathVariable Integer num) {
        if (num == null || num == 0) {
            throw new CustomException(400, "num不能为空或0");
        }
        int i = 10 / num;
        return "result:" + i;
    }
}
```
以上这个接口的实现，解释几点：
- 用Api注解定义改controller的名称
- 使用ApiOperation定义Api每个操作方法的名称和描述
- 使用ApiImplicitParams进行方法传参的名称，描述，类型等
- 使用ApiResponses实现Api返回值的code和具体含义描述

### 3. 文档查看和调试
1. 完成以上代码后，启动Spring Boot项目；
2. 在http://localhost:8081/swagger-ui.html 参看文档，以及传入参数进行api调试验证。

