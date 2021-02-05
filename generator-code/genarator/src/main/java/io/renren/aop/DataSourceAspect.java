package io.renren.aop;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import io.renren.dynamic.DynamicDto;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author: lipj
 * @Description:
 * @Date: 2021/2/5 10:28
 */

@Aspect
@Component
public class DataSourceAspect {

    @Before(value = "@annotation(ds)")
    public void doAfter(JoinPoint joinPoint,DS ds) throws Exception {
        //获取方法的参数名和参数值
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        List<Class> classList = Arrays.asList(methodSignature.getParameterTypes());
        List<Object> paramList = Arrays.asList(joinPoint.getArgs());
        //将方法的参数类型和参数值一一对应的放入上下文中
        for (int i = 0; i < classList.size(); i++) {
            if(classList.get(i) == DynamicDto.class){
                // 修改 value 属性值
                if(paramList.get(i) != null && StringUtils.isNotBlank(((DynamicDto) paramList.get(i)).getKey())){
                    DynamicDataSourceContextHolder.push(((DynamicDto) paramList.get(i)).getKey());
                }
            }
        }
    }
}
