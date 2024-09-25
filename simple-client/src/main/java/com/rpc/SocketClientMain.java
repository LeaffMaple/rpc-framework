package com.rpc;

import com.rpc.config.RpcServiceConfig;
import com.rpc.proxy.RpcClientProxy;
import com.rpc.remoting.transport.RpcRequestTransport;
import com.rpc.remoting.transport.socket.SocketRpcClient;

public class SocketClientMain {
    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceConfig);
        TestService helloService = rpcClientProxy.getProxy(TestService.class);
        String hello = helloService.test(new Test("111", "222"));
        System.out.println(hello);
    }
}
