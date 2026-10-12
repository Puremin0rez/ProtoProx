package com.dethemium.protoprox

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelInboundHandlerAdapter
import java.net.InetSocketAddress

// Sits right before Connection and holds back its channelActive until the PROXY header is read.
// Any other event before that activates it first.
internal class ActivationGuard(private val connection: ChannelHandlerContext) : ChannelInboundHandlerAdapter() {
    private var activated = false

    // Runs Connection.channelActive once, with the client address when the header had one
    fun activate(address: InetSocketAddress?) {
        if (activated) return
        activated = true

        val channel = connection.channel()
        if (address != null) ProtoProx.setChannelAddress(channel, address)
        channel.attr(ProtoProx.ACTIVATED).set(true)
        connection.pipeline().remove(this)
        (connection.handler() as ChannelInboundHandler).channelActive(connection)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        activate(null)
        ctx.fireChannelRead(msg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        activate(null)
        ctx.fireChannelInactive()
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        activate(null)
        ctx.fireUserEventTriggered(evt)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        activate(null)
        ctx.fireExceptionCaught(cause)
    }
}
