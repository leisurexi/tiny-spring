# 简介

为了学习 Spring 而开发，可以认为是一个 Spring 的精简版。从使用功能的角度出发，参考 Spring 的实现一步一步构建，最终完成一个精简版的 Spring。功能相同的类我会尽量和 Spring 的类名一样。

# 功能

## beans 模块

* 支持 `singleton` 、`prototype` 以及自定义作用域类型的 `bean`，包括初始化、构造器注入、属性注入、以及依赖注入。
* 可从 XML中读取配置。
* 支持属性注入循环依赖。
* 可自定义扩展 `bean` 实例化生命周期方法。
* 可自定义扩展 `bean` 初始化生命周期方法。

## context 模块

* 使用 `ClassPathApplicationContext` 支持 XML 文件 `context:compoment-scan` 标签的指定包扫描标注了 `@Component` 注解的类，并把该类注册为 `bean`，并且支持 `@Autowired` 注解给属性自动注入。
* 增加 `AnnotationConfigApplicationContext` 支持完全脱离 XML 文件来启动上下文，更增加 `@Configuration`、`@ComponemtScan`、`@Bean` 注解，使用方法基本和 Spring 一致。

# 使用

`tiny-spring` 是逐步进行构建的，里程碑版本我都使用了 **分支** 来管理。例如，最开始的分支是  `step-1-basic-ioc-container`，那么可以使用

```shell
git checkout step-1-basic-ioc-container
```

来获得这一版本。或者可以通过 IntelliJ IDEA 工具来切换分支，可以点击链接查看图片，[切换分支图片](http://ww1.sinaimg.cn/large/006Vpl27gy1gf3kvly17kj30bu07z0sy.jpg)。

> `master` 分支，一直保持最新的代码。
>
> 版本历史见[`changelog.md`](https://github.com/leisurexi/tiny-spring/blob/master/changelog.md)。