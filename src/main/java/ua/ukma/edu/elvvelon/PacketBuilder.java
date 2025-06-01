package ua.ukma.edu.elvvelon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PacketBuilder {
    public static final byte MAGIC_BYTE = 0x13;

    private final byte bSrc;
    private final long bPktId;
    private final Message message;

    public PacketBuilder(byte clientSource, long packetId, Message message) {
        this.bSrc = clientSource;
        this.bPktId = packetId;
        this.message = message;
    }

    public byte[] build() throws Exception {
        // 1. Create inner message bytes (cType + bUserId + payload)
        byte[] payloadBytes = message.getPayload().getBytes(StandardCharsets.UTF_8);
        ByteBuffer messageBuffer = ByteBuffer.allocate(4 + 4 + payloadBytes.length);
        messageBuffer.order(ByteOrder.BIG_ENDIAN);
        messageBuffer.putInt(message.getCommandType());
        messageBuffer.putInt(message.getUserId());
        messageBuffer.put(payloadBytes);
        byte[] unencryptedMessage = messageBuffer.array();

        // 2. Encrypt the inner message
        byte[] encryptedMessage = CryptoUtils.encrypt(unencryptedMessage);
        int wLen = encryptedMessage.length;

        // 3. Allocate buffer for the entire packet
        // Magic(1) + Src(1) + PktId(8) + Len(4) + Crc16(2) + Message(wLen) + Crc16(2)
        int totalLength = 1 + 1 + 8 + 4 + 2 + wLen + 2;
        ByteBuffer packetBuffer = ByteBuffer.allocate(totalLength);
        packetBuffer.order(ByteOrder.BIG_ENDIAN);

        // 4. Fill header
        packetBuffer.put(MAGIC_BYTE);
        packetBuffer.put(bSrc);
        packetBuffer.putLong(bPktId);
        packetBuffer.putInt(wLen);

        // 5. Calculate and put header CRC16 (for first 14 bytes)
        int headerCrc = CRC16.calculate(packetBuffer.array(), 0, 14);
        packetBuffer.putShort((short) headerCrc);

        // 6. Put encrypted message
        packetBuffer.put(encryptedMessage);

        // 7. Calculate and put message CRC16
        int messageCrc = CRC16.calculate(encryptedMessage);
        packetBuffer.putShort((short) messageCrc);

        return packetBuffer.array();
    }
}