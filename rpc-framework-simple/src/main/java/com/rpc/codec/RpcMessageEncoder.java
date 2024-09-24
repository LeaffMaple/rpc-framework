package com.rpc.codec;


import com.rpc.Serializer.Serializer;
import com.rpc.compress.Compress;
import com.rpc.constants.RpcConstants;
import com.rpc.enums.CompressTypeEnum;
import com.rpc.enums.SerializationTypeEnum;
import com.rpc.extension.ExtensionLoader;
import com.rpc.remoting.dto.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;




/**
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 *
 */

@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) {
        try {
            //写入魔数
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            //写入版本号
            out.writeByte(RpcConstants.VERSION);
            // 为总长度预留4个字节的空间（稍后会填充）
            out.writerIndex(out.writerIndex() + 4);
            //写入消息类型
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            //写入编码类型
            out.writeByte(rpcMessage.getCodec());
            //写入压缩类型
            out.writeByte(CompressTypeEnum.GZIP.getCode());
            //写入消息的唯一标识
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            // 计算消息的总长度（头部长度 + 消息体长度）
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;//除去消息体外，一共有16个字节(可以看自定义结构)
            // 如果消息不是心跳消息，处理消息体的序列化和压缩
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                // 获取序列化器，根据消息中的编码类型进行序列化
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("codec name: [{}] ", codecName);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                // 获取压缩算法，根据压缩类型对序列化后的字节进行压缩
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            int writeIndex = out.writerIndex();
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }

    }
}

