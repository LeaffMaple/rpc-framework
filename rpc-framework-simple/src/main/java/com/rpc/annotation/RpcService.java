package com.rpc.annotation;


import java.lang.annotation.*;

/**
 * RPC 服务注解，标记在服务实现类上。
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {

    /**
     * 服务版本，默认为空
     */
    String version() default "";

    /**
     * 服务组，默认为空
     */
    String group() default "";

}
