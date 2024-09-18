package com.rpc.config;

import lombok.*;

/**
 * RpcServiceConfig 类，用于配置 RPC 通信中的服务相关信息
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString

public class RpcServiceConfig {
    /**
     * 服务版本
     */
    private String version = "";

    /**
     * 组信息，用于区分接口的多个实现类
     */
    private String group = "";

    /**
     * 目标服务对象
     */
    private Object service;

    /**
     * 获取服务的唯一名称，包括服务名称、组和版本
     *
     * @return 服务的唯一名称
     */
    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    /**
     * 获取服务接口的全名
     *
     * @return 服务接口的全名
     */
    public String getServiceName() {
        // 假设服务对象至少实现了某个接口，返回第一个接口的全名
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
