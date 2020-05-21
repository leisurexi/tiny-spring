package com.leisurexi.tiny.spring.beans.factory;

import cn.hutool.core.bean.BeanException;
import cn.hutool.core.util.ReflectUtil;
import com.google.common.base.Strings;
import com.leisurexi.tiny.spring.beans.PropertyValue;
import com.leisurexi.tiny.spring.beans.PropertyValues;
import com.leisurexi.tiny.spring.beans.exception.BeansException;
import com.leisurexi.tiny.spring.beans.factory.config.BeanPostProcessor;
import com.leisurexi.tiny.spring.beans.factory.config.DependencyDescriptor;
import com.leisurexi.tiny.spring.beans.factory.config.InstantiationAwareBeanPostProcessor;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition;
import com.leisurexi.tiny.spring.beans.factory.support.BeanDefinitionValueResolver;
import com.leisurexi.tiny.spring.beans.factory.support.ConstructorResolver;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

import static com.leisurexi.tiny.spring.beans.factory.support.BeanDefinition.*;


/**
 * 可自动装配的 BeanFactory
 *
 * @author: leisurexi
 * @date: 2020-04-04 2:42 下午
 * @since 0.0.1
 */
@Slf4j
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory {

    @Override
    protected Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException {
        Object bean = resolveBeforeInstantiation(beanName, beanDefinition);
        if (bean != null) {
            return bean;
        }
        return doCreateBean(beanName, beanDefinition);
    }

    /**
     * 为指定的 bean 创建实例
     *
     * @param beanName       bean 的名称
     * @param beanDefinition bean 的定义元数据
     * @return bean 的实例
     * @since 0.0.3
     */
    protected Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
        // 创建 bean 的实例阶段
        Object bean = createBeanInstance(beanName, beanDefinition);
        // 是否要提前曝光 bean
        boolean earlySingletonExposure = beanDefinition.isSingleton();
        if (earlySingletonExposure) {
            addSingletonFactory(beanName, () -> bean);
        }
        // 属性填充阶段
        populateBean(beanName, bean, beanDefinition);
        Object exposedObject = bean;
        // 初始化 bean 阶段
        initializeBean(beanName, exposedObject, beanDefinition);
        return bean;
    }

    /**
     * 初始化 bean
     *
     * @param beanName       bean 的名称
     * @param exposedObject  新的 bean 实例
     * @param beanDefinition bean 的定义元信息
     */
    protected Object initializeBean(String beanName, Object exposedObject, BeanDefinition beanDefinition) {
        Object wrapperBean = exposedObject;
        // 执行 bean 初始化前回调方法
        wrapperBean = applyBeanPostProcessorsBeforeInitialization(exposedObject, beanName);
        // 调用 bean 的初始化方法，如实现 InitializingBean 接口，XML 中的 init-method 属性指定的方法
        invokeInitMethods(beanName, wrapperBean, beanDefinition);
        // 执行 bean 初始化后回调方法
        wrapperBean = applyBeanPostProcessorsAfterInitialization(exposedObject, beanName);
        return wrapperBean;
    }

    /**
     * 调用 bean 的初始化方法：
     * 1.如果 bean 实现了 InitializingBean 接口，先调用其重写的 afterPropertiesSet()
     * 2.如果在XML中定义了 init-method 属性，调用其指定的方法，不能是静态方法
     *
     * @param beanName       bean 的名称
     * @param bean           bean 的实例
     * @param beanDefinition bean 的定义元信息
     * @since 0.0.3
     */
    protected void invokeInitMethods(String beanName, Object bean, BeanDefinition beanDefinition) {
        // bean 是否实现了 InitializingBean 接口
        boolean isInitializingBean = (bean instanceof InitializingBean);
        if (isInitializingBean) {
            // 进行强转并调用 afterPropertiesSet()
            ((InitializingBean) bean).afterPropertiesSet();
        }
        // bean 的初始化方法不为空
        if (!Strings.isNullOrEmpty(beanDefinition.getInitMethodName())) {
            invokeCustomInitMethod(beanName, bean, beanDefinition);
        }
    }

    /**
     * 调用自定义初始化方法
     *
     * @param beanName       bean 的名称
     * @param bean           bean 的实例
     * @param beanDefinition bean 的定义元信息
     */
    protected void invokeCustomInitMethod(String beanName, Object bean, BeanDefinition beanDefinition) {
        String initMethodName = beanDefinition.getInitMethodName();
        Class<?> clazz = bean.getClass();
        Method method = null;
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals(initMethodName)) {
                method = m;
            }
        }
        if (method == null) {
            throw new BeanException("Could not find an init method named '" + initMethodName + "' with bean name '" + beanName + "'");
        }
        method.setAccessible(true);
        try {
            method.invoke(bean, null);
        } catch (Exception e) {
            throw new BeanException(e);
        }
    }

    /**
     * 创建 bean 的实例
     *
     * @param beanName       bean 的名称
     * @param beanDefinition bean 的定义元数据
     * @return bean 的实例
     * @throws BeansException
     * @since 0.0.3
     */
    protected Object createBeanInstance(String beanName, BeanDefinition beanDefinition) throws BeansException {
        if (beanDefinition.hasConstructorArgumentValues() || beanDefinition.getAutowireMode() == AUTOWIRE_CONSTRUCTOR) {
            return autowireConstructor(beanName, beanDefinition);
        }
        return instantiateBean(beanDefinition);
    }

    /**
     * 使用默认构造函数实例化 bean
     *
     * @param beanDefinition bean 定义元信息
     * @return bean 实例
     * @since 0.0.3
     */
    private Object instantiateBean(BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getBeanClass();
        if (clazz.isInterface()) {
            throw new BeansException("Specified class is an interface");
        }
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new BeansException("No default constructor found");
        }
    }

    /**
     * 找个匹配的构造函数进行实例化
     *
     * @param beanName       bean 的名称
     * @param beanDefinition bean 的定义元信息
     * @return bean 的实例
     * @since 0.0.3
     */
    private Object autowireConstructor(String beanName, BeanDefinition beanDefinition) {
        return new ConstructorResolver(this).autowireConstructor(beanName, beanDefinition);
    }

    /**
     * 填充 bean
     *
     * @param beanName       bean 的名称
     * @param bean           bean 的实例
     * @param beanDefinition bean 的定义元信息
     */
    protected void populateBean(String beanName, Object bean, BeanDefinition beanDefinition) {
        if (beanDefinition.getPropertyValues() == null) {
            return;
        }

        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                InstantiationAwareBeanPostProcessor bp = (InstantiationAwareBeanPostProcessor) beanPostProcessor;
                // 如果 bean 的实例化后回调方法返回 false，直接跳过下面的属性赋值阶段
                if (!bp.postProcessAfterInstantiation(bean, beanName)) {
                    return;
                }
            }
        }

        PropertyValues propertyValues = beanDefinition.getPropertyValues();
        int autowireMode = beanDefinition.getAutowireMode();
        // 按照名字自动注入
        if (autowireMode == AUTOWIRE_BY_NAME) {
            autowireByName(beanName, bean, propertyValues);
        }
        // 按照类型自动注入
        if (autowireMode == AUTOWIRE_BY_TYPE) {
            autowireByType(beanName, bean, propertyValues);
        }

        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                InstantiationAwareBeanPostProcessor bp = (InstantiationAwareBeanPostProcessor) beanPostProcessor;
                // 如果 bean 的属性后置处理方法返回非空，则直接使用返回的值，否则直接跳过下面的属性赋值阶段
                PropertyValues pvsToUse = bp.postProcessProperties(propertyValues, bean, beanName);
                if (propertyValues == null) {
                    return;
                }
                propertyValues = pvsToUse;
            }
        }

        // 真正进行 bean 属性赋值的方法
        applyPropertyValues(bean, propertyValues);
    }

    /**
     * 根据类型自动注入属性，首先找类型匹配的 bean 如果有多个再匹配名称
     *
     * @sine 0.0.3
     */
    private void autowireByType(String beanName, Object bean, PropertyValues propertyValues) {
        // 获取需要依赖注入的属性名称
        Set<String> propertyNames = unsatisfiedNonSimpleProperties(propertyValues, bean);
        for (String propertyName : propertyNames) {
            try {
                Field field = bean.getClass().getDeclaredField(propertyName);
                // 根据类型注入永远不要注入 Object 类型，你细细地品一下
                if (Object.class != field.getType()) {
                    // bean 的定义存在，就调用 getBean() 去获取实例，否则 bean 没有定义抛出异常
                    if (getBeanDefinition(propertyName) != null) {
                        DependencyDescriptor descriptor = new DependencyDescriptor(bean.getClass(), field.getType(), propertyName);
                        propertyValues.addPropertyValues(new PropertyValue(propertyName, resolveDependency(descriptor, beanName)));
                    } else {
                        log.error("Not autowiring property '{}' of bean '{}' by type: no matching bean found", propertyName, beanName);
                    }
                }
            } catch (Exception e) {
                throw new BeanException(e);
            }
        }
    }

    /**
     * 根据名称自动注入属性，找不到对应名称的就不填充
     *
     * @sine 0.0.3
     */
    private void autowireByName(String beanName, Object bean, PropertyValues propertyValues) {
        // 获取需要依赖注入的属性名称
        Set<String> propertyNames = unsatisfiedNonSimpleProperties(propertyValues, bean);
        for (String propertyName : propertyNames) {
            // bean 的定义存在，就调用 getBean() 去获取实例，否则 bean 没有定义抛出异常
            if (getBeanDefinition(propertyName) != null) {
                propertyValues.addPropertyValues(new PropertyValue(propertyName, getBean(propertyName)));
            } else {
                log.error("Not autowiring property '{}' of bean '{}' by type: no matching bean found", propertyName, beanName);
            }
        }
    }

    /**
     * 找出属性中需要依赖注入的属性
     *
     * @param propertyValues bean 的属性
     * @return 属性名称
     * @sine 0.0.3
     */
    private Set<String> unsatisfiedNonSimpleProperties(PropertyValues propertyValues, Object bean) {
        Class<?> clazz = bean.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Set<String> set = new TreeSet<>();
        for (Field field : fields) {
            if (!propertyValues.contains(field.getName())) {
                set.add(field.getName());
            }
        }
        return set;
    }

    /**
     * 给 bean 的属性赋值
     *
     * @param bean           目标 bean
     * @param propertyValues 多个属性值
     * @throws BeansException
     */
    protected void applyPropertyValues(Object bean, PropertyValues propertyValues) throws BeansException {
        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this);
        propertyValues.forEach(propertyValue -> ReflectUtil.setFieldValue(bean, propertyValue.getName(), valueResolver.resolveValueIfNecessary(propertyValue.getName(), propertyValue.getValue())));
    }

    /**
     * 实例化前应用后置处理器，以解决是否存在指定 bean 的快捷创建方式
     *
     * @param beanName       bean 的名称
     * @param beanDefinition bean 的定义元信息
     * @return 快捷方式创建的 bean 实例，或者为空
     */
    protected Object resolveBeforeInstantiation(String beanName, BeanDefinition beanDefinition) {
        // 执行 bean 的实例化前回调
        Object bean = applyBeanPostProcessorBeforeInstantiation(beanDefinition.getBeanClass(), beanName);
        // 如果返回的不为空，那么直接执行 bean 的初始化后回调，因为只能在这里执行了
        if (bean != null) {
            bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
        }
        return bean;
    }

    /**
     * 调用 bean 实例化之前的回调方法
     *
     * @param beanClass bean 的类型
     * @param beanName  bean 的名称
     * @return bean 的实例，可能是代理后的 bean
     * @sine 0.0.3
     */
    protected Object applyBeanPostProcessorBeforeInstantiation(Class<?> beanClass, String beanName) {
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
                InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) beanPostProcessor;
                Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
                log.debug("[{}] 执行实例化前回调，返回: [{}]", beanName, result);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * bean 初始化前回调
     *
     * @param existingBean bean 的实例
     * @param beanName     bean 的名称
     * @return bean 的实例，可能是代理后的 bean
     * @since 0.0.3
     */
    protected Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) {
        Object result = existingBean;
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            Object current = beanPostProcessor.postProcessAfterInitialization(result, beanName);
            log.debug("[{}] 执行初始化前回调，返回: [{}]", beanName, current);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    /**
     * bean 的初始化后回调
     *
     * @param existingBean bean 的实例
     * @param beanName     bean 的名称
     * @return bean 的实例，可能是代理后的 bean
     * @sine 0.0.3
     */
    protected Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) {
        Object result = existingBean;
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            Object current = beanPostProcessor.postProcessAfterInitialization(result, beanName);
            log.debug("[{}] 执行初始化后回调，返回: [{}]", beanName, current);
            if (current == null) {
                return result;
            }
            result = current;
        }
        return result;
    }

    /**
     * 解决指定 bean 的依赖关系
     *
     * @param descriptor         依赖描述符
     * @param requestingBeanName 需要解决依赖的 bean 名称
     * @return 符合条件的 bean，或者 {@code null} 如果没找到
     * @since 0.0.3
     */
    public abstract Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName);

}
