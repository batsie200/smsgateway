package zw.co.econet.smsgateway.smpp.cloudhopper;


public enum MessageState {
    ENROUTE((byte) 0x01), DELIVERED((byte) 0x02), EXPIRED((byte) 0x03), DELETED((byte) 0x04), UNDELIVERABLE((byte) 0x05), ACCEPTED((byte) 0x06), UNKNOWN((byte) 0x07), REJECTED((byte) 0x07);

    public final byte state;

    MessageState(byte state) {
        this.state = state;
    }

    public byte getState() {
        return state;
    }
}
