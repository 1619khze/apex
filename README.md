# Apex

<p align="left">A fully functional and simple dependency injection framework</p>
<p align="left">
   <img src="https://img.shields.io/badge/JDK-8+-green.svg" alt="Build Status">
   <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="Build Status">
   <img src="https://img.shields.io/badge/Author-1619kHz-ff69b4.svg">
 </a>
 <a target="_blank" href="https://github.com/everknwon/apex">
   <img src="https://img.shields.io/badge/Copyright%20-Apex-%23ff3f59.svg" alt="Downloads"/>
 </a>
 </p>

A fully functional and simple dependency injection framework

## Contents

- [Install](#install)
- [Usage](#usage)
- [License](#license)

## Install

Create a basic `Maven` or `Gradle` project.

> Do not create a `webapp` project, Aquiver does not require much trouble.

```git
git clone https://github.com/AquiverV/apex.git

cd apex

mvn clean install
```

Run with `Maven`:

```xml
<dependency>
    <groupId>org.apex</groupId>
    <artifactId>apex</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Usage

```java
final Apex apex = Apex.of();
final String scanPath = aquiver.bootCls().getPackage().getName();
final ClassgraphOptions classgraphOptions = ClassgraphOptions.builder()
        .verbose(false).realtimeLogging(false)
        .scanPackages(scanPath).build();

final Scanner scanner = new ClassgraphScanner(classgraphOptions);
final ApexContext apexContext = apex.addScanAnnotation(extendAnnotation())
        .options(classgraphOptions).scanner(scanner).packages(scanPath)
        .apexContext();

User beanByName = apexContext.getBean(User.class.getName());
```

## License

[MIT](https://opensource.org/licenses/MIT "MIT")

Copyright (c) 2020-present, Yi (Ever) Wang
