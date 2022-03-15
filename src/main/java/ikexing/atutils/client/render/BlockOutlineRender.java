package ikexing.atutils.client.render;


import com.google.gson.JsonSyntaxException;
import ikexing.atutils.ATUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlockOutlineRender {

    private static final Logger LOGGER = LogManager.getLogger();

    private Framebuffer outlineBuffer;
    private ShaderGroup shader;
    private Minecraft mc;

    private int displayWidth = -1;
    private int displayHeight = -1;

    private Map<BlockPos, IBlockState> list;


    public void init() {
        if (!OpenGlHelper.shadersSupported) {
            return;
        }


        //这里idea会提示始终非空，但是实际上它有可能null
        //noinspection ConstantConditions
        if (ShaderLinkHelper.getStaticShaderLinkHelper() == null) {
            ShaderLinkHelper.setNewStaticShaderLinkHelper();
        }

        ResourceLocation resourcelocation = new ResourceLocation(ATUtils.MODID, "shaders/post/block_outline.json");

        try {
            this.mc = Minecraft.getMinecraft();
            //初始化着色器
            this.shader = new ShaderGroup(this.mc.getTextureManager(), this.mc.getResourceManager(), this.mc.getFramebuffer(), resourcelocation);

            //初始化帧缓冲
            this.displayWidth = mc.displayWidth;
            this.displayHeight = mc.displayHeight;
            this.shader.createBindFramebuffers(displayWidth, displayHeight);
            this.outlineBuffer = this.shader.getFramebufferRaw("atutils:final");
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.warn("Failed to load shader: {}", resourcelocation, e);
        }

    }


    public void updateFrameBufferSize() {

        if (!isEnabled()) {
            return;
        }

        if (this.displayHeight != mc.displayHeight || this.displayWidth != mc.displayWidth) {
            this.displayWidth = mc.displayWidth;
            this.displayHeight = mc.displayHeight;
            this.shader.createBindFramebuffers(displayWidth, displayHeight);
        }

    }

    public void renderToBuffer(float partialTicks) {
        if (!isEnabled()) {
            return;
        }
        this.outlineBuffer.framebufferClear();

        if (list != null && !list.isEmpty()) {
            this.outlineBuffer.bindFramebuffer(false);

            //按理来说混合是关的，这里在调用一遍保证一下
            GlStateManager.disableBlend();

            //深度测试关掉，透明度测试关掉，深度数据写入关掉，材质关掉
            GlStateManager.disableDepth();
            GlStateManager.disableAlpha();
            GlStateManager.depthMask(false);
            GlStateManager.disableTexture2D();


            GlStateManager.pushMatrix();

            //坐标根据玩家镜头一次变换
            RenderManager renderManager = mc.getRenderManager();
            double viewerPosX = renderManager.viewerPosX;
            double viewerPosY = renderManager.viewerPosY;
            double viewerPosZ = renderManager.viewerPosZ;
            GlStateManager.translate(-viewerPosX, -viewerPosY, -viewerPosZ);


            BlockRendererDispatcher renderer = mc.getBlockRendererDispatcher();
            GlStateManager.color(1F, 1F, 1F ,1F);
            for (Map.Entry<BlockPos, IBlockState> entry : list.entrySet()) {
                GlStateManager.pushMatrix();
                BlockPos offset = entry.getKey();

                //渲染具体坐标二次变换
                GlStateManager.translate(offset.getX(), offset.getY(), offset.getZ());
                IBakedModel ibakedmodel = renderer.getModelForState(entry.getValue());
                renderer.getBlockModelRenderer().renderModelBrightnessColor(
                    entry.getValue(), ibakedmodel, 1.0f, 1.0f, 1.0f, 1.0f

                );
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();

            //着色器后处理轮廓
            this.shader.render(partialTicks);

            //上面的4个改回去
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();


        }
        //改回主缓冲区
        this.mc.getFramebuffer().bindFramebuffer(false);
    }

    /**
     * 将缓冲区内容渲染到屏幕上面
     */
    public void renderToScreen() {
        if (this.isEnabled()) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableBlend();

            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            this.outlineBuffer.framebufferRenderExt(this.mc.displayWidth, this.mc.displayHeight, false);
            GlStateManager.disableBlend();
        }
    }


    public boolean isEnabled() {
        return this.outlineBuffer != null && this.shader != null && this.mc != null && this.mc.player != null;
    }

    public void setRenderList(Map<BlockPos, IBlockState> list) {
        this.list = list;
    }
}

