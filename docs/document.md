## 系统环境

我们将我们的应用容器化后部署在四个节点组成的kubernetes集群上。我们的四个节点的系统参数化如下表：

| 节点          | vCPU | Memory | STORAGE |
| ------------- | ---- | ------ | ------- |
| km1（master） | 4    | 8GB    | 80GB    |
| kn1           | 4    | 8GB    | 80GB    |
| kn2           | 4    | 8GB    | 80GB    |
| kn3           | 4    | 8GB    | 80GB    |

我们在集群上部署了我们的应用与系统，详细细节如下：

- kafka ：部署在kubernetes上，共3个节点。
- zookeeper: 部署在kubernetes上，共3个节点。
- MySQL: 直接部署在集群裸机上，共1个节点。
- spark： 使用spark-kubernetes-operator，在提交spark任务时会启动driver节点通过kubernetes API自动创建可执行spark任务的executor节点。
- NFS： 部署在裸机上，共4个节点，向kubernetes提供持久化储存。

## 系统部署

### Kubernetes集群部署

我们的应用部署在kubernetes集群上，我们部署了一个4节点的单master kubernetes集群。kubernetes的部署我们使用了KubeSpray。

KubeSpray是基于Ansible的kubernetes的自动化部署工具，它提供了一套完整的kubernetes部署的Ansible脚本，通过修改inventory来自定义自己的集群节点，即可自动化部署kubernetes。

需要注意的是，由于kubernetes大部分依赖托管在Google上，因此需要先将依赖镜像以及二进制文件离线缓存，分别加载到docker 缓存与本地环境变量中。

当一切就绪：`ansible-playbook -i inventory/mycluster/hosts.yml --become --become-user=root cluster.yml` ，即可开始部署。

### Kubernetes上应用部署

采用Helm进行kubernetes上的应用部署，Helm是kubernetes的应用管理工具，它提供了一系列流行应用的安装包，使得用户能够通过简单的命令`helm install <appname>`快速地在kubernetes上部署应用。同时只要编写描述文件Chart.yaml指定自己的镜像，也可以方便地部署自己的应用。

Helm官方的镜像都托管在Google container repository上，由于众所周知的原因，国内无法获取到相应镜像，我们使用了Aliyun container repository 来作为替代管理我们的容器镜像。

#### NFS Provision

Kubernetes自身没有提供持久化储存，而是依赖于外部分布式储存来进行持久化，因此在部署stateful应用时用户需要为kubernetes集群配置分布式储存提供者。

在我们的系统中，我们采用了部署较为简单的NFS来提供分布式储存。部署好之后需要有相应的服务来将NFS提供给集群里的应用。也即NFS Provisioner,这是kubernetes官方提供的应用，他会动态的管理PVC(persistence volume )和PV(persistence volume)来为应用提供持久化服务。

`helm install <repo/nfs-provision>`

部署好之后，NFS Provision 会定义一个Storage Class,之后的应用只要指定这个Storage Class即可使用NFS 储存。

![1562855439280](./pic\1562855439280.png)

#### Kafka 及 Zookeeper

由于Kafka依赖于Zookeeper，部署时我们将这两个集群同时部署，在kafa的Chart中加入依赖：

```yaml
dependencies:
- name: zookeeper
  version: 2.x.x
  repository: https://apphub.aliyuncs.com
  condition: zookeeper.enabled

```

Helm支持使用自定义的配置文件来统一管理应用部署变量，针对我们的需求我们对默认配置进行了以下修改：

```yaml
#mykafkavalue.yaml
...
replicaCount: 3
#指定持久化储存
persistence:
  enabled: true
  storageClass: nfs-client
  accessModes:
    - ReadWriteOnce
  size: 8Gi
  annotations: {}

zookeeper:
  enabled: true
  persistence:
    enabled: true
    storageClass: nfs-client
    accessModes:
      - ReadWriteOnce
    size: 8Gi
    annotations: {}
  replicaCount: 3

```

`helm install kafka -f mykafkavalue.yaml` 后部署截图：

![1562854507253](./pic\1562854507253.png)

#### Spark-operator

`helm install <repo/spark-operator>`

Spark-operator 是Google为spark提供的kubernetes原生支持，使得spark容器可以直接运行在kubernetes上，不需要再依赖Hadoop。

Spark-operator 提供了一系列用于spark的CRD（自定义资源定义），用户可以使用spark-submit向kubernetes集群直接提交spark任务，Spark-operator会创建driver来进行spark容器调度，并通过kubernetes API创建executor进行计算。

