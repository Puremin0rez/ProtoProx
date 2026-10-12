package com.dethemium.protoprox.mixin

import com.dethemium.protoprox.ProtoProx
import io.netty.channel.ChannelHandlerContext
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

// Connection under Mojang's name, used by NeoForge and Fabric on 26.1+
@Mixin(targets = ["net.minecraft.network.Connection"])
abstract class ConnectionMixin {

    @Inject(method = ["channelActive"], at = [At("HEAD")], cancellable = true)
    private fun `protoprox$install`(ctx: ChannelHandlerContext, ci: CallbackInfo) {
        if (ProtoProx.install(ctx)) ci.cancel()
    }
}
