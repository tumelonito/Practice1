package ua.ukma.edu.elvvelon;

import java.util.Objects;

public class Packet {
    private final byte magicByte;
    private final byte clientSource;
    private final long packetId;
    private final Message message;

    public Packet(byte magicByte, byte clientSource, long packetId, Message message) {
        this.magicByte = magicByte;
        this.clientSource = clientSource;
        this.packetId = packetId;
        this.message = message;
    }

    // Getters
    public byte getMagicByte() { return magicByte; }
    public byte getClientSource() { return clientSource; }
    public long getPacketId() { return packetId; }
    public Message getMessage() { return message; }

    @Override
    public String toString() {
        return "Packet{" +
                "magicByte=" + String.format("0x%02X", magicByte) +
                ", clientSource=" + clientSource +
                ", packetId=" + packetId +
                ", message=" + message +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Packet packet = (Packet) o;
        return magicByte == packet.magicByte &&
                clientSource == packet.clientSource &&
                packetId == packet.packetId &&
                Objects.equals(message, packet.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(magicByte, clientSource, packetId, message);
    }
}