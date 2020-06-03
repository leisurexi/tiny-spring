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

## 2. step-2-bean的作用域

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

## 3. step-3-依赖注入

```shell
git checkout step-3-dependency-injection
```

* 增加了 `bean` 的实例化前后生命周期方法回调。
* 增加了构造器自动注入的功能，默认按照类型注入，如果有多个类型匹配的 `bean` 就寻找与参数名相匹配的 `bean`。
* 增加了属性填充阶段，`bean` 的属性赋值前生命周期方法回调。
* 增加了属性按照类型自动注入的功能，如果有多个匹配的 `bean` 就寻找与参数名相匹配的 `bean`。
* 增加了属性按照名称自动注入的功能。
* 解决了属性自动注入，循环依赖的问题。
* 增加了 `bean` 的初始化阶段，`bean` 的初始化前和初始化后生命周期回调方法，以及实现 `InitialingBean` 接口的 `afterPropertiesSet()` 和自定义初始化方法的调用。

> 测试代码太多，这里就不展示了，大家可以在 `XmlBeanDefinitionReaderTest` 类下找到所有的测试用例。

## 4. step-4-ApplicationContext 登场

```shell
git checkout step-4-application-context
```

现在 `BeanFactory` 的功能齐全了，但是每次使用都需要手动调用 `XmlBeanDefinitionReader#loadBeanDefinitions(String)`。于是我们引入熟悉的 `ApplicationContext` 接口，并在 `AbstractApplicationContext` 的 `refresh()` 方法中进行bean的初始化工作；同时添加注解 `@Autowired`、`@Component`、`@Scope`，可以使用 `<context:component-scan>` 标签来指定包路径实现自动扫包，`ApplicationContext` 会自动把标注了 `@Component` 的类注册为 `bean`，同时支持在 `bean` 的属性上标注 `@Autowired` 注解来实现自动注入。下面是一个使用示例：

XML 文件：

```xml
<?xml version="1.0" encoding="utf-8" ?>
<beans>

    <bean id="user" class="com.leisurexi.tiny.spring.context.domain.User">
        <property name="id" value="1"/>
        <property name="name" value="leisurexi"/>
    </bean>

    <context:component-scan base-package="com.leisurexi.tiny.spring.context"/>

</beans>
```

实体类大家自己去 `context` 模块的测试文件夹下找，然后我们看一下测试代码：

```java
@Slf4j
public class ClassPathApplicationContextTest {

    @Test
    public void test() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/classpath-application-context.xml");
        User user = context.getBean("user", User.class);
        log.info(user.toString());
    }

}
```

## 5. step-5-注解驱动

```java
git checkout step-5-annotation-application-context
```

之前我们想启动 IoC 容器获取 `bean` 都需要 XML 配置文件来完成，现在引入注解驱动 `AnnotationConfigApplicationContext` 再也不用写 XML 啦！

下面是一个简单的示例，这里只展示关键代码，其他可以去 `context` 模块的测试文件夹下找。

配置类：

```java
@Configuration
@ComponentScan(basePackages = "com.leisurexi.tiny.spring.context")
public class BeanConfig {

    @Bean
    public City city() {
        City city = City.builder()
                .id(1L)
                .name("北京")
                .build();
        return city;
    }

    @Bean
    public User user(City city) {
        User user = User.builder()
                .id(1L)
                .name("leisurexi")
                .city(city)
                .build();
        return user;
    }

}
```

测试类：

```java
@Slf4j
public class AnnotationConfigApplicationContextTest {

    @Test
    public void test() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(BeanConfig.class);
        context.refresh();
        UserService userService = context.getBean(UserService.class);
        userService.save();
    }

}
```

至此 IoC 部分已经结束了，后续会进行 AOP 模块的实现。 

