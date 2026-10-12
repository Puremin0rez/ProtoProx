package com.dethemium.protoprox

import io.netty.channel.AbstractChannel
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder
import io.netty.util.AttributeKey
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.net.SocketAddress
import org.slf4j.LoggerFactory

object ProtoProx {
    internal const val DECODER_NAME = "protoprox-decoder"
    internal const val HANDLER_NAME = "protoprox-handler"
    internal const val GUARD_NAME = "protoprox-guard"

    internal val ACTIVATED: AttributeKey<Boolean> = AttributeKey.valueOf("protoprox-activated")

    internal val logger = LoggerFactory.getLogger("protoprox")

    // Setter for AbstractChannel's cached remote address, null when Netty doesn't allow access
    private val channelAddressSetter: MethodHandle? =
        try {
            val field = AbstractChannel::class.java.getDeclaredField("remoteAddress")
            MethodHandles.privateLookupIn(AbstractChannel::class.java, MethodHandles.lookup()).unreflectSetter(field)
        } catch (e: Exception) {
            logger.error("Can't update Netty's channel address, ProtoProx will refuse all connections", e)
            null
        }

    // Called by the Connection mixins at the start of Connection.channelActive.
    // Returns true when channelActive has to wait for the PROXY header.
    @JvmStatic
    fun install(ctx: ChannelHandlerContext): Boolean {
        val channel = ctx.channel()
        // only TCP connections accepted by the server, and not the replay from ActivationGuard
        if (channel !is SocketChannel || channel.parent() == null || channel.hasAttr(ACTIVATED)) return false

        // refuse connections when Netty's address can't be set
        if (channelAddressSetter == null) {
            ctx.close()
            return false
        }

        addHandlers(ctx)
        logger.debug("Proxy protocol handlers added to channel {}", channel)
        return true
    }

    // connection: Connection's context
    internal fun addHandlers(connection: ChannelHandlerContext) {
        val guard = ActivationGuard(connection)
        connection
            .pipeline()
            .addBefore(connection.name(), GUARD_NAME, guard)
            .addFirst(HANDLER_NAME, ProxyProtocolHandler(guard))
            .addFirst(DECODER_NAME, HAProxyMessageDecoder())
    }

    // Makes Channel.remoteAddress() return the client address
    internal fun setChannelAddress(channel: Channel, address: SocketAddress) {
        channelAddressSetter?.invoke(channel as AbstractChannel, address)
    }
}
