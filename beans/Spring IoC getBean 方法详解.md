

# 前言

本篇文章主要介绍 Spring IoC 容器 `getBean()` 方法。

下图是一个大致的流程图：

![](http://ww1.sinaimg.cn/large/006Vpl27gy1ge1rh4g3o5j30pc0p7dh4.jpg)

# 正文

首先定义一个简单的 POJO，如下：

```java
public class User {

	private Long id;
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
```

再编写一个 XML 文件。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

	<bean id="user" class="com.leisurexi.ioc.domain.User">
		<property name="id" value="1"/>
		<property name="name" value="leisurexi"/>
	</bean>
    
</beans>
```

最后再来一个测试类。

```java
@Test
public void test(){
	DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
	XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
	reader.loadBeanDefinitions("META-INF/spring-bean.xml");
	User user = beanFactory.getBean("user", User.class);
	System.out.println(user);
}
```

上面的代码还是上篇文章的示例代码，这次我们主要分析 `beanFactory.getBean()` 方法。 

## AbstractBeanFactory#getBean

```java
/**
* @param name 		  bean的名称
* @param requiredType bean的类型
*/
public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
	// 调用 doGetBean 方法(方法以do开头实际做操作的方法)
	return doGetBean(name, requiredType, null, false);
}

/**
* @param name          bean的名称
* @param requiredType  bean的类型
* @param args          显示传入的构造参数
* @param typeCheckOnly 是否仅仅做类型检查
*/
protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
			@Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {
	// 获取bean的实际名称，见下文详解
	final String beanName = transformedBeanName(name);
	Object bean;

	// Eagerly check singleton cache for manually registered singletons.
	// 直接尝试从缓存获取或 singletonFactories 中的 ObjectFactory 中获取，见下文详解
	Object sharedInstance = getSingleton(beanName);
	if (sharedInstance != null && args == null) {
		if (logger.isTraceEnabled()) {
			if (isSingletonCurrentlyInCreation(beanName)) {
				logger.trace("Returning eagerly cached instance of singleton bean '" + beanName +
						"' that is not fully initialized yet - a consequence of a circular reference");
			}
			else {
				logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
			}
		}
		// 检查bean是否是FactoryBean的实现。不是直接返回bean，是的话首先检查beanName是否以 & 开头
		// 如果是返回FactoryBean本身，不是调用FactoryBean#getObject()返回对象
        // 见下文详解
		bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
	}

	else {
        // Fail if we're already creating this bean instance:
		// We're assumably within a circular reference.
		// 只有在单例情况下才会去尝试解决循环依赖，原型模式下，如果存在A中有
		// B属性，B中有A属性，那么当依赖注入时，就会产生当A还未创建完的时候
		// 对于B的创建而在此返回创建A，造成循环依赖
		if (isPrototypeCurrentlyInCreation(beanName)) {
			throw new BeanCurrentlyInCreationException(beanName);
		}

		// Check if bean definition exists in this factory.
		// 检查当前bean的BeanDefinition是否在当前的beanFactory，不在递归调用父工厂的getBean()去获取bean
		BeanFactory parentBeanFactory = getParentBeanFactory();
		if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
			// Not found -> check parent.
			String nameToLookup = originalBeanName(name);
			if (parentBeanFactory instanceof AbstractBeanFactory) {
				return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
							nameToLookup, requiredType, args, typeCheckOnly);
			}
			else if (args != null) {
				// Delegation to parent with explicit args.
				return (T) parentBeanFactory.getBean(nameToLookup, args);
			}
			else if (requiredType != null) {
				// No args -> delegate to standard getBean method.
				return parentBeanFactory.getBean(nameToLookup, requiredType);
			}
			else {
				return (T) parentBeanFactory.getBean(nameToLookup);
			}
		}
		// 如果不是仅仅做类型检查，则是创建bean，这里要进行记录
		if (!typeCheckOnly) {
			// 记录bean已经创建过，见下文详解
			markBeanAsCreated(beanName);
		}

		try {
			// 合并BeanDefinition，见下文详解
			final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
			checkMergedBeanDefinition(mbd, beanName, args);

			// Guarantee initialization of beans that the current bean depends on.
			// 实例化bean前先实例化依赖bean，也就是depends-on属性中配置的beanName
			String[] dependsOn = mbd.getDependsOn();
			if (dependsOn != null) {
				for (String dep : dependsOn) {
					// 检查是否循环依赖，即当前bean依赖dep，dep依赖当前bean，见下文详解
					if (isDependent(beanName, dep)) {
						throw new BeanCreationException(mbd.getResourceDescription(), beanName,
								"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
					}
					// 将dep和beanName的依赖关系放入到缓存中，见下文详解
					registerDependentBean(dep, beanName);
					try {
						// 获取依赖dep对应的bean实例，如果还未创建实例，则先去创建
						getBean(dep);
					}
					catch (NoSuchBeanDefinitionException ex) {
						throw new BeanCreationException(mbd.getResourceDescription(), beanName,
									"'" + beanName + "' depends on missing bean '" + dep + "'", ex);
					}
				}
			}

			// Create bean instance.
			// 如果 bean 的作用域是单例
			if (mbd.isSingleton()) {
				// 创建和注册单例 bean，见下文详解
				sharedInstance = getSingleton(beanName, () -> {
					try {
						// 创建 bean 实例，下篇文章详解
						return createBean(beanName, mbd, args);
					}
					catch (BeansException ex) {
						// Explicitly remove instance from singleton cache: It might have been put there
						// eagerly by the creation process, to allow for circular reference resolution.
						// Also remove any beans that received a temporary reference to the bean.
						destroySingleton(beanName);
						throw ex;
					}
				});
				// 上文解释过，这里不再赘述
				bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
			}
			// bean 的作用域是原型
			else if (mbd.isPrototype()) {
				// It's a prototype -> create a new instance.
				Object prototypeInstance = null;
				try {
					// 原型 bean 创建前回调，默认实现是将 beanName 保存到 prototypesCurrentlyInCreation 缓存中
					beforePrototypeCreation(beanName);
					// 创建 bean 实例，下篇文章详解
					prototypeInstance = createBean(beanName, mbd, args);
				}
				finally {
					// 原型 bean 创建后回调，默认实现是将 beanName 从 prototypesCurrentlyInCreation 缓存中移除
					afterPrototypeCreation(beanName);
				}
				// 上文解释过，这里不再赘述
				bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
			}
			// 自定义作用域
			else {
				// 获取自定义作用域名称
				String scopeName = mbd.getScope();
				// 获取作用域对象
				final Scope scope = this.scopes.get(scopeName);
				// 如果为空表示作用域未注册，抛出异常
				if (scope == null) {
					throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
				}
				try {
					// 其他 Scope 的 bean 创建(新建了一个 ObjectFactory，并且重写了 getObject 方法)
					Object scopedInstance = scope.get(beanName, () -> {
						// 原型 bean 创建前回调，默认实现是将 beanName 保存到 prototypesCurrentlyInCreation 缓存中
						beforePrototypeCreation(beanName);
						try {
                            // 创建 bean 实例，下篇文章详解
							return createBean(beanName, mbd, args);
						}
						finally {
							// 原型 bean 创建后回调，默认实现是将 beanName 从 prototypesCurrentlyInCreation 缓存中移除
							afterPrototypeCreation(beanName);
						}
					});
					// 上文解释过，这里不再赘述
					bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
				}
				catch (IllegalStateException ex) {
					throw new BeanCreationException(beanName,
							"Scope '" + scopeName + "' is not active for the current thread; consider " +
							"defining a scoped proxy for this bean if you intend to refer to it from a singleton",
							ex);
				}
			}
		}
		catch (BeansException ex) {
			cleanupAfterBeanCreationFailure(beanName);
			throw ex;
		}
	}

	// Check if required type matches the type of the actual bean instance.
	// 检查所需的类型是否与实际 bean 实例的类型匹配
	if (requiredType != null && !requiredType.isInstance(bean)) {
		try {
			// 如果类型不等，进行转换，转换失败抛出异常；转换成功直接返回
			T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
			if (convertedBean == null) {
				throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
			}
			return convertedBean;
		}
		catch (TypeMismatchException ex) {
			if (logger.isTraceEnabled()) {
				logger.trace("Failed to convert bean '" + name + "' to required type '" +
						ClassUtils.getQualifiedName(requiredType) + "'", ex);
			}
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
		}
	}
	// 返回 bean 实例
	return (T) bean;
}
```

## AbstractBeanFactory#transformedBeanName

```java
protected String transformedBeanName(String name) {
	return canonicalName(BeanFactoryUtils.transformedBeanName(name));
}

