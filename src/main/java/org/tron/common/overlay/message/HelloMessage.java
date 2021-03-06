package org.tron.common.overlay.message;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.beans.factory.annotation.Autowired;
import org.tron.common.overlay.discover.Node;
import org.tron.common.utils.ByteArray;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.config.args.Args;
import org.tron.core.db.Manager;
import org.tron.core.net.message.MessageTypes;
import org.tron.protos.Discover.Endpoint;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.HelloMessage.Builder;

public class HelloMessage extends P2pMessage {

  Protocol.HelloMessage helloMessage;

  public HelloMessage(byte[] rawData) {
    super(rawData);
    this.type = MessageTypes.P2P_HELLO.asByte();
    unPack();
  }

  public HelloMessage(byte type, byte[] rawData) {
    super(type, rawData);
    try {
      this.helloMessage = Protocol.HelloMessage.parseFrom(rawData);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
    }
    unPack();
  }

  /**
   * Create hello message.
   */
  public HelloMessage(Node from, long timestamp, BlockCapsule.BlockId genesisBlockId,
                      BlockCapsule.BlockId solidBlockId, BlockCapsule.BlockId headBlockId){

    Endpoint fromEndpoint = Endpoint.newBuilder()
        .setNodeId(ByteString.copyFrom(from.getId()))
        .setPort(from.getPort())
        .setAddress(ByteString.copyFrom(ByteArray.fromString(from.getHost())))
        .build();

    Protocol.HelloMessage.BlockId gBlockId = Protocol.HelloMessage.BlockId.newBuilder()
            .setHash(genesisBlockId.getByteString())
            .setNumber(genesisBlockId.getNum())
            .build();

    Protocol.HelloMessage.BlockId sBlockId = Protocol.HelloMessage.BlockId.newBuilder()
            .setHash(solidBlockId.getByteString())
            .setNumber(solidBlockId.getNum())
            .build();

    Protocol.HelloMessage.BlockId hBlockId = Protocol.HelloMessage.BlockId.newBuilder()
            .setHash(headBlockId.getByteString())
            .setNumber(headBlockId.getNum())
            .build();

    Builder builder = Protocol.HelloMessage.newBuilder();

    builder.setFrom(fromEndpoint);
    builder.setVersion(Args.getInstance().getNodeP2pVersion());
    builder.setTimestamp(timestamp);
    builder.setGenesisBlockId(gBlockId);
    builder.setSolidBlockId(sBlockId);
    builder.setHeadBlockId(hBlockId);

    this.helloMessage = builder.build();
    this.type = MessageTypes.P2P_HELLO.asByte();
    this.data = this.helloMessage.toByteArray();
  }

  public void unPack() {
    try {
      this.helloMessage = Protocol.HelloMessage.parseFrom(this.data);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
    }
  }

  @Override
  public byte[] getData() {
    return this.data;
  }

  /**
   * Get the version of p2p protocol.
   */
  public int getVersion() {
    return this.helloMessage.getVersion();
  }

  public long getTimestamp(){
    return this.helloMessage.getTimestamp();
  }

  public Node getFrom() {
    Endpoint from = this.helloMessage.getFrom();
    return new Node(from.getNodeId().toByteArray(),
            ByteArray.toStr(from.getAddress().toByteArray()), from.getPort());
  }

  public BlockCapsule.BlockId getGenesisBlockId(){
    return new BlockCapsule.BlockId(this.helloMessage.getGenesisBlockId().getHash(),
            this.helloMessage.getGenesisBlockId().getNumber());
  }

  public BlockCapsule.BlockId getSolidBlockId(){
    return new BlockCapsule.BlockId(this.helloMessage.getSolidBlockId().getHash(),
            this.helloMessage.getSolidBlockId().getNumber());
  }

  public BlockCapsule.BlockId getHeadBlockId(){
    return new BlockCapsule.BlockId(this.helloMessage.getHeadBlockId().getHash(),
            this.helloMessage.getHeadBlockId().getNumber());
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

  @Override
  public MessageTypes getType() {
    return MessageTypes.fromByte(this.type);
  }

  @Override
  public String toString() {
    return helloMessage.toString();
  }

}