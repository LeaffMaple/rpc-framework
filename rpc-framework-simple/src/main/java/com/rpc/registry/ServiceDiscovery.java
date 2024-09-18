package com.rpc.registry;


import com.rpc.extension.SPI;
import com.rpc.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 服务发现
 *
 */
@SPI
public interface ServiceDiscovery {
    /**
     * 根据 rpcServiceName 获取远程服务地
     *
     * @param rpcRequest rpc service pojo
     * @return service address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
