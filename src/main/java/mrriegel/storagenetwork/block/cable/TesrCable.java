package mrriegel.storagenetwork.block.cable;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TesrCable extends TileEntitySpecialRenderer<TileCable> {

  private ModelCable model;
  private static Map<Block, ResourceLocation> renderMaps = new HashMap<>();

  public static void addCableRender(Block block, ResourceLocation image) {
    renderMaps.put(block, image);
  }

  // TODO: Use baked models instead of tesrs
  public TesrCable() {
    model = new ModelCable();
  }

  public void renderFacadeBlock(IBlockState block, World world, BlockPos tePos, double x, double y, double z) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();
    this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    RenderHelper.disableStandardItemLighting();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GlStateManager.enableBlend();
    GlStateManager.disableCull();

    if (Minecraft.isAmbientOcclusionEnabled()) {
      GlStateManager.shadeModel(GL11.GL_SMOOTH);
    } else {
      GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

    IBlockAccess renderAccess = MinecraftForgeClient.getRegionRenderCache(world, tePos);
    buffer.setTranslation(x - tePos.getX(), y - tePos.getY(), z - tePos.getZ());

    BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
    dispatcher.renderBlock(block, tePos, new BlockAccessFacade(renderAccess), buffer);

    buffer.setTranslation(0, 0, 0);

    tessellator.draw();

    RenderHelper.enableStandardItemLighting();
  }

  @Override
  public void render(TileCable te, double x, double y, double z, float partialTicks, int destroyStage, float partial) {
    if (te == null) {
      return;
    }
    IBlockState blockstate = te.getWorld().getBlockState(te.getPos());
    if (!(blockstate.getBlock() instanceof BlockCable)) {
      return;
    }
    blockstate = blockstate.getActualState(te.getWorld(), te.getPos());
    IExtendedBlockState extendedBlockState = (IExtendedBlockState) blockstate.getBlock().getExtendedState(blockstate, te.getWorld(), te.getPos());
    UnlistedPropertyBlockNeighbors.BlockNeighbors neighbors = extendedBlockState.getValue(BlockCable.BLOCK_NEIGHBORS);
    if (neighbors == null) {
      return;
    }
    IBlockState facadeState = te.facadeState;
    if (facadeState != null) {
      renderFacadeBlock(facadeState, te.getWorld(), te.getPos(), x, y, z);
      return;
    }
    GlStateManager.pushMatrix();
    GlStateManager.enableRescaleNormal();
    GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
    Block kind = te.getBlockType();
    if (renderMaps.containsKey(kind)) {
      Minecraft.getMinecraft().renderEngine.bindTexture(renderMaps.get(kind));
    }
    GlStateManager.pushMatrix();
    GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.pushAttrib();
    RenderHelper.disableStandardItemLighting();
    model.render(neighbors, kind);
    RenderHelper.enableStandardItemLighting();
    GlStateManager.popAttrib();
    GlStateManager.popMatrix();
    GlStateManager.disableRescaleNormal();
    GlStateManager.popMatrix();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
  }
}
