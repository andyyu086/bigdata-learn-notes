
### 安装
- Linux Redhat CentOS 7下步骤:

sudo yum install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm
sudo yum install -y postgresql10-server
postgresql-10-setup initdb

systemctl restart postgresql-10.service

- 修改密码:
[root@datateam-db ~]# su - postgres
Last login: Wed Feb  7 10:43:10 CST 2018 on pts/4
-bash-4.2$ psql
psql (9.2.15, server 10.17)
WARNING: psql version 9.2, server version 10.0.
         Some psql features might not work.
Type "help" for help.

postgres=# ALTER USER postgres WITH PASSWORD 'patsnapDATA';

- 外部访问，修改配置文件:
postgresql.conf和pg_hba.conf

- 完整操作步骤请参考:
https://blog.csdn.net/qq_32448349/article/details/84336848



