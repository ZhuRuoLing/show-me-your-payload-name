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
package icu.takeneko.smypn.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PacketDecoder.class)
public class PacketDecoderMixin {
    @Shadow
    @Final
    private ProtocolInfo<?> protocolInfo;

    @Inject(
        method = "decode",
        at = @At(
            value = "INVOKE",
            target = "Ljava/io/IOException;<init>(Ljava/lang/String;)V"
        )
    )
    void wrapNewException(
        ChannelHandlerContext channelHandlerContext,
        ByteBuf byteBuf,
        List<Object> list,
        CallbackInfo ci,
        @Local PacketType<?> packetType
    ) {
        if (packetType == CommonPacketTypes.CLIENTBOUND_CUSTOM_PAYLOAD || packetType == CommonPacketTypes.SERVERBOUND_CUSTOM_PAYLOAD) {
            FriendlyByteBuf buf = new FriendlyByteBuf(byteBuf);
            buf.readerIndex(0);
            buf.readVarInt();
            String packetId = buf.readUtf();
            throw new RuntimeException(
                "Packet " + this.protocolInfo.id().id() + "/" + packetType
                    + " (" + "custom payload id: " + packetId + ") was larger than I expected, found "
                    + byteBuf.readableBytes() + " bytes extra whilst reading packet "
                    + packetType
            );
        }
    }
}