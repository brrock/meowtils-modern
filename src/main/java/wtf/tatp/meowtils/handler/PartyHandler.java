package wtf.tatp.meowtils.handler;

import wtf.tatp.meowtils.event.ChatReceivedEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.module.hypixel.AutoChannel;

/** Tracks Hypixel party membership from plain chat, matching 2.0.1 PartyHandler. */
public final class PartyHandler {
    private static boolean inParty;

    public static boolean inParty() { return inParty; }
    public static void reset() { inParty = false; }

    @EventTarget
    public void onChatReceived(ChatReceivedEvent event) {
        if (event.isOverlay()) return;
        String msg = event.getText();
        if (Server.HYPIXEL.isNotActive() || msg.contains(":")) return;
        if (msg.endsWith("has disbanded the party!")) {
            inParty = false;
            AutoChannel.swapToAll();
        }
        if (msg.equals("The party was disbanded because all invites expired and the party was empty.")) {
            inParty = false;
            AutoChannel.swapToAll();
        }
        if (msg.startsWith("You have joined") && msg.endsWith("party!")) {
            inParty = true;
            AutoChannel.swapToParty();
        }
        if (msg.endsWith("joined the party.")) {
            inParty = true;
            AutoChannel.swapToParty();
        }
        if (msg.equals("You left the party.")) {
            inParty = false;
            AutoChannel.swapToAll();
        }
        if (msg.startsWith("You have been kicked from the party")) {
            inParty = false;
            AutoChannel.swapToAll();
        }
        if (msg.equals("You are not in a party right now.")) {
            inParty = false;
            AutoChannel.swapToAll();
        }
    }
}
