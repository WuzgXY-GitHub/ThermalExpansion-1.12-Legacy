package cofh.thermalexpansion.block.dynamo;

import cofh.core.fluid.FluidTankCore;
import cofh.core.gui.GuiCore;
import cofh.core.network.PacketCoFHBase;
import cofh.core.render.TextureHelper;
import cofh.core.util.helpers.AugmentHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.dynamo.BlockDynamo.Type;
import cofh.thermalexpansion.gui.client.dynamo.GuiDynamoMagmatic;
import cofh.thermalexpansion.gui.container.ContainerTEBase;
import cofh.thermalexpansion.init.TEProps;
import cofh.thermalexpansion.util.managers.CoolantManager;
import cofh.thermalexpansion.util.managers.dynamo.MagmaticManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashSet;

public class TileDynamoMagmatic extends TileDynamoBase {

	private static final int TYPE = Type.MAGMATIC.getMetadata();
	public static int basePower = 40;
	public static int fluidAmount = 100;

	public static void initialize() {

		VALID_AUGMENTS[TYPE] = new HashSet<>();
		VALID_AUGMENTS[TYPE].add(TEProps.DYNAMO_BOILER);
		VALID_AUGMENTS[TYPE].add(TEProps.DYNAMO_MAGMATIC_COOLANT);

		GameRegistry.registerTileEntity(TileDynamoMagmatic.class, "thermalexpansion:dynamo_magmatic");

		config();
	}

	public static void config() {

		String category = "Dynamo.Magmatic";
		BlockDynamo.enable[TYPE] = ThermalExpansion.CONFIG.get(category, "Enable", true);

		String comment = "Adjust this value to change the Energy generation (in RF/t) for a Magmatic Dynamo. This base value will scale with block level and Augments.";
		basePower = ThermalExpansion.CONFIG.getConfiguration().getInt("BasePower", category, basePower, MIN_BASE_POWER, MAX_BASE_POWER, comment);

		DEFAULT_ENERGY_CONFIG[TYPE] = new EnergyConfig();
		DEFAULT_ENERGY_CONFIG[TYPE].setDefaultParams(basePower);
	}

