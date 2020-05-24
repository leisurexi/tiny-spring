# 第一部分：IoC 容器

## 1. step-1-基本的容器

```shell
git checkout step-1-basic-ioc-container
```

主要实现从 XML 文件中读取 `bean` 的定义信息并构建成 `BeanDefinition`，创建了一个简单的 `BeanFactory` 用来获取 `bean`，现在只可以 **依赖查找**，也就是手动去调用 `getBean` 方法去获取 `bean` 。

测试代码:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<beans>
    <bean id="user" class="com.leisurexi.tiny.spring.beans.domain.User">
        <property name="id" value="1"/>
        <property name="name" value="leisurexi"/>
    </bean>
</beans>
```

```java
@Test
public void test() {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
    int count = beanDefinitionReader.loadBeanDefinitions("META-INF/xml-beans.xml");
    log.info("加载 Bean 的数量: {}", count);
    User user = (User) beanFactory.getBean("user");
    log.info(user.toString());
}
```

## 2.step-1-bean的作用域

```shell
git checkout step-2-bean-scope
```

增加了 `bean` 的作用域，默认支持两种作用域 `singleton` 和 `prototype`；还支持自定义作用域，实现 `Scope` 接口即可。以下是 `ThreadLocal` 级别作用域的扩展示例。

首先实现 `Scope` 接口，如下：

```java
public class ThreadLocalScope implements Scope {

    private ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal() {
        // 兜底实现，防止空指针
        @Override
        protected Object initialValue() {
            return new HashMap<>();
        }
    };

    @Override
    public String scopeName() {
        return "thread-local";
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Map<String, Object> map = threadLocal.get();
        Object bean = map.get(name);
        if (bean == null) {
            bean = objectFactory.getObject();
            map.put(name, bean);
        }
        return bean;
    }

}
```

然后测试类，如下：

```java
@Test
public void scopeTest() throws InterruptedException {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    beanFactory.registerScope(new ThreadLocalScope());
    XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
    beanDefinitionReader.loadBeanDefinitions("META-INF/xml-beans.xml");
    for (int i = 0; i < 3; i++) {
        Thread thread = new Thread(() -> {
            User user = (User) beanFactory.getBean("thread-local-user");
            System.err.printf("[Thread id :%d] user = %s%n", Thread.currentThread().getId(), user.getClass().getName() + "@" + Integer.toHexString(user.hashCode()));
            User user1 = (User) beanFactory.getBean("thread-local-user");
            System.err.printf("[Thread id :%d] user1 = %s%n", Thread.currentThread().getId(), user1.getClass().getName() + "@" + Integer.toHexString(user1.hashCode()));
        });
        thread.start();
        thread.join();
    }
}
```

## 3.step-3-依赖注入

* 增加了 `bean` 的实例化前后生命周期方法回调。
* 增加了构造器自动注入的功能，默认按照类型注入，如果有多个类型匹配的 `bean` 就寻找与参数名相匹配的 `bean`。
* 增加了属性填充阶段，`bean` 的属性赋值前生命周期方法回调。
* 增加了属性按照类型自动注入的功能，如果有多个匹配的 `bean` 就寻找与参数名相匹配的 `bean`。
* 增加了属性按照名称自动注入的功能。
* 解决了属性自动注入，循环依赖的问题。
* 增加了 `bean` 的初始化阶段，`bean` 的初始化前和初始化后生命周期回调方法，以及实现 `InitialingBean` 接口的 `afterPropertiesSet()` 和自定义初始化方法的调用。

> 测试代码太多，这里就不展示了，大家可以在 `XmlBeanDefinitionReaderTest` 类下找到所有的测试用例。
