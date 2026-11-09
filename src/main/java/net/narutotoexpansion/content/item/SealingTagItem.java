package net.narutotoexpansion.content.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.narutotoexpansion.core.BijuuService;
import net.narutotoexpansion.content.entity.TailedBeastEntity;

/**
 * Seals a weakened tailed beast into the user: the user becomes its jinchuriki.
 * Use it by right-clicking the beast. Consumed on success (not in creative mode).
 */
public class SealingTagItem extends Item {
    public SealingTagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof TailedBeastEntity beast)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ServerPlayer sealer = (ServerPlayer) player;
        if (!beast.isWeakened()) {
            sealer.displayClientMessage(Component.translatable("msg.narutoto_expansion.beast_not_weak"), true);
            return InteractionResult.FAIL;
        }
        BijuuService.Result result = BijuuService.tryHost(sealer, beast.getBeast(), false);
        if (result != BijuuService.Result.OK) {
            sealer.displayClientMessage(result.message(), true);
            return InteractionResult.FAIL;
        }
        if (beast.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.PORTAL, beast.getX(), beast.getY() + beast.getBbHeight() * 0.5, beast.getZ(),
                    120, beast.getBbWidth() * 0.4, beast.getBbHeight() * 0.4, beast.getBbWidth() * 0.4, 0.4);
        }
        beast.discard();
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