	private FluidTankCore fuelTank = new FluidTankCore(TEProps.MAX_FLUID_SMALL);
	private FluidTankCore coolantTank = new FluidTankCore(TEProps.MAX_FLUID_SMALL);
	private FluidStack renderFluid = new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME);

	private int coolantRF;

	/* AUGMENTS */
	protected boolean augmentCoolant;
	protected boolean flagTank;

	@Override
	public int getType() {

		return TYPE;
	}

	@Override
	public int getLightValue() {

		return isActive ? 14 : 0;
	}

	@Override
	protected boolean canStart() {

		if (augmentBoiler || augmentCoolant) {
			return (fuelRF > 0 || fuelTank.getFluidAmount() >= fluidAmount) && (coolantRF > 0 || coolantTank.getFluidAmount() >= fluidAmount);
		}
		return fuelTank.getFluidAmount() >= fluidAmount;
	}

	@Override
	protected boolean canFinish() {

		if (augmentBoiler || augmentCoolant) {
			return fuelRF <= 0 || coolantRF <= 0;
		}
		return fuelRF <= 0;
	}

	@Override
	protected void processStart() {

		if (augmentBoiler || augmentCoolant) {
			if (fuelRF <= 0) {
				fuelRF += MagmaticManager.getFuelEnergy100mB(fuelTank.getFluid()) * energyMod / ENERGY_BASE;
				fuelTank.drain(fluidAmount, true);
			}
			if (coolantRF <= 0) {
				coolantRF += CoolantManager.getCoolantRF100mB(coolantTank.getFluid());
				coolantTank.drain(fluidAmount, true);
			}
			return;
		}
		fuelRF += MagmaticManager.getFuelEnergy100mB(fuelTank.getFluid()) * energyMod / ENERGY_BASE;
		fuelTank.drain(fluidAmount, true);
	}

	protected int processTick() {

		if (augmentBoiler) {
			fuelRF -= energyConfig.maxPower;
			coolantRF -= energyConfig.maxPower;
			transferSteam();

			return energyConfig.maxPower;
		}
		lastEnergy = calcEnergy();
		energyStorage.modifyEnergyStored(lastEnergy);
		fuelRF -= lastEnergy;
		coolantRF -= augmentCoolant ? lastEnergy : 0;
		transferEnergy();

		return lastEnergy;
	}

	@Override
	public TextureAtlasSprite getBaseUnderlayTexture() {

		return TextureHelper.getTexture(renderFluid.getFluid().getStill(renderFluid));
	}

	/* GUI METHODS */
	@Override
	public int getScaledDuration(int scale) {

		return isActive ? GuiCore.SPEED : 0;
	}

	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiDynamoMagmatic(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerTEBase(inventory, this);
	}

	@Override
	public FluidTankCore getTank(int tankIndex) {

		if (tankIndex == 0) {
			return fuelTank;
		}
		return coolantTank;
	}

	public boolean showCoolantTank() {

		return (augmentBoiler || augmentCoolant) && flagTank;
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		coolantRF = nbt.getInteger("Coolant");
		fuelTank.readFromNBT(nbt.getCompoundTag("FuelTank"));
		coolantTank.readFromNBT(nbt.getCompoundTag("CoolantTank"));

		if (!MagmaticManager.isValidFuel(fuelTank.getFluid())) {
			fuelTank.setFluid(null);
		}
		if (!CoolantManager.isValidCoolant(coolantTank.getFluid())) {
			coolantTank.setFluid(null);
		}
		if (fuelTank.getFluid() != null) {
			renderFluid = fuelTank.getFluid();
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setInteger("Coolant", coolantRF);
		nbt.setTag("FuelTank", fuelTank.writeToNBT(new NBTTagCompound()));
		nbt.setTag("CoolantTank", coolantTank.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	/* NETWORK METHODS */

	/* SERVER -> CLIENT */
	@Override
	public PacketCoFHBase getGuiPacket() {

		PacketCoFHBase payload = super.getGuiPacket();

		payload.addBool(augmentCoolant);
		payload.addFluidStack(fuelTank.getFluid());
		payload.addFluidStack(coolantTank.getFluid());

		return payload;
	}

	@Override
	public PacketCoFHBase getTilePacket() {

		PacketCoFHBase payload = super.getTilePacket();

		payload.addFluidStack(fuelTank.getFluid());

		return payload;
	}

	@Override
	protected void handleGuiPacket(PacketCoFHBase payload) {

		super.handleGuiPacket(payload);

		augmentCoolant = payload.getBool();
		flagTank = augmentBoiler || augmentCoolant;
		fuelTank.setFluid(payload.getFluidStack());
		coolantTank.setFluid(payload.getFluidStack());
	}

	@Override
	@SideOnly (Side.CLIENT)
	public void handleTilePacket(PacketCoFHBase payload) {

		super.handleTilePacket(payload);

		FluidStack tempRender = payload.getFluidStack();
		if (tempRender == null) {
			renderFluid = new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME);
		} else {
			renderFluid = tempRender;
		}
	}

	/* HELPERS */
	@Override
	protected void preAugmentInstall() {

		super.preAugmentInstall();

		augmentCoolant = false;
	}

	@Override
	protected void postAugmentInstall() {

		super.postAugmentInstall();

		if (augmentBoiler) {
			coolantTank.setLock(FluidRegistry.WATER);
		} else if (!augmentCoolant) {
			coolantTank.drain(coolantTank.getCapacity(), true);
			coolantRF = 0;
		}
	}

	@Override
	protected boolean installAugmentToSlot(int slot) {

		String id = AugmentHelper.getAugmentIdentifier(augments[slot]);

		if (!augmentBoiler && TEProps.DYNAMO_BOILER.equals(id)) {
			augmentBoiler = true;
			hasModeAugment = true;
			energyConfig.setDefaultParams(energyConfig.maxPower + getBasePower(this.level));
			energyStorage.setEnergyStored(0);
			energyMod -= 30;
			return true;
		}
		if (!augmentCoolant && TEProps.DYNAMO_MAGMATIC_COOLANT.equals(id)) {
			augmentCoolant = true;
			hasModeAugment = true;
			energyConfig.setDefaultParams(energyConfig.maxPower + 3 * getBasePower(this.level));
			energyMod += 25;
			return true;
		}
		return super.installAugmentToSlot(slot);
	}

	/* CAPABILITIES */
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing from) {

		return super.hasCapability(capability, from) || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, final EnumFacing from) {

		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new IFluidHandler() {

				@Override
				public IFluidTankProperties[] getTankProperties() {

					return FluidTankProperties.convert(new FluidTankInfo[] { fuelTank.getInfo() });
				}

				@Override
				public int fill(FluidStack resource, boolean doFill) {

					if (resource == null || (from != null && from.ordinal() == facing && !augmentCoilDuct)) {
						return 0;
					}
					if (MagmaticManager.isValidFuel(resource)) {
						return fuelTank.fill(resource, doFill);
					}
					if (augmentBoiler && resource.getFluid() == FluidRegistry.WATER) {
						return coolantTank.fill(resource, doFill);
					} else if (augmentCoolant && CoolantManager.isValidCoolant(resource)) {
						return coolantTank.fill(resource, doFill);
					}
					return 0;
				}

				@Nullable
				@Override
				public FluidStack drain(FluidStack resource, boolean doDrain) {

					if (resource == null || !augmentCoilDuct && from.ordinal() == facing) {
						return null;
					}
					if (resource.equals(fuelTank.getFluid())) {
						return fuelTank.drain(resource.amount, doDrain);
					}
					if (augmentCoolant && resource.equals(coolantTank.getFluid())) {
						return coolantTank.drain(resource.amount, doDrain);
					}
					return null;
				}

				@Nullable
				@Override
				public FluidStack drain(int maxDrain, boolean doDrain) {

					if (!augmentCoilDuct && from.ordinal() == facing) {
						return null;
					}
					return fuelTank.drain(maxDrain, doDrain);
				}
			});
		}
		return super.getCapability(capability, from);
	}

}
