## Docker的使用记录

### 1. Spring Boot Web服务打包样例
1. 使用idea里面的mvn install建项目打包成target里面的jar，可以先使用java命令运行测试，spring启动没有问题：
> java -jar spring-boot-docker-1.0.jar

2. 确认docker已经安装完毕，Mac下安装Desktop，直接点击启动即可，Linux下使用命令启动：
> service docker start

3. docker镜像build，可以使用idea的docker插件进行构建，或者使用docker命令行：
- 新建一个docker目录，将DockerFile和jar都复制到这个新目录，
- DockerFile样例：
> FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD spring-boot-docker-1.0.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

- build镜像操作：
> docker build -t springboot/spring-boot-docker:bld .
- 查看镜像清单：
> docker images
- 启动运行：
> docker run -td springboot/spring-boot-docker:bld
- 进入docker容器内部：
> docker exec -it c92d3fb7aaeb /bin/sh
- 外部查看执行日志
> docker logs -f c92d3fb7aaeb

4. 查看本机已有的所有镜像清单：
> docker images
> docker pull docker_name

5. 启动构建完成的docker镜像：(使用-p指定宿主机和docker的对应tcp端口绑定，-t启动虚拟的终端)
> docker run -p 8080:8080 -t springboot/spring-boot-docker

6. 查看所有docker清单
> docker ps -a

7. 查看最近创建过的docker
> docker ps -l

8. 使用-d后台启动docker
> docker run -p 8080:8080 -t -d springboot/spring-boot-docker

9. 使用logs查看docker内程序的标准输出
> docker logs -f 7e20ea4cc87d

10. 使用-it以交互的模式登陆进入docker，后面跟sh
> sudo docker exec -it 7e20ea4cc87d /bin/sh
- 进入docker，可以进行一些修正后，commit重新打出一个镜像：
> docker commit -m="has update" e218edb10161 docker_name:v2

11. 停止对应id的docker，删除docker操作，删除docker进行操作：
> docker stop 7e20ea4cc87d
> docker rm -f f6a5e35b0b1e
> docker rmi 4f7e25ea2f07

12. 镜像本地导入和导出服务
> cat xx.tar | docker import - docker_name
> docker export docker_id >./xx.tar

13. 小技巧：
- 删除所有stop的容器：
> docker container prune








