# **ReceivePacketEvent**

---

This fires each time your client receives a packet from the server.

**Cancellable =** `true`

!!! Warning

    Cancelling packets is not safe and can lead to non-vanilla client behaviour that may flag anticheats. Only cancel packets when you know what you are doing.

## Parameter

`ReceivePacketEvent`

## Methods

### Packet

``` java
Packet<?> getPacket()
```

Returns current `Packet`.

## Example

``` java
/*
Prints the content of each chat message packet you receive.
*/
@EventTarget
public void onReceivePacket(ReceivePacketEvent event) {
    if (event.getPacket() instanceof S02PacketChat) {
        final S02PacketChat packet = (S02PacketChat) event.getPacket();
        final String msg = packet.getChatComponent().getUnformattedText();

        if (msg != null) {
            Meowtils.addMessage("You received a chat message: " + msg);
        }
    }
}
```