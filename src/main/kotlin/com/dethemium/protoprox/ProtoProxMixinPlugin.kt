package com.dethemium.protoprox

import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

// Applies only the Connection mixin whose target class exists at runtime, and none on clients
class ProtoProxMixinPlugin : IMixinConfigPlugin {

    override fun getMixins(): List<String> {
        if (MixinEnvironment.getCurrentEnvironment().side == MixinEnvironment.Side.CLIENT) return emptyList()
        val intermediary = javaClass.classLoader.getResource("net/minecraft/class_2535.class") != null
        return listOf(if (intermediary) "IntermediaryConnectionMixin" else "ConnectionMixin")
    }

    override fun onLoad(mixinPackage: String) {}

    override fun getRefMapperConfig(): String? = null

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String) = true

    override fun acceptTargets(myTargets: Set<String>, otherTargets: Set<String>) {}

    override fun preApply(target: String, targetClass: ClassNode, mixin: String, mixinInfo: IMixinInfo) {}

    override fun postApply(target: String, targetClass: ClassNode, mixin: String, mixinInfo: IMixinInfo) {}
}
