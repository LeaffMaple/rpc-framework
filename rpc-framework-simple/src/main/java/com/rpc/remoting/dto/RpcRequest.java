package com.rpc.remoting.dto;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1905122041950251207L;
    /**
     * 请求ID
     */
    private String requestId;
    /**
     * 接口名
     */
    private String interfaceName;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数
     */
    private Object[] parameters;
    /**
     * 参数类型
     */
    private Class<?>[] paramTypes;
    /**
     * 版本号
     */
    private String version;
    /**
     * 组
     */
    private String group;

    /**
     * 获取服务名
     * @return
     */
    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}

