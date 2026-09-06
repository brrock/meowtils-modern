package wtf.tatp.meowtils.mixin;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BookViewScreen.class)
public interface BookViewScreenAccessor {
    @Accessor("bookAccess")
    BookViewScreen.BookAccess meowtils$bookAccess();
}
