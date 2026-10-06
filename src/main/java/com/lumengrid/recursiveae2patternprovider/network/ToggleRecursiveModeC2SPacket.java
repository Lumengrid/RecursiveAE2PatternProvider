package com.lumengrid.recursiveae2patternprovider.network;

import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import com.lumengrid.recursiveae2patternprovider.menu.IRecursivePatternMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ToggleRecursiveModeC2SPacket implements CustomPacketPayload {
    public static final Type<ToggleRecursiveModeC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(RecursiveAE2PatternProvider.MODID, "toggle_recursive_mode"));

    private final boolean isRecursive;

    public ToggleRecursiveModeC2SPacket(boolean isRecursive) {
        this.isRecursive = isRecursive;
    }

    public static final StreamCodec<FriendlyByteBuf, ToggleRecursiveModeC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    net.minecraft.network.codec.ByteBufCodecs.BOOL, p -> p.isRecursive,
                    ToggleRecursiveModeC2SPacket::new
            );

    public boolean isRecursive() {
        return isRecursive;
    }

    public static void handle(final ToggleRecursiveModeC2SPacket msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                if (player.containerMenu instanceof IRecursivePatternMenu recursiveMenu) {
                    recursiveMenu.setRecursiveMode(msg.isRecursive());
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
