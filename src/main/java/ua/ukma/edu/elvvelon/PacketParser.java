package ua.ukma.edu.elvvelon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PacketParser {

    public Packet parse(byte[] data) throws Exception {
        if (data.length < 18) { // Minimum length: 14 (header) + 2 (crc) + 2 (crc)
            throw new IllegalArgumentException("Packet is too short.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);

        // 1. Read and validate Magic Byte
        byte bMagic = buffer.get();
        if (bMagic != PacketBuilder.MAGIC_BYTE) {
            throw new IllegalArgumentException("Invalid magic byte: " + String.format("0x%02X", bMagic));
        }

        // 2. Read header fields
        byte bSrc = buffer.get();
        long bPktId = buffer.getLong();
        int wLen = buffer.getInt();

        // 3. Validate header CRC16
        short headerCrcFromPacket = buffer.getShort();
        int calculatedHeaderCrc = CRC16.calculate(data, 0, 14);
        if (headerCrcFromPacket != (short) calculatedHeaderCrc) {
            throw new IllegalArgumentException("Header CRC16 check failed.");
        }

        // Check if packet has enough data for message and its CRC
        if (data.length != 16 + wLen + 2) {
            throw new IllegalArgumentException("Packet length mismatch.");
        }

        // 4. Read encrypted message and validate its CRC16
        byte[] encryptedMessage = new byte[wLen];
        buffer.get(encryptedMessage);

        short messageCrcFromPacket = buffer.getShort();
        int calculatedMessageCrc = CRC16.calculate(encryptedMessage);
        if (messageCrcFromPacket != (short) calculatedMessageCrc) {
            throw new IllegalArgumentException("Message CRC16 check failed.");
        }

        // 5. Decrypt message
        byte[] decryptedMessageBytes = CryptoUtils.decrypt(encryptedMessage);
        ByteBuffer messageBuffer = ByteBuffer.wrap(decryptedMessageBytes);
        messageBuffer.order(ByteOrder.BIG_ENDIAN);

        // 6. Parse inner message
        int cType = messageBuffer.getInt();
        int bUserId = messageBuffer.getInt();
        byte[] payloadBytes = new byte[messageBuffer.remaining()];
        messageBuffer.get(payloadBytes);
        String payload = new String(payloadBytes, StandardCharsets.UTF_8);

        // 7. Create POJO objects
        Message message = new Message(cType, bUserId, payload);
        return new Packet(bMagic, bSrc, bPktId, message);
    }
}