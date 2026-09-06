package wtf.tatp.meowtils.event;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Optional;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/** Unlit ESP geometry that neither tests nor writes terrain depth. */
public final class EspRenderTypes {
    public static final RenderType FILL = create("esp_fill", RenderPipelines.DEBUG_FILLED_SNIPPET);
    public static final RenderType LINES = create("esp_lines", RenderPipelines.LINES_SNIPPET);

    private EspRenderTypes() {}

    private static RenderType create(String name, RenderPipeline.Snippet snippet) {
        RenderPipeline pipeline = RenderPipelines.register(RenderPipeline.builder(snippet)
                .withLocation("meowtils:pipeline/" + name)
                .withDepthStencilState(Optional.empty())
                .withCull(false)
                .build());
        // Preserve submission order so the health fill stays above its background.
        return RenderType.create("meowtils_" + name, RenderSetup.builder(pipeline).createRenderSetup());
    }
}
