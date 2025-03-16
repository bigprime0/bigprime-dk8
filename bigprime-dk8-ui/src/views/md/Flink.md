# Flink基于session模式布署

------

## 1、准备5个yaml文件

### 1.1 flink-configuration-configmap.yaml

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: flink-config
  labels:
    app: flink
data:
  flink-conf.yaml: |+
    jobmanager.rpc.address: flink-jobmanager
    taskmanager.numberOfTaskSlots: 2
    blob.server.port: 6124
    jobmanager.rpc.port: 6123
    taskmanager.rpc.port: 6122
    jobmanager.memory.process.size: 1600m
    taskmanager.memory.process.size: 1728m
    parallelism.default: 2    
  log4j-console.properties: |+
    # 如下配置会同时影响用户代码和 Flink 的日志行为
    rootLogger.level = INFO
    rootLogger.appenderRef.console.ref = ConsoleAppender
    rootLogger.appenderRef.rolling.ref = RollingFileAppender

    # 如果你只想改变 Flink 的日志行为则可以取消如下的注释部分
    #logger.flink.name = org.apache.flink
    #logger.flink.level = INFO

    # 下面几行将公共 libraries 或 connectors 的日志级别保持在 INFO 级别。
    # root logger 的配置不会覆盖此处配置。
    # 你必须手动修改这里的日志级别。
    logger.pekko.name = org.apache.pekko
    logger.pekko.level = INFO
    logger.kafka.name= org.apache.kafka
    logger.kafka.level = INFO
    logger.hadoop.name = org.apache.hadoop
    logger.hadoop.level = INFO
    logger.zookeeper.name = org.apache.zookeeper
    logger.zookeeper.level = INFO

    # 将所有 info 级别的日志输出到 console
    appender.console.name = ConsoleAppender
    appender.console.type = CONSOLE
    appender.console.layout.type = PatternLayout
    appender.console.layout.pattern = %d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %-60c %x - %m%n

    # 将所有 info 级别的日志输出到指定的 rolling file
    appender.rolling.name = RollingFileAppender
    appender.rolling.type = RollingFile
    appender.rolling.append = false
    appender.rolling.fileName = ${sys:log.file}
    appender.rolling.filePattern = ${sys:log.file}.%i
    appender.rolling.layout.type = PatternLayout
    appender.rolling.layout.pattern = %d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %-60c %x - %m%n
    appender.rolling.policies.type = Policies
    appender.rolling.policies.size.type = SizeBasedTriggeringPolicy
    appender.rolling.policies.size.size=100MB
    appender.rolling.strategy.type = DefaultRolloverStrategy
    appender.rolling.strategy.max = 10

    # 关闭 Netty channel handler 中不相关的（错误）警告
    logger.netty.name = org.jboss.netty.channel.DefaultChannelPipeline
    logger.netty.level = OFF 
```



### 1.2 jobmanager-rest-service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: flink-jobmanager-rest
spec:
  type: NodePort
  ports:
  - name: rest
    port: 8081
    targetPort: 8081
    # 暴露对外端口
    nodePort: 30081
  selector:
    app: flink
    component: jobmanager
```



### 1.3 jobmanager-service.yaml

```
apiVersion: v1
kind: Service
metadata:
  name: flink-jobmanager
spec:
  type: ClusterIP
  ports:
  - name: rpc
    port: 6123
  - name: blob-server
    port: 6124
  - name: webui
    port: 8081
  selector:
    app: flink
    component: jobmanager
```



### 1.4 jobmanager-session-deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flink-jobmanager
spec:
  replicas: 1
  selector:
    matchLabels:
      app: flink
      component: jobmanager
  template:
    metadata:
      labels:
        app: flink
        component: jobmanager
    spec:
      containers:
      - name: jobmanager
        #image: apache/flink:1.18.1-scala_2.12
        # 使用国内镜像加快速度
        image: swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/flink:1.19.1-java11
        args: ["jobmanager"]
        ports:
        - containerPort: 6123
          name: rpc
        - containerPort: 6124
          name: blob-server
        - containerPort: 8081
          name: webui
        livenessProbe:
          tcpSocket:
            port: 6123
          initialDelaySeconds: 30
          periodSeconds: 60
        volumeMounts:
        - name: flink-config-volume
          mountPath: /opt/flink/conf
        - name: localtime
          mountPath: /etc/localtime
        securityContext:
          runAsUser: 9999  # 参考官方 flink 镜像中的 _flink_ 用户，如有必要可以修改
      volumes:
      - name: flink-config-volume
        configMap:
          name: flink-config
          items:
          - key: flink-conf.yaml
            path: flink-conf.yaml
          - key: log4j-console.properties
            path: log4j-console.properties
      - name: localtime
        hostPath:
          path: /etc/localtime
          type: ''
      
```



### 1.5 taskmanager-session-deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flink-taskmanager
spec:
  replicas: 2
  selector:
    matchLabels:
      app: flink
      component: taskmanager
  template:
    metadata:
      labels:
        app: flink
        component: taskmanager
    spec:
      containers:
      - name: taskmanager
        #image: apache/flink:1.18.1-scala_2.12
        image: swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/flink:1.19.1-java11
        args: ["taskmanager"]
        ports:
        - containerPort: 6122
          name: rpc
        livenessProbe:
          tcpSocket:
            port: 6122
          initialDelaySeconds: 30
          periodSeconds: 60
        volumeMounts:
        - name: flink-config-volume
          mountPath: /opt/flink/conf/
        - name: localtime
          mountPath: /etc/localtime          
        securityContext:
          runAsUser: 9999  # 参考官方 flink 镜像中的 _flink_ 用户，如有必要可以修改
      volumes:
      - name: flink-config-volume
        configMap:
          name: flink-config
          items:
          - key: flink-conf.yaml
            path: flink-conf.yaml
          - key: log4j-console.properties
            path: log4j-console.properties
      - name: localtime
        hostPath:
          path: /etc/localtime
          type: ''            
```

### 1.6 布署

```yaml
# 在linux服务中创建目录，copy上面的5个yaml文件到目录下，执行命令：
$ cd [文件目录]
$ kubectl apply -f.
```



## 二、说明

​	推荐在K8s中使用flink-session模式布署，可以通过HPA动态扩缩容，并有web端查看