/*
 * This file is part of the Show Me Your Payload Name project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  ZhuRuoLing and contributors
 *
 * Show Me Your Payload Name is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Show Me Your Payload Name is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Show Me Your Payload Name.  If not, see <https://www.gnu.org/licenses/>.
 */

package icu.takeneko.smypn;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class Test implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.INIT.register(((handler, server) -> {
            ServerPlayNetworking.registerReceiver(
                handler,
                BoomPacket.TYPE,
                BoomPacket::handle
            );
        }));
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            ClientPlayNetworking.registerReceiver(
                BoomPacket.TYPE,
                BoomPacket::handleClient
            );
        });
        PayloadTypeRegistry.playC2S().register(BoomPacket.TYPE, BoomPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BoomPacket.TYPE, BoomPacket.CODEC);
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            commandDispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("crash").
                    then(LiteralArgumentBuilder.<CommandSourceStack>literal("client").
                        executes(ctx -> {
                            ServerPlayNetworking.send(ctx.getSource().getPlayer(), new BoomPacket(1));
                            return 1;
                        })
                    ).then(LiteralArgumentBuilder.<CommandSourceStack>literal("server").
                        executes(ctx -> {
                            ClientPlayNetworking.send(new BoomPacket(1));
                            return 1;
                        })
                    )
            );
        });
    }

    public record BoomPacket(int wtf) implements CustomPacketPayload {
        //#if MC >= 12100
        //$$public static final Type<BoomPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("smypn", "boom"));
        //#else
        public static final Type<BoomPacket> TYPE = new Type<>(new ResourceLocation("smypn:boom"));
        //#endif
        public static final StreamCodec<FriendlyByteBuf, BoomPacket> CODEC = StreamCodec.of(
            (s, o) -> s.writeInt(0),
            s -> new BoomPacket(1)
        );

        public void handle(ServerPlayNetworking.Context context) {
        }


        public void handleClient(ClientPlayNetworking.Context context) {
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
