package moze_intel.projecte.gameObjs.items.armor;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.handlers.InternalTimers;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GemHelmet extends GemArmorBase {

	public GemHelmet(Properties props) {
		super(EquipmentSlotType.HEAD, props);
	}

	public static boolean isNightVisionEnabled(ItemStack helm) {
		return helm.hasTag() && helm.getTag().contains(Constants.NBT_KEY_NIGHT_VISION) && helm.getTag().getBoolean(Constants.NBT_KEY_NIGHT_VISION);
	}

	public static void toggleNightVision(ItemStack helm, PlayerEntity player) {
		boolean value;
		CompoundNBT helmetTag = helm.getOrCreateTag();
		if (helmetTag.contains(Constants.NBT_KEY_NIGHT_VISION)) {
			helmetTag.putBoolean(Constants.NBT_KEY_NIGHT_VISION, !helmetTag.getBoolean(Constants.NBT_KEY_NIGHT_VISION));
			value = helmetTag.getBoolean(Constants.NBT_KEY_NIGHT_VISION);
		} else {
			//If we don't have the tag count that as it already being "false"
			helmetTag.putBoolean(Constants.NBT_KEY_NIGHT_VISION, true);
			value = true;
		}
		if (value) {
			player.sendMessage(PELang.NIGHT_VISION.translate(TextFormatting.GREEN, PELang.GEM_ENABLED), Util.DUMMY_UUID);
		} else {
			player.sendMessage(PELang.NIGHT_VISION.translate(TextFormatting.RED, PELang.GEM_DISABLED), Util.DUMMY_UUID);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltips, @Nonnull ITooltipFlag flags) {
		super.addInformation(stack, world, tooltips, flags);
		tooltips.add(PELang.GEM_LORE_HELM.translate());
		//TODO - 1.16: Change this to using the keybind modifier system forge adds so that we can allow them to change it separately?
		tooltips.add(PELang.NIGHT_VISION_PROMPT.translate(Minecraft.getInstance().gameSettings.keyBindSneak.func_238171_j_(), ClientKeyHelper.getKeyName(PEKeybind.ARMOR_TOGGLE)));
		if (isNightVisionEnabled(stack)) {
			tooltips.add(PELang.NIGHT_VISION.translate(TextFormatting.GREEN, PELang.GEM_ENABLED));
		} else {
			tooltips.add(PELang.NIGHT_VISION.translate(TextFormatting.RED, PELang.GEM_DISABLED));
		}
	}

	@Override
	public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
		if (world.isRemote) {
			int x = (int) Math.floor(player.getPosX());
			int y = (int) (player.getPosY() - player.getYOffset());
			int z = (int) Math.floor(player.getPosZ());
			BlockPos pos = new BlockPos(x, y, z);
			Block b = world.getBlockState(pos.down()).getBlock();

			if (b == Blocks.WATER && world.isAirBlock(pos)) {
				if (!player.isSneaking()) {
					player.setMotion(player.getMotion().mul(1, 0, 1));
					player.fallDistance = 0.0f;
					player.setOnGround(true);
				}
			}
		} else {
			player.getCapability(InternalTimers.CAPABILITY).ifPresent(handler -> {
				handler.activateHeal();
				if (player.getHealth() < player.getMaxHealth() && handler.canHeal()) {
					player.heal(2.0F);
				}
			});

			if (isNightVisionEnabled(stack)) {
				player.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 220, 0, true, false));
			} else {
				player.removePotionEffect(Effects.NIGHT_VISION);
			}

			if (player.isInWater()) {
				player.setAir(300);
			}
		}
	}

	public void doZap(PlayerEntity player) {
		if (ProjectEConfig.server.difficulty.offensiveAbilities.get()) {
			BlockRayTraceResult strikeResult = PlayerHelper.getBlockLookingAt(player, 120.0F);
			if (strikeResult.getType() != Type.MISS) {
				BlockPos strikePos = strikeResult.getPos();
				World world = player.getEntityWorld();
				LightningBoltEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
				if (lightning != null) {
					lightning.moveForced(Vector3d.copyCentered(strikePos));
					lightning.setCaster((ServerPlayerEntity) player);
					world.addEntity(lightning);
				}
			}
		}
	}
}