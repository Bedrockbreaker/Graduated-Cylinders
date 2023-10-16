package bedrockbreaker.graduatedcylinders.proxy.tankproperties;

import bedrockbreaker.graduatedcylinders.api.IProxyTankProperties;
import bedrockbreaker.graduatedcylinders.proxy.stack.EssentiaStack;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;

public class EssentiaTankProperties implements IProxyTankProperties {
	
	protected IAspectContainer essentiaHandler;
	protected TileEntity tileEntity;

	public EssentiaTankProperties(IAspectContainer essentiaHandler, TileEntity tileEntity) {
		this.essentiaHandler = essentiaHandler;
		this.tileEntity = tileEntity;
	}

	public EssentiaStack getContents() {
		return new EssentiaStack(this.essentiaHandler.getAspects());
	}

	public int getCapacity() {
		// Duplicate the tile entity, remove all aspects, and attempt to transfer as much essentia as possible.
		TileEntity tileEntityCopy = TileEntity.create(this.tileEntity.getWorld(), this.tileEntity.getTileData());
		IAspectContainer essentiaHandlerCopy = (IAspectContainer) tileEntityCopy;
		if (essentiaHandlerCopy == null) return 0; // IDE complaint
		essentiaHandlerCopy.setAspects(new AspectList());
		/*
		FIXME: essentia tile entity capacity
		warded jars apparently check if the amount transfered is over 250, and if it is, just skips trying to transfer any.
		brilliant code, azanor.
		could possibly be worked around by attempting to add 1 essentia at a time until #addToContainer returns a non-zero value.
		*/
		// IAspectContainer#addToContainer returns amount of essentia which could *not* be added
		return Integer.MAX_VALUE - essentiaHandlerCopy.addToContainer(this.essentiaHandler.getAspects().getAspects()[0], Integer.MAX_VALUE);
	}
}
