/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.websocket.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import org.opendaylight.yangtools.websocket.client.callback.ClientMessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WebSocketClientHandler} is implementation of
 * {@link SimpleChannelInboundHandler} which handle {@link TextWebSocketFrame},
 * {@link PongWebSocketFrame} and {@link CloseWebSocketFrame} messages.
 *
 * @deprecated This code is deprecated without replacement.
 */
@Deprecated
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClientHandler.class.toString());
    private final WebSocketClientHandshaker handshaker;
    private final ClientMessageCallback messageListener;

    private ChannelPromise handshakeFuture;

    /**
     * Create new Web Socket Client Handler.
     *
     * @param handshaker
     *            manages handshake process
     * @param listener
     *
     *
     */
    public WebSocketClientHandler(final WebSocketClientHandshaker handshaker, final ClientMessageCallback listener) {
        this.handshaker = handshaker;
        this.messageListener = listener;
    }

    /**
     * Notifies by Future when handshake process succeeds or fails.
     *
     * @return information about the completation of the handshake
     */
    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final Object msg)
            throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            LOGGER.info("WebSocket Client connected!");
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new RuntimeException(
                    "Unexpected FullHttpResponse (getStatus="
                            + response.getStatus() + ", content="
                            + response.content().toString(CharsetUtil.UTF_8)
                            + ')');
        }

        messageListener.onMessageReceived(msg);
        WebSocketFrame frame = (WebSocketFrame) msg;

        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            LOGGER.info("WebSocket Client received message: "
                    + textFrame.text());
        } else if (frame instanceof PongWebSocketFrame) {
            LOGGER.info("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            LOGGER.info("WebSocket Client received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause)
            throws Exception {
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
