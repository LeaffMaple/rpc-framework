package com.rpc;


import com.rpc.config.RpcServiceConfig;
import com.rpc.remoting.transport.socket.SocketRpcServer;
import com.rpc.serviceimpl.TestServiceImpl;

public class SocketServerMain {
    public static void main(String[] args) {
        TestService testService = new TestServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setService(testService);
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();
    }
}
