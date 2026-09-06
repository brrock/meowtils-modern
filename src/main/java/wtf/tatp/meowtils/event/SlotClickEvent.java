package wtf.tatp.meowtils.event;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import wtf.tatp.meowtils.event.api.Event;

/** Client container click, used by ShopHelper to cancel duplicates and rewrite left clicks. */
public final class SlotClickEvent extends Event {
    private final AbstractContainerScreen<?> screen;
    private final Slot slot;
    private final int slotId;
    private int button;
    private ContainerInput input;
    private boolean replaceClick;

    public SlotClickEvent(AbstractContainerScreen<?> screen, Slot slot, int slotId, int button, ContainerInput input) {
        this.screen = screen;
        this.slot = slot;
        this.slotId = slotId;
        this.button = button;
        this.input = input;
    }

    public AbstractContainerScreen<?> getScreen() { return screen; }
    public Slot getSlot() { return slot; }
    public int getSlotId() { return slotId; }
    public int getButton() { return button; }
    public ContainerInput getInput() { return input; }
    public boolean getReplaceClick() { return replaceClick; }
    public void setButton(int button) { this.button = button; }
    public void setInput(ContainerInput input) { this.input = input; }
    public void setReplaceClick() { this.replaceClick = true; }
}
