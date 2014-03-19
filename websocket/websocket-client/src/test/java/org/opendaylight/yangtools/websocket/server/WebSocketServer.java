/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.opendaylight.yangtools.websocket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.net.InetSocketAddress;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
/**
 * A HTTP server which serves Web Socket requests at:
 *
 * http://localhost:8080/websocket
 *
 * Open your browser at http://localhost:8080/, then the demo page will be loaded and a Web Socket connection will be
 * made automatically.
 *
 * This server illustrates support for the different web socket specification versions and will work with:
 *
 * <ul>
 * <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Firefox 11+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * </ul>
 */
public class WebSocketServer implements Runnable {

    /**
     * Simple Future implementation to allow the retrival of the port number
     * used by the server when a random port is assigned. Essentially the
     * port number is not known until after the server is started and as
     * such clients that request the port before the server is start must
     * wait.
     */
    private class AssignedPort implements Future<Integer> {

        /**
         * The value, null is used to indicate the value has not been set
         */
        private Integer value = null;

        /**
         * Cancel is not supported in this implementation
         */
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }

        /*
         * Wait until the value is set, then return it
         */
        @Override
        public Integer get() throws InterruptedException, ExecutionException {
            synchronized (this) {
                if (value == null) {
					this.wait();
                }
            }
            return value;
        }

        /*
         * Wait for a specified time and then return the value
         */
        @Override
        public Integer get(long timeout, TimeUnit unit) throws  InterruptedException,
             ExecutionException, TimeoutException {
            synchronized (this) {
                if (value == null) {
					this.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));

                    // If after the wait returns the value is still null, then
                    // the wait timed out, so throw a time out exception.
                    if (value == null) {
                        throw new TimeoutException();
                    }
                }
            }
            return value;
        }

        /**
         * Cancel is not supported
         */
        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return value != null;
        }

        /**
         * Assigns a value ot the future and notifies anyone waiting on that
         * value.
         * @param value the value for the future
         */
        void setValue(Integer value) {
            synchronized (this) {
                this.value = value;
                this.notifyAll();
            }
        }
    }

    private AssignedPort port;
    private final int inPort;
    private final ServerBootstrap bootstrap = new ServerBootstrap();
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class.toString());


    public WebSocketServer(int inPort) {
        this.inPort = inPort;
        port = new AssignedPort();
    }

    public void run(){
        try {
            startServer();
        } catch (Exception e) {
            logger.info("Exception occured while starting webSocket server {}",e);
        }
    }

    public Future<Integer> getPort() {
        return port;
    }

    public void startServer() throws Exception {
        try {
            bootstrap.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new WebSocketServerInitializer());

            Channel ch = bootstrap.bind(inPort).sync().channel();
            SocketAddress localSocket = ch.localAddress();
            if (localSocket instanceof InetSocketAddress) {
                port.setValue(((InetSocketAddress) localSocket).getPort());
            }
            logger.info("Web socket server started at port " + port.get() + '.');
            logger.info("Open your browser and navigate to http://localhost:" + port.get() + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


}
