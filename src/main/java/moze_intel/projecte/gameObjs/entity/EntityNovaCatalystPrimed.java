package moze_intel.projecte.gameObjs.entity;

import javax.annotation.Nonnull;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityNovaCatalystPrimed extends TNTEntity {

	public EntityNovaCatalystPrimed(EntityType<EntityNovaCatalystPrimed> type, World world) {
		super(type, world);
		setFuse(getFuse() / 4);
	}

	public EntityNovaCatalystPrimed(World world, double x, double y, double z, LivingEntity placer) {
		super(world, x, y, z, placer);
		setFuse(getFuse() / 4);
	}

	@Nonnull
	@Override
	public EntityType<?> getType() {
		return PEEntityTypes.NOVA_CATALYST_PRIMED.get();
	}

	@Override
	protected void explode() {
		WorldHelper.createNovaExplosion(world, this, getPosX(), getPosY(), getPosZ(), 16.0F);
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}