# Apex

<p align="left">Apex是一个简单的依赖注入库</p>
<p align="left">
   <img src="https://img.shields.io/badge/JDK-8+-green.svg" alt="Build Status">
   <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="Build Status">
   <img src="https://img.shields.io/badge/Author-1619kHz-ff69b4.svg">
 </a>
 <a target="_blank" href="https://github.com/everknwon/apex">
   <img src="https://img.shields.io/badge/Copyright%20-Apex-%23ff3f59.svg" alt="Downloads"/>
 </a>
 </p>

## 安装

通过 `Maven` 或 `Gradle` 创建一个项目，因为还未上传到Maven中心仓库，所以目前暂时通过clone代码进行安装

```git
git clone https://github.com/AquiverV/apex.git

cd apex

mvn clean install
```

通过 `Maven` 安装:

```xml
<dependency>
    <groupId>org.apex</groupId>
    <artifactId>apex</artifactId>
    <version>1.0</version>
</dependency>
```

## 使用方式

```java
final Apex apex = Apex.of();
final String scanPath = aquiver.bootCls().getPackage().getName();
final List<Class<? extends Annotation>> typeAnnotations = new ArrayList<>();

//添加需要自动扫描的注解
typeAnnotations.add(Path.class);
typeAnnotations.add(RouteAdvice.class);
typeAnnotations.add(RestPath.class);
typeAnnotations.add(WebSocket.class);
typeAnnotations.add(Singleton.class);
typeAnnotations.add(ConfigBean.class);
typeAnnotations.add(PropertyBean.class);
typeAnnotations.add(Scheduled.class);
apex.typeAnnotation(typeAnnotations);
//添加扫描路径
apex.packages().add(scanPath);
apex.mainArgs(aquiver.mainArgs());
//通过instance方法获取ApexContext对象
ApexContext apexContext = ApexContext.instance();
//初始化
apexContext.init(apex);
```

## License

[MIT](https://opensource.org/licenses/MIT "MIT")

Copyright (c) 2020-present, Yi (Ever) Wang
