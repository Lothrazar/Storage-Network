package mrriegel.storagenetwork.gui;

import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.block.cable.io.ContainerCableIO;
import mrriegel.storagenetwork.block.cable.io.GuiCableIO;
import mrriegel.storagenetwork.block.cable.link.ContainerCableLink;
import mrriegel.storagenetwork.block.cable.link.GuiCableLink;
import mrriegel.storagenetwork.block.cable.processing.ContainerCableProcessing;
import mrriegel.storagenetwork.block.cable.processing.GuiCableProcessing;
import mrriegel.storagenetwork.block.cable.processing.TileCableProcess;
import mrriegel.storagenetwork.block.control.ContainerControl;
import mrriegel.storagenetwork.block.control.GuiControl;
import mrriegel.storagenetwork.block.control.TileControl;
import mrriegel.storagenetwork.block.inventory.ContainerInventory;
import mrriegel.storagenetwork.block.inventory.GuiInventory;
import mrriegel.storagenetwork.block.inventory.TileInventory;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.block.request.ContainerRequest;
import mrriegel.storagenetwork.block.request.GuiRequest;
import mrriegel.storagenetwork.block.request.TileRequest;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.gui.fb.ContainerFastRemote;
import mrriegel.storagenetwork.gui.fb.ContainerFastRequest;
import mrriegel.storagenetwork.gui.fb.GuiFastRemote;
import mrriegel.storagenetwork.gui.fb.GuiFastRequest;
import mrriegel.storagenetwork.item.remote.ContainerRemote;
import mrriegel.storagenetwork.item.remote.GuiRemote;
import mrriegel.storagenetwork.item.remote.RemoteType;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandler implements IGuiHandler {

  public enum GuiIDs {
    LINK, IMPORT, EXPORT, PROCESSING, REQUEST, REMOTE, CONTROLLER, INVENTORY
  }

  public static final boolean FB_LOADED = Loader.isModLoaded("fastbench");

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    BlockPos pos = new BlockPos(x, y, z);
    if (ID != GuiIDs.REMOTE.ordinal()) {
      UtilTileEntity.updateTile(world, pos);
    }
    if (ID == GuiIDs.LINK.ordinal()) {
      return new ContainerCableLink((TileCable) world.getTileEntity(pos), player.inventory);
    }
    if (ID == GuiIDs.IMPORT.ordinal()) {
      return new ContainerCableIO((TileCable) world.getTileEntity(pos), player.inventory);
    }
    if (ID == GuiIDs.EXPORT.ordinal()) {
      return new ContainerCableIO((TileCable) world.getTileEntity(pos), player.inventory);
    }
    if (ID == GuiIDs.PROCESSING.ordinal()) {
      return new ContainerCableProcessing((TileCable) world.getTileEntity(pos), player.inventory);
    }
    if (ID == GuiIDs.CONTROLLER.ordinal()) {
      TileMaster master = ((TileControl) world.getTileEntity(pos)).getMaster().getTileEntity(TileMaster.class);
      return new ContainerControl(master, player.inventory);
    }
    if (ID == GuiIDs.INVENTORY.ordinal()) {
      return new ContainerInventory((TileInventory) world.getTileEntity(pos), player.inventory);
    }
    if (ID == GuiIDs.REQUEST.ordinal()) {
      if (FB_LOADED && ConfigHandler.allowFastWorkBenchIntegration) {
        return new ContainerFastRequest((TileRequest) world.getTileEntity(pos), player, world, pos);
      }
      else {
        return new ContainerRequest((TileRequest) world.getTileEntity(pos), player.inventory);
      }
    }
    if (ID == GuiIDs.REMOTE.ordinal()) {
      if (FB_LOADED && ConfigHandler.allowFastWorkBenchIntegration && player.inventory.getStackInSlot(x).getMetadata() != RemoteType.SIMPLE.ordinal()) {
        return new ContainerFastRemote(player, world, x);
      }
      else {
        return new ContainerRemote(player.inventory, x);
      }
    }
    return null;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    BlockPos pos = new BlockPos(x, y, z);
    if (ID == GuiIDs.LINK.ordinal()) {
      TileCable tile = (TileCable) world.getTileEntity(pos);
      return new GuiCableLink(new ContainerCableLink(tile, player.inventory));
    }
    if (ID == GuiIDs.IMPORT.ordinal()) {
      TileCable tile = (TileCable) world.getTileEntity(pos);
      return new GuiCableIO(new ContainerCableIO(tile, player.inventory));
    }
    if (ID == GuiIDs.EXPORT.ordinal()) {
      TileCable tile = (TileCable) world.getTileEntity(pos);
      return new GuiCableIO(new ContainerCableIO(tile, player.inventory));
    }
    if (ID == GuiIDs.PROCESSING.ordinal()) {
      return new GuiCableProcessing((TileCableProcess) world.getTileEntity(pos), new ContainerCableProcessing((TileCable) world.getTileEntity(pos), player.inventory));
    }
    if (ID == GuiIDs.CONTROLLER.ordinal()) {
      TileMaster master = ((TileControl) world.getTileEntity(pos)).getMaster().getTileEntity(TileMaster.class);
      return new GuiControl(new ContainerControl(master, player.inventory));
    }
    if (ID == GuiIDs.INVENTORY.ordinal()) {
      return new GuiInventory(new ContainerInventory((TileInventory) world.getTileEntity(pos), player.inventory));
    }
    //todo new ID similar to CONTROLLER but pass master from itemstack not tile 
    // for button on a gui 
    if (ID == GuiIDs.REQUEST.ordinal()) {
      if (FB_LOADED && ConfigHandler.allowFastWorkBenchIntegration) {
        return new GuiFastRequest(player, world, pos);
      }
      else {
        return new GuiRequest(new ContainerRequest((TileRequest) world.getTileEntity(pos), player.inventory));
      }
    }
    if (ID == GuiIDs.REMOTE.ordinal()) {
      if (FB_LOADED && ConfigHandler.allowFastWorkBenchIntegration && player.inventory.getStackInSlot(x).getMetadata() != RemoteType.SIMPLE.ordinal()) {
        return new GuiFastRemote(player, world, x);
      }
      else {
        return new GuiRemote(new ContainerRemote(player.inventory, x));
      }
    }
    return null;
  }
}