![image.png](http://ata2-img.cn-hangzhou.img-pub.aliyun-inc.com/48624573464b63b542932308062a3973.png)

任务结束后executor会被清理，日志与Spark-UI都会在driver中保留。

![1562856716230](./pic\1562856716230.png)

## 程序设计

整个系统被分为三个部分：

- Spark Streaming Processing：处理订单数据
- HTTP Server
  - 处理Sender的新订单请求，并将其存入Kafka
  - 处理用户的查询请求
- Order Generator & Sender：生成和发送新的订单数据

### Spark Streaming Processing

这一部分从Kafka读取数据，经过Spark Streaming处理后，将结果保存在MySQL和Zookeeper中，是整个程序的核心。

#### MySQL管理

MySQL中存有lab需求文档所述的两张表：commodity和result。其中commodity可能会被多请求同时修改，需要加分布式锁。

为了方便的管理MySQL，我们设计了DAO层，接口如下：

```java
public interface MysqlDao {
    OrderResponse buyItem(OrderForm order, ZooKeeper zooKeeper);
    boolean storeResult(String user_id, String initiator, boolean success, double paid);
}
```

我们选择了最原始的JDBC来连接数据库，其中实例化DAO的构造函数中会完成JDBC driver类的注册，在每次需要查询数据的时候会建立连接。

在实现“购买商品”的代码中，为了保证原子性（如果有任一商品的库存不足，则判定该购买不生效，亦即不存在购买了一半的情况），我们在操作commodity表之前会查询该表检查商品是否充足。

#### Zookeeper管理

类似地，我们也在Zookeeper这里设置了DAO层。在DAO层被实例化时会建立连接，需要显式地调用close函数来关闭连接。

相关的znode如下：

```
/
|---- kiwi
      |---- CNY
      |---- USD
      |---- JPY
      |---- EUR
      |---- txAmount    # total tx amount
      |
|---- lock    # used for distributed lock
      |---- x-<session-id>-seqid
```

#### 分布式锁

分布式锁基于Zookeeper实现，基本数据结构如下：

![](http://www.sleberknight.com/blog/sleberkn/resource/dist-lock-nodes-small.png)

算法的思路很简单：要拿锁的用户首先在管理分布式锁的节点（/lock）上创建一个临时序列递增节点(EPHEMERAL_SEQUENTIAL)，如果该节点是最小的，就成功拿到了该锁；如果还有序列号比它小的，就在比它小一点的那个节点上设置Watcher。待到那个节点被删除时，再去尝试申请锁。释放锁将自己创建的节点删除即可。

但是这样还存在一个问题，如果一次连接失败，导致创建了一个znode却没被使用，可能会导致持续死锁。因此我们需要另外将sessionId嵌入到节点的name中，这样在重新连接时，该节点能够识别出该节点是自己创建的。

基于上述算法，我们实现了一个基于Zookeeper的分布式锁，并提供了lock和unlock的接口。

#### 流处理逻辑

1. 首先使用`KafkaUtils.createDirectStream`来获取Kafka的数据流
2. 使用foreachRDD和foreach方法遍历每一条从Kafka接收的数据，并将其反序列化
3. 使用MysqlDao提供的购买商品接口，传入反序列化得到的对象，得到购买结果和商品信息（价格和货币种类）
4. 如果购买失败，将失败信息写入result表，返回；如果成功，继续执行5
5. 使用ZkDao提供的接口从Zookeeper中查询当前货币汇率
6. 计算购买价格，将结果存入result表，并将对应的人民币价格存入Zookeeper

### HTTP Server

这一部分实现的是一个HTTP，负责处理Client发来的订单信息，并提供查询结果和总交易量的接口。

我们使用了Nanohttpd来做服务器，因为它比较轻量级，我们也不需要Spring提供的大部分功能。我们提供了以下几个endpoints：

| PATH           | METHOD | PARAMETER               | RESULT                            |
| -------------- | ------ | ----------------------- | --------------------------------- |
| /              | POST   | json-based order object | success or not                    |
| /amount        | GET    | <void>                  | total transaction amount          |
| /querybyid     | GET    | id                      | json-based result object          |
| /querybyuserid | GET    | user-id                 | list of json-based result objects |
|                |        |                         |                                   |

### Order Generator & Sender

这一部分是实现测试数据的生成和发送，我们使用python来完成这一部分的工作。

生成测试数据时设置以下参数：

- MAX_ITEM：每个用户最多购买的物品的种类数（默认为5）
- MAX_PURCHASE：每件商品最多的购买数量（默认为5）

在发送数据时使用requests库，同时使用多进程的方法加速发送速度。