// BeanFactoryUtils.java
public static String transformedBeanName(String name) {
	Assert.notNull(name, "'name' must not be null");
	// 如果name不是&开头，直接返回
	if (!name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
		return name;
	}
	// 去除name的&前缀
	return transformedBeanNameCache.computeIfAbsent(name, beanName -> {
		do {
			beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
		}
		while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
		return beanName;
	});
}

// SimpleAliasRegistry.java
public String canonicalName(String name) {
	String canonicalName = name;
	// Handle aliasing...
	String resolvedName;
	// 如果name是别名，则会循环去查找bean的实际名称
	do {
		resolvedName = this.aliasMap.get(canonicalName);
		if (resolvedName != null) {
			canonicalName = resolvedName;
		}
	}
	while (resolvedName != null);
	return canonicalName;
}
```

上面代码首先去除 `FactoryBean` 的修饰符，比如 `name=&aa` ，那么会首先去除 `&` 使 `name=aa`。然后取 `alias` 所表示的最终 `beanName`。

我们这里简单介绍什么是 `FactoryBean` 。

一般情况下，Spring 通过反射机制利用 `bean` 的 `class` 属性指定实现类来实例化 `bean`。在某些情况下，实例化 `bean` 过程比较复杂，如果按照传统的方式，则需要在 `<bean>` 中提供大量的配置信息，配置方式的灵活性是受限的，这是采用编码的方式可能会得到一个简单的方案。Spring 为此提供了 `org.springframework.bean.factory.FactoryBean` 的工厂类接口，用户可以通过实现该接口定制实例化 `bean` 的逻辑。

`FactoryBean` 接口对于 Spring 框架来说占有重要的地位，Spring 自身就提供了70多个 `FactoryBean` 的实现。它们隐藏了一下复杂 `bean` 的细节，给上层应用带来了便利。下面是该接口的定义：

```java
public interface FactoryBean<T> {

