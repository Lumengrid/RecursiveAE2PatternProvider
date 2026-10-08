package com.lumengrid.recursiveae2patternprovider.network;

import com.lumengrid.recursiveae2patternprovider.RecursiveAE2PatternProvider;
import com.lumengrid.recursiveae2patternprovider.menu.IRecursivePatternMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleRecursiveModeC2SPacket(boolean isRecursive) implements CustomPacketPayload {

    public static final Type<ToggleRecursiveModeC2SPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(RecursiveAE2PatternProvider.MODID, "toggle_recursive_mode"));

    public static final StreamCodec<ByteBuf, ToggleRecursiveModeC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    ToggleRecursiveModeC2SPacket::isRecursive,
                    ToggleRecursiveModeC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
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
}