# 简介
为了学习 Spring 而开发，可以认为是一个 Spring 的精简版。从使用功能的角度出发，参考 Spring 的实现一步一步构建，最终完成一个精简版的 Spring。功能相同的类我会尽量和 Spring 的类名一样。

# 模块

## beans

### 版本

#### 0.0.1

主要实现从 XML 文件中读取 `Bean` 的定义信息并构建成 `BeanDefinition`，创建了一个简单的 `BeanFactory` 用来获取 `Bean`，现在只可以 **依赖查找**，也就是手动去调用 `getBean` 方法去获取 `Bean` 。