    // 返回由FactoryBean创建的bean实例，如果isSingleton()返回true，
    // 则该实例会放到Spring容器中单例缓存池中
	@Nullable
	T getObject() throws Exception;
	
    // 返回FactoryBean创建的bean类型
	@Nullable
	Class<?> getObjectType();

    // 返回由FactoryBean创建的bean实例的作用域是singleton还是prototype
	default boolean isSingleton() {
		return true;
	}

}
```

当配置文件中 `<bean>` 的 `class` 属性配置的实现类时 `FactoryBean` 时，通过 `getBean()` 返回的不是 `FactoryBean` 本身，而是 `FactoryBean#getObject()` 所返回的对象，相当于 `FactoryBean#getObject()` 代理了 `getBean()`。下面用简单的代码演示一下：

首先定义一个 `Car` 实体类：

```java
public class Car {
	
	private Integer maxSpeed;
	private String brand;
	private Double price;

	public Integer getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(Integer maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}
}
```

上面的实体类，如果用传统方式配置，每一个属性都会对应一个 `<property>` 元素标签。如果用 `FactoryBean` 的方式实现就会灵活一点，下面通过逗号分隔的方式一次性的为 `Car` 的所有属性配置值。

```java
public class CarFactoryBean implements FactoryBean<Car> {
	
	private String carInfo;
	
	@Override
	public Car getObject() throws Exception {
		Car car = new Car();
		String[] infos = carInfo.split(",");
		car.setBrand(infos[0]);
		car.setMaxSpeed(Integer.valueOf(infos[1]));
		car.setPrice(Double.valueOf(infos[2]));
		return car;
	}

	@Override
	public Class<?> getObjectType() {
		return Car.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public String getCarInfo() {
		return carInfo;
	}

	public void setCarInfo(String carInfo) {
		this.carInfo = carInfo;
	}
}
```

接下来，我们在 XML 中配置。

```XML
<bean id="car" class="com.leisurexi.ioc.domain.CarFactoryBean">
	<property name="carInfo" value="超级跑车,400,2000000"/>
</bean>
```

最后看下测试代码和运行结果：

```java
@Test
public void factoryBeanTest() {
	DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
	XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
	reader.loadBeanDefinitions("META-INF/spring-bean.xml");
	Car car = beanFactory.getBean("car", Car.class);
	System.out.println(car);
	CarFactoryBean carFactoryBean = beanFactory.getBean("&car", CarFactoryBean.class);
	System.out.println(carFactoryBean);
}
```

