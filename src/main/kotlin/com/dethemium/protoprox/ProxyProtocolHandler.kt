package com.dethemium.protoprox

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.haproxy.HAProxyCommand
import io.netty.handler.codec.haproxy.HAProxyMessage
import io.netty.handler.codec.haproxy.HAProxyProtocolException
import io.netty.handler.codec.haproxy.HAProxyProxiedProtocol.AddressFamily
import io.netty.util.NetUtil
import java.net.InetSocketAddress

// Reads the client address from the PROXY header and activates the connection with it
internal class ProxyProtocolHandler(private val guard: ActivationGuard) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is HAProxyMessage) {
            super.channelRead(ctx, msg)
            return
        }

        try {
            val address = clientAddress(msg)
            guard.activate(address)
            ProtoProx.logger.debug("Proxy protocol resolved real address: {}", address)
        } finally {
            msg.release()
            ctx.pipeline().remove(this)
        }
    }

    // Null for LOCAL commands and non-IP families, never resolves hostnames
    private fun clientAddress(msg: HAProxyMessage): InetSocketAddress? {
        if (msg.command() != HAProxyCommand.PROXY) return null
        val family = msg.proxiedProtocol().addressFamily()
        if (family != AddressFamily.AF_IPv4 && family != AddressFamily.AF_IPv6) return null
        val ip = NetUtil.createInetAddressFromIpAddressString(msg.sourceAddress() ?: return null) ?: return null
        return InetSocketAddress(ip, msg.sourcePort())
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause is HAProxyProtocolException) {
            ProtoProx.logger.warn(
                "Rejected connection from {}: invalid PROXY protocol header ({})",
                ctx.channel().remoteAddress(),
                generateSequence<Throwable>(cause) { it.cause }.last().message,
            )
        } else {
            ProtoProx.logger.debug("Proxy protocol error: {}", cause.message)
        }
        ctx.close()
    }
}
