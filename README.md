# 简介

为了学习 Spring 而开发，可以认为是一个 Spring 的精简版。从使用功能的角度出发，参考 Spring 的实现一步一步构建，最终完成一个精简版的 Spring。功能相同的类我会尽量和 Spring 的类名一样。

# 功能

## beans 模块

* 支持 `singleton` 、`prototype` 以及自定义作用域类型的 `bean`，包括初始化、构造器注入、属性注入、以及依赖注入。
* 可从 XML中读取配置。
* 支持属性注入循环依赖。
* 可自定义扩展 `bean` 实例化生命周期方法。
* 可自定义扩展 `bean` 初始化生命周期方法。

# 使用

`tiny-spring` 是逐步进行构建的，里程碑版本我都使用了 **分支** 来管理。例如，最开始的分支是  `step-1-basic-ioc-container`，那么可以使用

```shell
git checkout step-1-basic-ioc-container
```

> `master` 分支，一直保持最新的代码。