![](http://ww1.sinaimg.cn/large/006Vpl27gy1gduss8hhfpj30g201rdfw.jpg)

可以看到如果 `beanName` 前面加上 `&` 获取的是 `FactoryBean` 本身，不加获取的 `getObject()` 返回的对象。

## AbstractBeanFactory#getSingleton

```java
public Object getSingleton(String beanName) {
	// allowEarlyReference设置为true表示允许早期依赖
	return getSingleton(beanName, true);
}

/**
* @param allowEarlyReference  是否提前创建曝光
*/
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
	// 检查单例传中是否存在
	Object singletonObject = this.singletonObjects.get(beanName);
	if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
		// 如果为空，锁定全局变量进行处理
		synchronized (this.singletonObjects) {
			singletonObject = this.earlySingletonObjects.get(beanName);
			if (singletonObject == null && allowEarlyReference) {
				// 当某些方法需要提前初始化时则会调用addSingletonFactory方法将对应的
				// ObjectFactory 初始化策略存储在 singletonFactories
				ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
				if (singletonFactory != null) {
					// 调用预先设定的getObject()
					singletonObject = singletonFactory.getObject();
					// 记录在缓存中，earlySingletonObjects和singletonFactories
					this.earlySingletonObjects.put(beanName, singletonObject);
					this.singletonFactories.remove(beanName);
				}
			}
		}
	}
	return singletonObject;
}
```

上面代码涉及到循环依赖的检测，首先尝试从 `singletonObjects` 里面获取实例，如果获取不到再从 `earlySingletonObjects` 里面获取，如果还获取不到，再尝试从 `singletonFactories` 里面获取 `beanName` 对应的 `ObjectFactory`，然后调用这个 `ObjectFactory` 的 `getObject()` 来创建 `bean`，并放到 `earlySingletonObjects` 里面去，并且从 `singletonFactories` 里面 `remove` 掉这个 `ObjectFactory` ，而对于后续的所有内存操作都只为了循环依赖检测时候用，也就是在 `allowEarlyReference` 为 `true` 的情况下才会使用。

这里涉及用于存储 `bean` 不同的 `map`，下面简单解释下：

* **singletonObjects：**同于保存 `beanName` 和 `bean` 实例之间的关系。
* **singletonFactories：**用于保存 `beanName` 和创建 `bean` 的工厂之间的关系。
* **earlySingletonObjects：**也是保存 `beanName` 和 `bean` 实例之间的关系，与 `singletonObjects` 的不同之处在于，当一个单例 `bean` 被放到这里后，那么当 `bean` 还在创建过程中，就可以通过 `getBean()` 获取到了，其目的是用来检测循环引用。
* **registeredSingletons：**用来保存当前所有已注册的 `bean`。 

## AbstractBeanFactory#getObjectForBeanInstance

```java
protected Object getObjectForBeanInstance(
			Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {

	// Don't let calling code try to dereference the factory if the bean isn't a factory.
	// name 是否以 & 开头
	if (BeanFactoryUtils.isFactoryDereference(name)) {
		// 如果是 null 直接返回
		if (beanInstance instanceof NullBean) {
			return beanInstance;
		}
		// beanName 以 & 开头，但又不是 FactoryBean 类型，抛出异常
		if (!(beanInstance instanceof FactoryBean)) {
			throw new BeanIsNotAFactoryException(beanName, beanInstance.getClass());
		}
		// 设置 isFactoryBean 为 true
		if (mbd != null) {
			mbd.isFactoryBean = true;
		}
		// 返回 bean 实例
		return beanInstance;
	}

	// Now we have the bean instance, which may be a normal bean or a FactoryBean.
	// If it's a FactoryBean, we use it to create a bean instance, unless the
	// caller actually wants a reference to the factory.
	// name 不是 & 开头，并且不是 FactoryBean 类型，直接返回
	if (!(beanInstance instanceof FactoryBean)) {
		return beanInstance;
	}

	Object object = null;
	if (mbd != null) {
		mbd.isFactoryBean = true;
	}
	else {
		// 从缓存中获取实例
		object = getCachedObjectForFactoryBean(beanName);
	}
	if (object == null) {
		// Return bean instance from factory.
		// 将 beanInstance 强转成 FactoryBean
		FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
		// Caches object obtained from FactoryBean if it is a singleton.
		// 合并 BeanDefinition
		if (mbd == null && containsBeanDefinition(beanName)) {
			mbd = getMergedLocalBeanDefinition(beanName);
		}
		boolean synthetic = (mbd != null && mbd.isSynthetic());
		// 获取实例
		object = getObjectFromFactoryBean(factory, beanName, !synthetic);
	}
	return object;
}

// FactoryBeanRegistrySupport.java
protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
	// 如果是单例 bean，并且已经存在缓存中
	if (factory.isSingleton() && containsSingleton(beanName)) {
		// 加锁
		synchronized (getSingletonMutex()) {
			// 从缓存中获取
			Object object = this.factoryBeanObjectCache.get(beanName);
			if (object == null) {
				// 调用 FactoryBean 的 getObject() 获取实例
				object = doGetObjectFromFactoryBean(factory, beanName);
				// Only post-process and store if not put there already during getObject() call above
				// (e.g. because of circular reference processing triggered by custom getBean calls)
				Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
				// 如果该 beanName 已经在缓存中存在，则将 object 替换成缓存中的
				if (alreadyThere != null) {
					object = alreadyThere;
				}
				else {
					if (shouldPostProcess) {
						// 如果当前 bean 还在创建中，直接返回
						if (isSingletonCurrentlyInCreation(beanName)) {
							// Temporarily return non-post-processed object, not storing it yet..
							return object;
						}
						// 单例 bean 创建前回调
						beforeSingletonCreation(beanName);
						try {
							// 对从 FactoryBean 获得给定对象后处理，默认按原样返回
							object = postProcessObjectFromFactoryBean(object, beanName);
						}
						catch (Throwable ex) {
							throw new BeanCreationException(beanName,
										"Post-processing of FactoryBean's singleton object failed", ex);
						}
						finally {
							// 单例 bean 创建后回调
							afterSingletonCreation(beanName);
						}
					}
					if (containsSingleton(beanName)) {
						// 将 beanName 和 object 放到 factoryBeanObjectCache 缓存中
						this.factoryBeanObjectCache.put(beanName, object);
					}
				}
			}
			// 返回实例
			return object;
		}
	}
	else {
		// 调用 FactoryBean 的 getObject() 获取实例
		Object object = doGetObjectFromFactoryBean(factory, beanName);
		if (shouldPostProcess) {
			try {
				// 对从 FactoryBean 获得给定对象后处理，默认按原样返回
				object = postProcessObjectFromFactoryBean(object, beanName);
			}
			catch (Throwable ex) {
				throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
			}
		}
		// 返回实例
		return object;
	}
}

// FactoryBeanRegistrySupport.java
private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName) throws BeanCreationException {

	Object object;
	try {
		if (System.getSecurityManager() != null) {
			AccessControlContext acc = getAccessControlContext();
			try {
				object = AccessController.doPrivileged((PrivilegedExceptionAction<Object>) factory::getObject, acc);
			}
			catch (PrivilegedActionException pae) {
				throw pae.getException();
			}
		}
		else {
			// 调用 getObject() 获取实例
			object = factory.getObject();
		}
	}
	catch (FactoryBeanNotInitializedException ex) {
		throw new BeanCurrentlyInCreationException(beanName, ex.toString());
	}
	catch (Throwable ex) {
		throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
	}

	// Do not accept a null value for a FactoryBean that's not fully
	// initialized yet: Many FactoryBeans just return null then.
	// 如果 object 为 null，并且当前 singleton bean 正在创建中，抛出异常
	if (object == null) {
		if (isSingletonCurrentlyInCreation(beanName)) {
			throw new BeanCurrentlyInCreationException(
					beanName, "FactoryBean which is currently in creation returned null from getObject");
		}
		object = new NullBean();
	}
	// 返回 object 实例
	return object;
}
```

上面代码总结起来就是：如果 `beanName` 以 `&` 开头，直接返回 `FactoryBean` 实例；否则调用 `getObject()` 方法获取实例，然后执行 `postProcessObjectFromFactoryBean()` 回调，可以在回调方法中修改实例，默认按原样返回。

## AbstractBeanFactory#getMergedLocalBeanDefinition

> 下文将合并后的 `BeanDefinition` 简称为 `MergedBeanDefinition` 。

```java
protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
	// Quick check on the concurrent map first, with minimal locking.
	// 获取当前bean合并后的BeanDefinition
	RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
	// 如果存在合并后的BeanDefinition，并且不是过期的，直接返回
	if (mbd != null && !mbd.stale) {
		return mbd;
	}
	// 获取已经注册的BeanDefinition然后去合并
	return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
}

protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd)
		throws BeanDefinitionStoreException {
	// 顶级bean获取合并后的BeanDefinition
	return getMergedBeanDefinition(beanName, bd, null);
}

/**
* @param containingBd 如果是嵌套bean该值为顶级bean，如果是顶级bean该值为null
*/
protected RootBeanDefinition getMergedBeanDefinition(
		String beanName, BeanDefinition bd, @Nullable BeanDefinition containingBd)
		throws BeanDefinitionStoreException {

	synchronized (this.mergedBeanDefinitions) {
		// 本次的RootBeanDefinition
		RootBeanDefinition mbd = null;
		// 以前的RootBeanDefinition
		RootBeanDefinition previous = null;

		// Check with full lock now in order to enforce the same merged instance.
		// 如果bean是顶级bean，直接获取合并后的BeanDefinition
		if (containingBd == null) {
			mbd = this.mergedBeanDefinitions.get(beanName);
		}
		// 没有合并后的BeanDefinition || BeanDefinition过期了
		if (mbd == null || mbd.stale) {
			previous = mbd;
			// 如果bean没有parent
			if (bd.getParentName() == null) {
				// Use copy of given root bean definition.
				// 如果bd本身就是RootBeanDefinition直接复制一份，否则创建一个
				if (bd instanceof RootBeanDefinition) {
					mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
				}
				else {
					mbd = new RootBeanDefinition(bd);
				}
			}
			else {
				// Child bean definition: needs to be merged with parent.
				// bean有parent
				BeanDefinition pbd;
				try {
					// 获取parent bean的实际名称
					String parentBeanName = transformedBeanName(bd.getParentName());
					if (!beanName.equals(parentBeanName)) {
						// 当前beanName不等于它的parent beanName
						// 获取parent合并后的BeanDefinition
						pbd = getMergedBeanDefinition(parentBeanName);
					}
					else {
						// 如果父定义的beanName与bd的beanName相同，则拿到父BeanFactory
						// 只有在存在父BeanFactory的情况下，才允许父定义beanName与自己相同
						BeanFactory parent = getParentBeanFactory();
						if (parent instanceof ConfigurableBeanFactory) {
							// 如果父BeanFactory是ConfigurableBeanFactory
							// 则通过父BeanFactory获取parent合并后的BeanDefinition
							pbd = ((ConfigurableBeanFactory) parent).getMergedBeanDefinition(parentBeanName);
						}
						else {
							// 如果父BeanFactory不是ConfigurableBeanFactory，抛出异常
							throw new NoSuchBeanDefinitionException(parentBeanName,
									"Parent name '" + parentBeanName + "' is equal to bean name '" + beanName +
									"': cannot be resolved without an AbstractBeanFactory parent");
						}
					}
				}
				catch (NoSuchBeanDefinitionException ex) {
					throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
							"Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
				}
				// Deep copy with overridden values.
				// 使用父定义pbd构建一个新的RootBeanDefinition对象（深拷贝）
				mbd = new RootBeanDefinition(pbd);
				// 覆盖与parent相同的属性，
				mbd.overrideFrom(bd);
			}

			// Set default singleton scope, if not configured before.
			// 如果bean没有设置scope属性，默认是singleton
			if (!StringUtils.hasLength(mbd.getScope())) {
				mbd.setScope(RootBeanDefinition.SCOPE_SINGLETON);
			}

			// A bean contained in a non-singleton bean cannot be a singleton itself.
			// Let's correct this on the fly here, since this might be the result of
			// parent-child merging for the outer bean, in which case the original inner bean
			// definition will not have inherited the merged outer bean's singleton status.
			// 当前bean是嵌套bean && 顶级bean的作用域不是单例 && 当前bean的作用域是单例
			// 这里总结起来就是，如果顶层bean不是单例的，那么嵌套bean也不能是单例的
			if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
				// 设置当前bean的作用域和顶级bean一样
				mbd.setScope(containingBd.getScope());
			}

			// Cache the merged bean definition for the time being
			// (it might still get re-merged later on in order to pick up metadata changes)
			// 当前bean是顶级bean && 缓存bean的元数据(该值默认为true)
			if (containingBd == null && isCacheBeanMetadata()) {
				// 将当前bean合并后的RootBeanDefinition缓存起来
				this.mergedBeanDefinitions.put(beanName, mbd);
			}
		}
		// 以前的RootBeanDefinition不为空，拷贝相关的BeanDefinition缓存
		if (previous != null) {
			copyRelevantMergedBeanDefinitionCaches(previous, mbd);
		}
		return mbd;
	}
}
	
```

上面代码主要是获取 `MergedBeanDefinition` ，主要步骤如下：

1. 首先从缓存中获取 `bean` 的 `MergedBeanDefinition`，如果存在并且未过期直接返回。

2. 不存在或者已过期的 `MergedBeanDefinition` ，获取已经注册的 `BeanDefinition` 去作为顶级 `bean` 合并。

3. `bean` 没有 `parent` (就是 XML 中的 parent 属性)，直接封装成 `RootBeanDefinition` 。

4. `bean` 有 `parent` ，先去获取父 `MergedBeanDefinition` ，然后覆盖和合并与 `parent` 相同的属性。

   > 注意：这里只有 `abstract`、`scope`、`lazyInit`、`autowireMode`、`dependencyCheck`、`dependsOn` 、`factoryBeanName`、`factoryMethodName`、`initMethodName`、`destroyMethodName`会覆盖，而 `constructorArgumentValues`、`propertyValues`、`methodOverrides` 会合并。

5. 如果没有设置作用域，默认作用域为 `singleton` 。

6. 缓存 `MergedBeanDefinition` 。

上文中提到如果 `bean` 有 `parent`，会合并一些属性，这里我们稍微展示一下合并后的 `propertyValues`:

首先定义一个 `SuperUser` 继承上面定义的 `User`，如下：

```java
public class SuperUser extends User {

	private String address;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "SuperUser{" +
				"address='" + address + '\'' +
				'}';
	}

}
```

然后我们在 XML 文件中配置一下，如下:

```XML
<bean id="superUser" class="com.leisurexi.ioc.domain.SuperUser" parent="user">
	<property name="address" value="北京"/>
 </bean>
```

然后下图是我 Debug 的截图，可以看到 `superUser` 的 `propertyValues` 合并了 `user` 的 `id` 和 `name` 属性。

![](http://ww1.sinaimg.cn/large/006Vpl27gy1gdtqv4ppzxj30g70cjdgv.jpg)

上文还提到了嵌套 `bean` ，下面我们简单看一下什么是嵌套 `bean`。

在 Spring 中，如果某个 `bean` 所依赖的 `bean` 不想被 Spring 容器直接访问，可以使用嵌套 `bean`。和普通的 `bean` 一样，使用 `bean` 元素来定义嵌套的 `bean`，嵌套 `bean` 只对它的外部 `bean` 有效，Spring 无法直接访问嵌套 `bean` ，因此定义嵌套 `bean` 也无需指定 `id` 属性。如下配置片段是一个嵌套 `bean` 示例：

![](http://ww1.sinaimg.cn/large/006Vpl27gy1gduqtoyjc4j30kg083aax.jpg)

采用上面的配置形式可以保证嵌套 `bean` 不能被容器访问，因此不用担心其他程序修改嵌套 `bean`。外部 `bean` 的用法和使用结果和以前没有区别。

> 嵌套 `bean` 提高了 `bean` 的内聚性，但是降低了程序的灵活性。只有在确定无需通过 Spring 容器访问某个 `bean` 实例时，才考虑使用嵌套 `bean` 来定义。

## DefaultSingletonBeanRegistry#isDependent

```java
protected boolean isDependent(String beanName, String dependentBeanName) {
	// 加锁
	synchronized (this.dependentBeanMap) {
		// 检测beanName和dependentBeanName是否有循环依赖
		return isDependent(beanName, dependentBeanName, null);
	}
}

private boolean isDependent(String beanName, String dependentBeanName, @Nullable Set<String> alreadySeen) {
	// 如果当前bean已经检测过，直接返回false
	if (alreadySeen != null && alreadySeen.contains(beanName)) {
		return false;
	}
	// 解析别名
	String canonicalName = canonicalName(beanName);
	// 获取canonicalName所依赖beanName集合
	Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
	// 如果为空，两者还未确定依赖关系，返回false
	if (dependentBeans == null) {
		return false;
	}
	// 如果dependentBeanName已经存在于缓存中，两者已经确定依赖关系，返回true
	if (dependentBeans.contains(dependentBeanName)) {
		return true;
	}
	// 循环检查，即检查依赖canonicalName的所有beanName是否被dependentBeanName依赖(即隔层依赖)
	for (String transitiveDependency : dependentBeans) {
		if (alreadySeen == null) {
			alreadySeen = new HashSet<>();
		}
		// 将已经检查过的记录下来，下次直接跳过
		alreadySeen.add(beanName);
		if (isDependent(transitiveDependency, dependentBeanName, alreadySeen)) {
			return true;
		}
	}
	return false;
}
```

这里的 `dependentBeanMap` 其实是 `beanName` 和其依赖的 `dependentBeanName` 反过来存的。比如，A 依赖 B，B 依赖 A；那么首先调用 `getBean()` 获取 A，然后到 `isDependent()` ，因为是第一次进来所以 `dependentBeans` 是空的直接返回 `false`，接着到下面 `registerDepenndentBean()` ，这里先将 `dependentBeanName` 作为 `key`，`value` 是添加了 `beanName` 的 `LinkedHashSet` ，添加进 `dependentBeanMap`；然后因为依赖 B，所以去实例化 B，又由于 B 依赖 A，到了 `isDepnedent()`，接着 `dependentBeans.contains(dependentBeanName)` 这行代码会返回 `true` (因为在实例化 A 的过程中，已经将 B 作为 `key` 放入了 `dependentBeanMap`)，最后直接抛出 **循环引用** 的异常。

## DefaultSingletonBeanRegistry#registerDependentBean

```java
public void registerDependentBean(String beanName, String dependentBeanName) {
    // 解析别名
	String canonicalName = canonicalName(beanName);
	// 加锁
	synchronized (this.dependentBeanMap) {
		// 获取canonicalName依赖beanName集合，如果为空默认创建一个LinkedHashSet当做默认值
		Set<String> dependentBeans =
				this.dependentBeanMap.computeIfAbsent(canonicalName, k -> new LinkedHashSet<>(8));
		// 如果dependentBeanName已经记录过了，直接返回
		if (!dependentBeans.add(dependentBeanName)) {
			return;
		}
	}
	// 加锁
	synchronized (this.dependenciesForBeanMap) {
		// 这里是和上面的dependentBeanMap倒过来，key为dependentBeanName
		Set<String> dependenciesForBean =
				this.dependenciesForBeanMap.computeIfAbsent(dependentBeanName, k -> new LinkedHashSet<>(8));
		dependenciesForBean.add(canonicalName);
	}
}
```

这个方法又引入了一个跟 `dependentBeanMap` 类似的缓存 `dependenciesForBeanMap`。这两个缓存很容易搞混，这里再举一个简单的例子：A 依赖 B，那么 `dependentBeanMap` 存放的是 `key` 为 B，`value` 为含有 A 的 `Set`；而 `dependenciesForBeanMap` 存放的是`key` 为 A，`value` 为含有 B 的 `Set`。

## DefaultSingletonBeanRegistry#getSingleton

```java
public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
	Assert.notNull(beanName, "Bean name must not be null");
	// 加锁
	synchronized (this.singletonObjects) {
		Object singletonObject = this.singletonObjects.get(beanName);
		// 缓存中不存在当前 bean，也就是当前 bean 第一次创建
		if (singletonObject == null) {
			// 如果当前正在销毁 singletons，抛出异常
			if (this.singletonsCurrentlyInDestruction) {
				throw new BeanCreationNotAllowedException(beanName,
					"Singleton bean creation not allowed while singletons of this factory are in destruction " +
						"(Do not request a bean from a BeanFactory in a destroy method implementation!)");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
			}
			// 创建单例 bean 之前的回调
			beforeSingletonCreation(beanName);
			boolean newSingleton = false;
			boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
			if (recordSuppressedExceptions) {
				this.suppressedExceptions = new LinkedHashSet<>();
			}
			try {
				// 获取 bean 实例，在此处才会去真正调用创建 bean 的方法，也就是 createBean 方法
				singletonObject = singletonFactory.getObject();
				newSingleton = true;
			}
			catch (IllegalStateException ex) {
				// Has the singleton object implicitly appeared in the meantime ->
				// if yes, proceed with it since the exception indicates that state.
				singletonObject = this.singletonObjects.get(beanName);
				if (singletonObject == null) {
					throw ex;
				}
			}
			catch (BeanCreationException ex) {
				if (recordSuppressedExceptions) {
					for (Exception suppressedException : this.suppressedExceptions) {
						ex.addRelatedCause(suppressedException);
					}
				}
				throw ex;
			}
			finally {
				if (recordSuppressedExceptions) {
					this.suppressedExceptions = null;
				}
				// 创建单例 bean 之后的回调
				afterSingletonCreation(beanName);
			}
			if (newSingleton) {
				// 将 singletonObject 放入缓存
				addSingleton(beanName, singletonObject);
			}
		}
		// 返回 bean 实例
		return singletonObject;
	}
}

// 单例 bean 创建前的回调方法，默认实现是将 beanName 加入到当前正在创建 bean 的缓存中
protected void beforeSingletonCreation(String beanName) {
	if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.add(beanName)) {
		throw new BeanCurrentlyInCreationException(beanName);
	}
}

// 单例 bean 创建后的回调方法，默认实现是将 beanName 从当前正在创建 bean 的缓存中移除
protected void afterSingletonCreation(String beanName) {
	if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.remove(beanName)) {
		throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
	}
}

protected void addSingleton(String beanName, Object singletonObject) {
	synchronized (this.singletonObjects) {
		// 将 bean 实例缓存起来
		this.singletonObjects.put(beanName, singletonObject);
		// 移除 bean 的工厂
		this.singletonFactories.remove(beanName);
		// bean 已经实际创建完毕，这里从早起单例缓存中删除
		this.earlySingletonObjects.remove(beanName);
		// 将 beanName 添加到已注册 bean 缓存中
		this.registeredSingletons.add(beanName);
	}
}
```

上面方法是单例 `bean` 的处理逻辑，主要做的就是创建 `bean` 实例，然后将实例放入到缓存中；然后下次再获取该 `bean` 是直接从缓存中获取返回。

在创建 `bean` 实例的前后提供了两个扩展点，分别是 `beforeSingletonCreation()` 和 `afterSingletonCreation()` ，我们可以继承 `DefaultSingletonBeanRegistry` 来扩展这两个方法。

## 自定义作用域示例

我们实现一个 `ThreadLocal` 级别的作用域，也就是同一个线程内 `bean` 是同一个实例，不同线程的 `bean` 是不同实例。首先我们继承 `Scope` 接口实现，其中方法。如下：

```java
public class ThreadLocalScope implements Scope {

	/** scope 名称，在 XML 中的 scope 属性就配置此名称 */
	public static final String SCOPE_NAME = "thread-local";

	private final NamedThreadLocal<Map<String, Object>> threadLocal = new NamedThreadLocal<>("thread-local-scope");

	/**
	 * 返回实例对象，该方法被 Spring 调用
	 */
	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		Map<String, Object> context = getContext();
		Object object = context.get(name);
		if (object == null) {
			object = objectFactory.getObject();
			context.put(name, object);
		}
		return object;
	}

	/**
	 * 获取上下文 map
	 */
	@NonNull
	private Map<String, Object> getContext() {
		Map<String, Object> map = threadLocal.get();
		if (map == null) {
			map = new HashMap<>();
			threadLocal.set(map);
		}
		return map;
	}

	@Override
	public Object remove(String name) {
		return getContext().remove(name);
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		// TODO
	}
	
	@Override
	public Object resolveContextualObject(String key) {
		Map<String, Object> context = getContext();
		return context.get(key);
	}

	@Override
	public String getConversationId() {
		return String.valueOf(Thread.currentThread().getId());
	}

}
```

上面的 `ThreadLocalScope` 重点关注下 `get()` 即可，该方法是被 Spring 调用的。

然后在 XML 中配置 `bean` 的 `scope` 为 `thread-local`。如下：

```XML
<bean id="user" name="user" class="com.leisurexi.ioc.domain.User" scope="thread-local">
	<property name="id" value="1"/>
	<property name="name" value="leisurexi"/>
</bean>
```

接着我们测试一下。测试类：

```java
@Test
public void test() throws InterruptedException {
	DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
	// 注册自定义作用域
	beanFactory.registerScope(ThreadLocalScope.SCOPE_NAME, new ThreadLocalScope());
	XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
	reader.loadBeanDefinitions("META-INF/spring-bean.xml");
	for (int i = 0; i < 3; i++) {
		Thread thread = new Thread(() -> {
			User user = beanFactory.getBean("user", User.class);
			System.err.printf("[Thread id :%d] user = %s%n", Thread.currentThread().getId(), user.getClass().getName() + "@" + Integer.toHexString(user.hashCode()));
			User user1 = beanFactory.getBean("user", User.class);
			System.err.printf("[Thread id :%d] user1 = %s%n", Thread.currentThread().getId(), user1.getClass().getName() + "@" + Integer.toHexString(user1.hashCode()));
		});
		thread.start();
		thread.join();
	}
}
```

说一下我们这里的主要思路，新建了三个线程，查询线程内 `user bean`  是否相等，不同线程是否不等。

结果如下图：

![](http://ww1.sinaimg.cn/large/006Vpl27gy1ge0ng9ixchj30ke04y74v.jpg)

# 总结

本文主要介绍了通过 `getBean()` 流程，我们可以重新梳理一下思路：

1. 获取 `bean` 实际名称，如果缓存中存在直接取出实际 `bean` 返回。
2. 缓存中不存在，判断当前工厂是否有 `BeanDefinition` ，没有递归去父工厂创建 `bean`。
3. 合并 `BeanDefinition` ，如果 `depends-on` 不为空，先去初始化依赖的 `bean`。
4. 如果 `bean` 的作用域是单例，调用 `createBean()` 创建实例，这个方法会执行 `bean` 的其它生命周期回调，以及属性赋值等操作；接着执行单例 `bean` 创建前后的生命周期回调方法，并放入 `singletonObjects` 缓存起来。
5. 如果 `bean` 的作用域是原型，调用 `createBean()` 创建实例，并执行原型 `bean` 前后调用生命周期回调方法。
6. 如果 `bean` 的作用域是自定义的，获取对应的 `Scope` 对象，调用重写的 `get()` 获取实例，并执行原型 `bean` 前后调用生命周期回调方法。
7. 最后检查所需的类型是否与实际 `bean` 实例的类型匹配，如果不等进行转换，最后返回实例。

**最后，我模仿 Spring 写了一个精简版，代码会持续更新，现在是 `0.0.1` 版本。地址：[https://github.com/leisurexi/tiny-spring](https://github.com/leisurexi/tiny-spring)。访问新博客地址，观看效果更佳 [https://leisurexi.github.io/](https://leisurexi.github.io/)**

# 参考

* 《Spring 源码深度解析》—— 郝佳
