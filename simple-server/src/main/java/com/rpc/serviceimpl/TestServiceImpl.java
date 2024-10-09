package com.rpc.serviceimpl;

import com.rpc.Test;
import com.rpc.TestService;
import com.rpc.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RpcService(group = "test1", version = "version1")
public class TestServiceImpl implements TestService {

    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String test(Test test) {
        log.info("TestServiceImpl收到: {}.", test.getMessage());
        String result = "description is " + test.getDescription();
        log.info("TestServiceImpl返回: {}.", result);
        return result;
    }
}
