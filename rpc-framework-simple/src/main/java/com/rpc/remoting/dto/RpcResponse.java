package com.rpc.remoting.dto;

import com.rpc.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;
    /**
     * 请求ID
     */
    private String requestId;
    /**
     * 响应码
     */
    private Integer code;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        // 设置响应码
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        // 设置响应消息
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        // 设置请求ID
        response.setRequestId(requestId);
        // 如果响应数据不为空，则设置响应数据
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        // 设置响应码
        response.setCode(rpcResponseCodeEnum.getCode());
        // 设置响应消息
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }

}
