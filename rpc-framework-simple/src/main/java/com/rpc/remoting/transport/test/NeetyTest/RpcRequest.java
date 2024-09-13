package com.rpc.remoting.transport.test.NeetyTest;

import lombok.*;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
@ToString
public class RpcRequest {
    private String interfaceName;
    private String methodName;
}
