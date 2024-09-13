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
     * 查找服务通过服务名
     *
     * @param rpcRequest rpc service pojo
     * @return service address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
