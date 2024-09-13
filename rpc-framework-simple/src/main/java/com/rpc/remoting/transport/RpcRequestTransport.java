package com.rpc.remoting.transport;


import com.rpc.extension.SPI;
import com.rpc.remoting.dto.RpcRequest;

/**
 * 发送rpc请求
 *
 */
@SPI
public interface RpcRequestTransport {
    /**
     * 发送RPC（远程过程调用）请求并获取结果
     *
     * @param rpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
