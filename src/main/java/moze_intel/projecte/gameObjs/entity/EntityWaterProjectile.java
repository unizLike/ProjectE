package moze_intel.projecte.gameObjs.entity;

import javax.annotation.Nonnull;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.IPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityWaterProjectile extends ThrowableEntity {

	public EntityWaterProjectile(EntityType<EntityWaterProjectile> type, World world) {
		super(type, world);
	}

	public EntityWaterProjectile(PlayerEntity entity, World world) {
		super(PEEntityTypes.WATER_PROJECTILE.get(), entity, world);
	}

	@Override
	protected void registerData() {
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.getEntityWorld().isRemote) {
			if (ticksExisted > 400 || !getEntityWorld().isBlockPresent(getPosition())) {
				remove();
				return;
			}
			Entity thrower = func_234616_v_();
			if (thrower instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity) thrower;
				BlockPos.getAllInBox(getPosition().add(-3, -3, -3), getPosition().add(3, 3, 3)).forEach(pos -> {
					FluidState state = getEntityWorld().getFluidState(pos);
					if (state.isTagged(FluidTags.LAVA)) {
						pos = pos.toImmutable();
						if (state.isSource()) {
							PlayerHelper.checkedReplaceBlock(player, pos, Blocks.OBSIDIAN.getDefaultState());
						} else {
							PlayerHelper.checkedReplaceBlock(player, pos, Blocks.COBBLESTONE.getDefaultState());
						}
						playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.5F, 2.6F + (getEntityWorld().rand.nextFloat() - getEntityWorld().rand.nextFloat()) * 0.8F);
					}
				});
			}
			if (isInWater()) {
				remove();
			}
			if (getPosY() > 128) {
				IWorldInfo worldInfo = this.getEntityWorld().getWorldInfo();
				worldInfo.setRaining(true);
				remove();
			}
		}
	}

	@Override
	public float getGravityVelocity() {
		return 0;
	}

	@Override
	protected void onImpact(@Nonnull RayTraceResult mop) {
		if (world.isRemote) {
			return;
		}
		Entity thrower = func_234616_v_();
		if (!(thrower instanceof PlayerEntity)) {
			remove();
			return;
		}
		if (mop instanceof BlockRayTraceResult) {
			BlockRayTraceResult result = (BlockRayTraceResult) mop;
			WorldHelper.placeFluid((ServerPlayerEntity) thrower, world, result.getPos(), result.getFace(), Fluids.WATER, !ProjectEConfig.server.items.opEvertide.get());
		} else if (mop instanceof EntityRayTraceResult) {
			Entity ent = ((EntityRayTraceResult) mop).getEntity();
			if (ent.isBurning()) {
				ent.extinguish();
			}
			ent.addVelocity(this.getMotion().getX() * 2, this.getMotion().getY() * 2, this.getMotion().getZ() * 2);
		}
		remove();
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}