/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.websocket.client;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import java.net.URI;
import org.opendaylight.yangtools.websocket.client.callback.ClientMessageCallback;

/**
 * Implementation of web socket client that supports WS and HTTP protocols.
 *
 * @deprecated This code is deprecated without replacement.
 */
@Deprecated
public class WebSocketIClient {
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
    private final WebSocketClientHandler clientHandler;
    private final URI uri;
    private Channel clientChannel;

    /**
     * Creates new web socket client
     *
     * @param uri
     *            URI
     * @param clientMessageCallback
     *            ClientMessageCallback
     */
    public WebSocketIClient(final URI uri, final ClientMessageCallback clientMessageCallback) {
        this.uri = Preconditions.checkNotNull(uri);
        clientHandler = new WebSocketClientHandler(
                WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, null),
                clientMessageCallback); // last null could be replaced with DefaultHttpHeaders
        initialize();
    }

    /**
     * Initializes {@link Channel} one when it was registered to its
     * {@link EventLoop}.
     */
    private void initialize() {

        String protocol = uri.getScheme();
        if (!"ws".equals(protocol) && !"http".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: "
                    + protocol);
        }

        bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(final SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("http-codec", new HttpClientCodec());
                        pipeline.addLast("aggregator",
                                new HttpObjectAggregator(8192));
                        pipeline.addLast("ws-handler", clientHandler);
                    }
                });
    }

    /**
     * Makes the connection attempt and notifies when the handshake process
     * succeeds or fail.
     */
    public void connect() throws InterruptedException {
        clientChannel = bootstrap.connect(uri.getHost(), uri.getPort()).sync()
                .channel();
        clientHandler.handshakeFuture().sync();
    }

    /**
     * Writes a String message through the
     * {@link ChannelPipeline} and request to actual {@link Channel#flush()} to flush
     * all pending data to the actual transport.
     *
     * @param message
     *            a message to write
     */
    public void writeAndFlush(final String message) {
        clientChannel.writeAndFlush(new TextWebSocketFrame(message));
    }

    /**
     * Writes a Object message through the
     * {@link ChannelPipeline} and request to actual {@link Channel#flush()} to flush
     * all pending data to the actual transport.
     *
     * @param message
     *            a message to write
     */
    public void writeAndFlush(final Object message) {
        clientChannel.writeAndFlush(message);
    }

    /**
     * Writes {@link PingWebSocketFrame}
     * through the {@link ChannelPipeline} and request to actual
     * {@link Channel#flush()} to flush all pending data to the actual transport.
     */
    public void ping() {
        clientChannel.writeAndFlush(new PingWebSocketFrame(Unpooled
                .copiedBuffer(new byte[] { 1, 2, 3, 4, 5, 6 })));
    }

    /**
     * Closes the connection when the server responds to the
     * {@link CloseWebSocketFrame}.
     */
    public void close() throws InterruptedException {
        clientChannel.writeAndFlush(new CloseWebSocketFrame());
        clientChannel.closeFuture().sync();
        group.shutdownGracefully();
    }

}
