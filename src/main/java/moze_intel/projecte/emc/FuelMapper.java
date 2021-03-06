package moze_intel.projecte.emc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import moze_intel.projecte.PECore;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollectionSupplier;

public final class FuelMapper {

	private static final List<Item> FUEL_MAP = new ArrayList<>();

	public static void loadMap(ITagCollectionSupplier tagCollectionSupplier) {
		FUEL_MAP.clear();
		//Note: We need to get the tag from the tag collection supplier as the named tag variants may not have been
		// populated yet
		ITag<Item> collectorFuelTag = tagCollectionSupplier.getItemTags().getTagByID(PECore.rl("collector_fuel"));
		collectorFuelTag.getAllElements().stream().filter(EMCHelper::doesItemHaveEmc).forEach(FUEL_MAP::add);
		FUEL_MAP.sort(Comparator.comparing(EMCHelper::getEmcValue));
	}

	public static boolean isStackFuel(ItemStack stack) {
		return FUEL_MAP.contains(stack.getItem());
	}

	public static boolean isStackMaxFuel(ItemStack stack) {
		return FUEL_MAP.indexOf(stack.getItem()) == FUEL_MAP.size() - 1;
	}

	public static ItemStack getFuelUpgrade(ItemStack stack) {
		int index = FUEL_MAP.indexOf(stack.getItem());
		if (index == -1) {
			PECore.LOGGER.warn("Tried to upgrade invalid fuel: {}", stack);
			return ItemStack.EMPTY;
		}
		int nextIndex = index == FUEL_MAP.size() - 1 ? 0 : index + 1;
		return new ItemStack(FUEL_MAP.get(nextIndex));
	}

	/**
	 * @return An immutable version of the Fuel Map
	 */
	public static List<Item> getFuelMap() {
		return Collections.unmodifiableList(FUEL_MAP);
	}
}