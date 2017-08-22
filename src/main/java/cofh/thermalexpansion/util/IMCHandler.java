package cofh.thermalexpansion.util;

import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.util.managers.dynamo.CompressionManager;
import cofh.thermalexpansion.util.managers.dynamo.MagmaticManager;
import cofh.thermalexpansion.util.managers.machine.*;
import cofh.thermalexpansion.util.managers.machine.CompactorManager.Mode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;

import java.util.List;
import java.util.Locale;

public class IMCHandler {

	public static final IMCHandler INSTANCE = new IMCHandler();

	public void handleIMC(List<IMCMessage> messages) {

		NBTTagCompound nbt;
		for (IMCMessage message : messages) {
			try {
				if (!message.isNBTMessage()) {
					continue;
				}
				nbt = message.getNBTValue();
				String operation = message.key.toLowerCase(Locale.US);

				switch (operation) {
					/* ADD RECIPES */
					case ADD_FURNACE_RECIPE:
						FurnaceManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(OUTPUT)));
						continue;
					case ADD_PULVERIZER_RECIPE:
						if (nbt.hasKey(SECONDARY_CHANCE)) {
							PulverizerManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_OUTPUT)), nbt.getInteger(SECONDARY_CHANCE));
						} else if (nbt.hasKey(SECONDARY_OUTPUT)) {
							PulverizerManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_OUTPUT)));
						} else {
							PulverizerManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)));
						}
						continue;
					case ADD_SAWMILL_RECIPE:
						if (nbt.hasKey(SECONDARY_CHANCE)) {
							SawmillManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_OUTPUT)), nbt.getInteger(SECONDARY_CHANCE));
						} else if (nbt.hasKey(SECONDARY_OUTPUT)) {
							SawmillManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_OUTPUT)));
						} else {
							SawmillManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)));
						}
						continue;
					case ADD_SMELTER_RECIPE:
						if (nbt.hasKey(SECONDARY_CHANCE)) {
							SmelterManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(PRIMARY_INPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_OUTPUT)), nbt.getInteger(SECONDARY_CHANCE));
						} else if (nbt.hasKey(SECONDARY_OUTPUT)) {
							SmelterManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(PRIMARY_INPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_OUTPUT)));
						} else {
							SmelterManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(PRIMARY_INPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)));
						}
						continue;
					case ADD_INSOLATOR_RECIPE:
						if (nbt.hasKey(SECONDARY_CHANCE)) {
							InsolatorManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(PRIMARY_INPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_OUTPUT)), nbt.getInteger(SECONDARY_CHANCE));
						} else if (nbt.hasKey(SECONDARY_OUTPUT)) {
							InsolatorManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(PRIMARY_INPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_OUTPUT)));
						} else {
							InsolatorManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(PRIMARY_INPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_INPUT)), new ItemStack(nbt.getCompoundTag(PRIMARY_OUTPUT)));
						}
						continue;
					case ADD_COMPACTOR_PRESS_RECIPE:
						CompactorManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(OUTPUT)), Mode.PRESS);
						continue;
					case ADD_COMPACTOR_STORAGE_RECIPE:
						CompactorManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(OUTPUT)), Mode.STORAGE);
						continue;
					case ADD_COMPACTOR_MINT_RECIPE:
						CompactorManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(OUTPUT)), Mode.MINT);
						continue;
					case ADD_CRUCIBLE_RECIPE:
						CrucibleManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(OUTPUT)));
						continue;
					case ADD_REFINERY_RECIPE:
						RefineryManager.addRecipe(nbt.getInteger(ENERGY), FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(INPUT)), FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(OUTPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_OUTPUT)));
						continue;
					case ADD_TRANSPOSER_FILL_RECIPE:
						TransposerManager.addFillRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(OUTPUT)), FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(FLUID)), nbt.getBoolean("reversible"));
						continue;
					case ADD_TRANSPOSER_EXTRACT_RECIPE:
						TransposerManager.addExtractRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(OUTPUT)), FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(FLUID)), nbt.getInteger("chance"), nbt.getBoolean("reversible"));
						continue;
					case ADD_CHARGER_RECIPE:
						ChargerManager.addRecipe(nbt.getInteger(ENERGY), new ItemStack(nbt.getCompoundTag(INPUT)), new ItemStack(nbt.getCompoundTag(OUTPUT)));
						continue;
					case ADD_CENTRIFUGE_RECIPE:
						// TODO
						continue;

					/* REMOVE RECIPES */
					case REMOVE_FURNACE_RECIPE:
						FurnaceManager.removeRecipe(new ItemStack(nbt.getCompoundTag(INPUT)));
						continue;
					case REMOVE_PULVERIZER_RECIPE:
						PulverizerManager.removeRecipe(new ItemStack(nbt.getCompoundTag(INPUT)));
						continue;
					case REMOVE_SAWMILL_RECIPE:
						SawmillManager.removeRecipe(new ItemStack(nbt.getCompoundTag(INPUT)));
						continue;
					case REMOVE_SMELTER_RECIPE:
						SmelterManager.removeRecipe(new ItemStack(nbt.getCompoundTag(PRIMARY_INPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_INPUT)));
						continue;
					case REMOVE_INSOLATOR_RECIPE:
						InsolatorManager.removeRecipe(new ItemStack(nbt.getCompoundTag(PRIMARY_INPUT)), new ItemStack(nbt.getCompoundTag(SECONDARY_INPUT)));
						continue;
					case REMOVE_COMPACTOR_PRESS_RECIPE:
						CompactorManager.removeRecipe(new ItemStack(nbt.getCompoundTag(INPUT)), Mode.PRESS);
						continue;
					case REMOVE_COMPACTOR_STORAGE_RECIPE:
						CompactorManager.removeRecipe(new ItemStack(nbt.getCompoundTag(INPUT)), Mode.STORAGE);
						continue;
					case REMOVE_COMPACTOR_MINT_RECIPE:
						CompactorManager.removeRecipe(new ItemStack(nbt.getCompoundTag(INPUT)), Mode.MINT);
						continue;
					case REMOVE_CRUCIBLE_RECIPE:
						CrucibleManager.removeRecipe(new ItemStack(nbt.getCompoundTag(INPUT)));
						continue;
					case REMOVE_REFINERY_RECIPE:
						RefineryManager.removeRecipe(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(INPUT)));
						continue;
					case REMOVE_TRANSPOSER_FILL_RECIPE:
						TransposerManager.removeFillRecipe(new ItemStack(nbt.getCompoundTag(INPUT)), FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(FLUID)));
						continue;
					case REMOVE_TRANSPOSER_EXTRACT_RECIPE:
						TransposerManager.removeExtractRecipe(new ItemStack(nbt.getCompoundTag(INPUT)));
						continue;
					case REMOVE_CHARGER_RECIPE:
						ChargerManager.removeRecipe(new ItemStack(nbt.getCompoundTag(INPUT)));
						continue;
					case REMOVE_CENTRIFUGE_RECIPE:
						CentrifugeManager.removeRecipe(new ItemStack(nbt.getCompoundTag(INPUT)));
						continue;

					/* FUELS */
					case ADD_MAGMATIC_FUEL:
						MagmaticManager.addFuel(nbt.getString(FLUID_NAME).toLowerCase(Locale.ENGLISH), nbt.getInteger(ENERGY));
						continue;
					case ADD_COMPRESSION_FUEL:
						CompressionManager.addFuel(nbt.getString(FLUID_NAME).toLowerCase(Locale.ENGLISH), nbt.getInteger(ENERGY));
						continue;
				}
				ThermalExpansion.LOG.warn("Thermal Expansion received an invalid IMC from " + message.getSender() + "! Key was " + message.key);
			} catch (Exception e) {
				ThermalExpansion.LOG.warn("Thermal Expansion received a broken IMC from " + message.getSender() + "!");
				e.printStackTrace();
			}
		}
	}

	/* IMC STRINGS */
	static final String ENERGY = "energy";
	static final String FLUID = "fluid";
	static final String FLUID_NAME = "fluidName";

	static final String INPUT = "input";
	static final String OUTPUT = "output";
	static final String PRIMARY_INPUT = "primaryInput";
	static final String SECONDARY_INPUT = "secondaryInput";
	static final String PRIMARY_OUTPUT = "primaryOutput";
	static final String SECONDARY_OUTPUT = "secondaryOutput";
	static final String SECONDARY_CHANCE = "secondaryChance";

	public static final String ADD_FURNACE_RECIPE = "addfurnacerecipe";
	public static final String ADD_PULVERIZER_RECIPE = "addpulverizerrecipe";
	public static final String ADD_SAWMILL_RECIPE = "addsawmillrecipe";
	public static final String ADD_SMELTER_RECIPE = "addsmelterrecipe";
	public static final String ADD_INSOLATOR_RECIPE = "addinsolatorrecipe";

	public static final String ADD_COMPACTOR_PRESS_RECIPE = "addcompactorpressrecipe";
	public static final String ADD_COMPACTOR_STORAGE_RECIPE = "addcompactorstoragerecipe";
	public static final String ADD_COMPACTOR_MINT_RECIPE = "addcompactormintrecipe";

	public static final String ADD_CRUCIBLE_RECIPE = "addcruciblerecipe";
	public static final String ADD_REFINERY_RECIPE = "addrefineryrecipe";

	public static final String ADD_TRANSPOSER_FILL_RECIPE = "addtransposerfillrecipe";
	public static final String ADD_TRANSPOSER_EXTRACT_RECIPE = "addtransposerextractrecipe";

	public static final String ADD_CHARGER_RECIPE = "addchargerrecipe";

	public static final String ADD_CENTRIFUGE_RECIPE = "addcentrifugerecipe";

	public static final String REMOVE_FURNACE_RECIPE = "removefurnacerecipe";
	public static final String REMOVE_PULVERIZER_RECIPE = "removepulverizerrecipe";
	public static final String REMOVE_SAWMILL_RECIPE = "removesawmillrecipe";
	public static final String REMOVE_SMELTER_RECIPE = "removesmelterrecipe";
	public static final String REMOVE_INSOLATOR_RECIPE = "removeinsolatorrecipe";

	public static final String REMOVE_COMPACTOR_PRESS_RECIPE = "removecompactorpressrecipe";
	public static final String REMOVE_COMPACTOR_STORAGE_RECIPE = "removecompactorstoragerecipe";
	public static final String REMOVE_COMPACTOR_MINT_RECIPE = "removecompactormintrecipe";

	public static final String REMOVE_CRUCIBLE_RECIPE = "removecruciblerecipe";
	public static final String REMOVE_REFINERY_RECIPE = "removerefineryrecipe";

	public static final String REMOVE_TRANSPOSER_FILL_RECIPE = "removetransposerfillrecipe";
	public static final String REMOVE_TRANSPOSER_EXTRACT_RECIPE = "removetransposerextractrecipe";

	public static final String REMOVE_CHARGER_RECIPE = "removechargerrecipe";

	public static final String REMOVE_CENTRIFUGE_RECIPE = "removecentrifugerecipe";

	public static final String ADD_MAGMATIC_FUEL = "addmagmaticfuel";
	public static final String ADD_COMPRESSION_FUEL = "addcompressionfuel";

}
