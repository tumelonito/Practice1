import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.ukma.edu.elvvelon.*;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ProtocolTest {

    @Test
    @DisplayName("Should correctly build and parse a standard packet")
    void testPacketBuildAndParse_Success() throws Exception {
        String jsonPayload = "{\"command\":\"sendMessage\",\"text\":\"Hello, world!\"}";
        Message originalMessage = new Message(101, 202, jsonPayload);
        byte clientSource = 1;
        long packetId = 123456789L;

        PacketBuilder builder = new PacketBuilder(clientSource, packetId, originalMessage);
        byte[] packetBytes = builder.build();

        PacketParser parser = new PacketParser();
        Packet parsedPacket = parser.parse(packetBytes);

        assertNotNull(parsedPacket);
        assertEquals(PacketBuilder.MAGIC_BYTE, parsedPacket.getMagicByte());
        assertEquals(clientSource, parsedPacket.getClientSource());
        assertEquals(packetId, parsedPacket.getPacketId());
        assertEquals(originalMessage, parsedPacket.getMessage());

        System.out.println("Standard packet test successful!");
        System.out.println("   Original Message: " + originalMessage);
        System.out.println("   Parsed Packet: " + parsedPacket);
    }

    @Test
    @DisplayName("Should correctly handle a packet with an empty payload")
    void testBuildAndParse_EmptyPayload() throws Exception {
        Message originalMessage = new Message(200, 300, "");
        byte clientSource = 2;
        long packetId = 987654321L;

        PacketBuilder builder = new PacketBuilder(clientSource, packetId, originalMessage);
        byte[] packetBytes = builder.build();
        PacketParser parser = new PacketParser();
        Packet parsedPacket = parser.parse(packetBytes);

        assertNotNull(parsedPacket);
        assertEquals(originalMessage, parsedPacket.getMessage());
        assertEquals("", parsedPacket.getMessage().getPayload());

        System.out.println("Empty payload test successful!");
    }

    @Test
    @DisplayName("Should correctly handle a packet with Unicode characters")
    void testBuildAndParse_WithUnicodePayload() throws Exception {
        String jsonPayload = "{\"user\":\"Cyril\",\"status\":\"testing\"}";
        Message originalMessage = new Message(10, 15, jsonPayload);
        byte clientSource = 3;
        long packetId = 1122334455L;

        PacketBuilder builder = new PacketBuilder(clientSource, packetId, originalMessage);
        byte[] packetBytes = builder.build();
        PacketParser parser = new PacketParser();
        Packet parsedPacket = parser.parse(packetBytes);

        assertNotNull(parsedPacket);
        assertEquals(originalMessage, parsedPacket.getMessage());

        System.out.println("Unicode payload test successful!");
    }

    @Test
    @DisplayName("Should throw an error for an invalid 'magic' byte")
    void testParse_InvalidMagicByte() throws Exception {
        Message message = new Message(1, 1, "{}");
        PacketBuilder builder = new PacketBuilder((byte)1, 1L, message);
        byte[] packetBytes = builder.build();

        packetBytes[0] = 0x14;

        PacketParser parser = new PacketParser();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(packetBytes));

        assertTrue(exception.getMessage().contains("Invalid magic byte"));
        System.out.println("Invalid magic byte test successful: " + exception.getMessage());
    }

    @Test
    @DisplayName("Should throw an error if the packet is too short")
    void testParse_PacketTooShort() {
        byte[] shortPacket = new byte[]{0x13, 0x01, 0x02, 0x03};

        PacketParser parser = new PacketParser();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(shortPacket));

        assertEquals("Packet is too short.", exception.getMessage());
        System.out.println("Packet too short test successful: " + exception.getMessage());
    }

    @Test
    @DisplayName("Should throw an error for a packet length mismatch")
    void testParse_PacketLengthMismatch() throws Exception {
        Message message = new Message(1, 1, "{\"data\":\"some data\"}");
        PacketBuilder builder = new PacketBuilder((byte)1, 1L, message);
        byte[] packetBytes = builder.build();

        byte[] truncatedPacket = Arrays.copyOf(packetBytes, packetBytes.length - 5);

        PacketParser parser = new PacketParser();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(truncatedPacket));

        assertEquals("Packet length mismatch.", exception.getMessage());
        System.out.println("Packet length mismatch test successful: " + exception.getMessage());
    }

    @Test
    @DisplayName("Should throw an error due to an invalid header CRC16")
    void testParse_BadHeaderCrc() throws Exception {
        Message message = new Message(1, 1, "{}");
        PacketBuilder builder = new PacketBuilder((byte)1, 1L, message);
        byte[] packetBytes = builder.build();

        packetBytes[2]++;

        PacketParser parser = new PacketParser();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(packetBytes));

        assertEquals("Header CRC16 check failed.", exception.getMessage());
        System.out.println("Bad Header CRC test successful: " + exception.getMessage());
    }

    @Test
    @DisplayName("Should throw an error due to an invalid message CRC16")
    void testParse_BadMessageCrc() throws Exception {
        Message message = new Message(1, 1, "{\"data\":\"test\"}");
        PacketBuilder builder = new PacketBuilder((byte)1, 1L, message);
        byte[] packetBytes = builder.build();

        if (packetBytes.length > 17) {
            packetBytes[17]++;
        }

        PacketParser parser = new PacketParser();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(packetBytes));

        assertEquals("Message CRC16 check failed.", exception.getMessage());
        System.out.println("Bad Message CRC test successful: " + exception.getMessage());
    }
}