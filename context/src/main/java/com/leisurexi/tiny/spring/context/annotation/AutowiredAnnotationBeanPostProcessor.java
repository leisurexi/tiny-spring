package com.leisurexi.tiny.spring.context.annotation;

import cn.hutool.core.util.ReflectUtil;
import com.leisurexi.tiny.spring.beans.PropertyValues;
import com.leisurexi.tiny.spring.beans.factory.AbstractAutowireCapableBeanFactory;
import com.leisurexi.tiny.spring.beans.factory.BeanFactoryAware;
import com.leisurexi.tiny.spring.beans.factory.config.DependencyDescriptor;
import com.leisurexi.tiny.spring.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link Autowired} 注解的后置处理器
 *
 * @author: leisurexi
 * @date: 2020-06-01 21:41
 * @since 0.0.4
 */
public class AutowiredAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

    private AbstractAutowireCapableBeanFactory beanFactory;

    /**
     * 重写 bean 的属性后置属性，在此处判断哪些属性标注了 {@link Autowired} 注解，
     * 并未这些属性找到合适类型的 bean 进行赋值
     *
     * @param pvs      解析好的属性值
     * @param bean     bean 的实例
     * @param beanName bean 的名称
     * @return bean 自动注入后的属性
     */
    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
        // 获取所有需要依赖注入的字段
        List<Field> autowiringFields = findAutowiringFields(bean.getClass());
        for (Field field : autowiringFields) {
            DependencyDescriptor desc = new DependencyDescriptor(bean.getClass(), field.getType(), field.getName());
            Object value = beanFactory.resolveDependency(desc, beanName);
            ReflectUtil.setFieldValue(bean, field, value);
        }
        return pvs;
    }

    /**
     * 找出需要依赖注入的字段
     *
     * @param clazz 当前初始化的 bean 的类型
     * @return 需要注入的字段集合
     */
    private List<Field> findAutowiringFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        List<Field> autowiringFields = new ArrayList<>(fields.length);
        // 遍历该类所有字段，找到标注了 @Autowired 注解的字段并添加进 fields 中
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                autowiringFields.add(field);
            }
        }
        return autowiringFields;
    }

    @Override
    public void setBeanFactory(AbstractAutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
