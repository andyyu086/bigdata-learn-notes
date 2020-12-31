1. 下载安装：
MAC环境：
https://www.cnblogs.com/nickchen121/p/11145123.html
Linux环境：
https://www.jianshu.com/p/276d59cbc529


2. 创建数据库：
> mysql> create database tb_base;
Query OK, 1 row affected (0.00 sec)

mysql> use tb_base;

3. 创建表和插入数据
CREATE TABLE tb_user (
id int(32) PRIMARY KEY AUTO_INCREMENT,
username varchar(32),
address varchar(256)
);
INSERT INTO tb_user VALUES ("1","xiaohan","beijinghaidian");
INSERT INTO tb_user VALUES ("2","xiaoshi","beijingchangping");
INSERT INTO tb_user VALUES ("3","xiaochen","beijingshunyi");
INSERT INTO tb_user VALUES ("4","xiaobai",NULL);

INSERT INTO tb_user VALUES ("5","xiaowang",NULL);
INSERT INTO tb_user VALUES ("6","xiaoliu",NULL);
INSERT INTO tb_user VALUES ("7","xiaoqian",NULL);

UPDATE tb_user SET username="xiaohan",address="beijinghaidian" WHERE id = 1;

4. Spring Boot和mybatis的配合使用
https://www.jianshu.com/p/541874714907

5. 合计统计的操作实例：
```sql
mysql> select * from tb_user;
+----+----------+------------------+
| id | username | address          |
+----+----------+------------------+
|  1 | xiaohan  | beijinghaidian   |
|  2 | xiaoshi  | beijingchangping |
|  3 | xiaochen | beijingshunyi    |
+----+----------+------------------+
3 rows in set (0.01 sec)

mysql> select coalesce(username,'Total'),count(*) from tb_user group by username with rollup;
+----------------------------+----------+
| coalesce(username,'Total') | count(*) |
+----------------------------+----------+
| xiaochen                   |        1 |
| xiaohan                    |        1 |
| xiaoshi                    |        1 |
| Total                      |        3 |
+----------------------------+----------+
4 rows in set (0.00 sec)
```

6. SQL编写注意事项：
- 关于 NULL 的条件比较运算是比较特殊的。你**不能**使用 = NULL 或 != NULL 在列中查找NULL值；
MySQL中处理 NULL使用IS NULL和IS NOT NULL运算符。

- SQL LIKE子句中使用百分号%字符来表示任意字符，类似于UNIX或正则表达式中的星号*。
如果没有使用百分号 %, LIKE 子句与等号 = 的效果是一样的。

7. 事务：
事务是必须满足4个条件（ACID）：
原子性（Atomicity，或称不可分割性）、一致性（Consistency）、隔离性（Isolation，又称独立性）、持久性（Durability）。
原子性：一个事务（transaction）中的所有操作，要么全部完成，要么全部不完成，不会结束在中间某个环节。事务在执行过程中发生错误，会被回滚（Rollback）到事务开始前的状态，就像这个事务从来没有执行过一样。
一致性：在事务开始之前和事务结束以后，数据库的完整性没有被破坏。这表示写入的资料必须完全符合所有的预设规则，这包含资料的精确度、串联性以及后续数据库可以自发性地完成预定的工作。
隔离性：数据库允许多个并发事务同时对其数据进行读写和修改的能力，隔离性可以防止多个事务并发执行时由于交叉执行而导致数据的不一致。事务隔离分为不同级别，包括读未提交（Read uncommitted）、读提交（read committed）、可重复读（repeatable read）和串行化（Serializable）。
持久性：事务处理结束后，对数据的修改就是永久的，即便系统故障也不会丢失。
- 举例如下：
```sql
mysql> begin;
Query OK, 0 rows affected (0.00 sec)

mysql> INSERT INTO tb_user VALUES ("5","xiaowang",NULL);
Query OK, 1 row affected (0.00 sec)

mysql> select * from tb_user;
+----+----------+------------------+
| id | username | address          |
+----+----------+------------------+
|  1 | xiaohan  | beijinghaidian   |
|  2 | xiaoshi  | beijingchangping |
|  3 | xiaochen | beijingshunyi    |
|  4 | xiaobai  | NULL             |
|  5 | xiaowang | NULL             |
+----+----------+------------------+
5 rows in set (0.00 sec)

mysql> INSERT INTO tb_user VALUES ("6","xiaoliu",NULL);
Query OK, 1 row affected (0.00 sec)

mysql> commit;
Query OK, 0 rows affected (0.00 sec)

mysql> select * from tb_user;
+----+----------+------------------+
| id | username | address          |
+----+----------+------------------+
|  1 | xiaohan  | beijinghaidian   |
|  2 | xiaoshi  | beijingchangping |
|  3 | xiaochen | beijingshunyi    |
|  4 | xiaobai  | NULL             |
|  5 | xiaowang | NULL             |
|  6 | xiaoliu  | NULL             |
+----+----------+------------------+
6 rows in set (0.00 sec)

mysql> begin;
Query OK, 0 rows affected (0.00 sec)

mysql> INSERT INTO tb_user VALUES ("7","xiaoqian",NULL);
Query OK, 1 row affected (0.00 sec)

mysql> select * from tb_user;
+----+----------+------------------+
| id | username | address          |
+----+----------+------------------+
|  1 | xiaohan  | beijinghaidian   |
|  2 | xiaoshi  | beijingchangping |
|  3 | xiaochen | beijingshunyi    |
|  4 | xiaobai  | NULL             |
|  5 | xiaowang | NULL             |
|  6 | xiaoliu  | NULL             |
|  7 | xiaoqian | NULL             |
+----+----------+------------------+
7 rows in set (0.00 sec)

mysql> rollback;
Query OK, 0 rows affected (0.00 sec)

mysql> select * from tb_user;
+----+----------+------------------+
| id | username | address          |
+----+----------+------------------+
|  1 | xiaohan  | beijinghaidian   |
|  2 | xiaoshi  | beijingchangping |
|  3 | xiaochen | beijingshunyi    |
|  4 | xiaobai  | NULL             |
|  5 | xiaowang | NULL             |
|  6 | xiaoliu  | NULL             |
+----+----------+------------------+
6 rows in set (0.00 sec)
```

8. 索引
- 索引分单列索引和组合索引。
单列索引，即一个索引只包含单个列，一个表可以有多个单列索引，但这不是组合索引。
组合索引，即一个索引包含多个列。
创建索引时，要确保该索引是应用在SQL查询语句的条件(一般作为 WHERE 子句的条件)。
虽然索引大大提高了查询速度，同时却会降低更新表的速度。因为更新表时，还要保存索引文件。
-操作举例：
> mysql> create index name_index on tb_user(username);
Query OK, 0 rows affected (0.04 sec)
Records: 0  Duplicates: 0  Warnings: 0

mysql> show index from tb_user;

9. 注入
- 防止SQL注入，要注意要点：
1.对用户的输入进行校验，可以通过正则表达式，或限制长度；对单引号和 双"-"进行转换等。
2.永远不要使用动态拼装sql，可以使用参数化的sql或者直接使用存储过程进行数据查询存取。
3.永远不要使用管理员权限的数据库连接，为每个应用使用单独的权限有限的数据库连接。
4.不要把机密信息直接存放，加密或者hash掉密码和敏感的信息。

10. SQL函数参考
[参考](https://www.runoob.com/mysql/mysql-functions.html)

