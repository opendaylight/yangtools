/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import org.immutables.value.Value;
import org.slf4j.Logger;

/**
 * Builder for {@link ThreadFactory}. Easier to use than Guava's {@code ThreadFactoryBuilder}, because it enforces
 * setting all required properties through a staged builder.
 *
 * @deprecated Java provides more powerful ways of creating {@link ThreadFactory} via {@link Thread#ofPlatform()} and
 *             {@link Thread#ofVirtual()} builders, please use those instead.
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Value.Style(stagedBuilder = true, allowedClasspathAnnotations = { SuppressWarnings.class })
@Deprecated(since = "14.0.16", forRemoval = true)
public abstract class ThreadFactoryProvider {

    // This class is also available in infrautils (but yangtools cannot depend on infrautils)
    // as org.opendaylight.infrautils.utils.concurrent.ThreadFactoryProvider

    public static ImmutableThreadFactoryProvider.NamePrefixBuildStage builder() {
        return ImmutableThreadFactoryProvider.builder();
    }

    /**
     * Prefix for threads from this factory. For example, "rpc-pool", to create
     * "rpc-pool-1/2/3" named threads. Note that this is a prefix, not a format,
     * so you pass just "rpc-pool" instead of e.g. "rpc-pool-%d".
     */
    @Value.Parameter public abstract String namePrefix();

    /**
     * Logger used to log uncaught exceptions from new threads created via this factory.
     */
    @Value.Parameter public abstract Logger logger();

    /**
     * Priority for new threads from this factory.
     */
    @Value.Parameter public abstract Optional<Integer> priority();

    /**
     * Daemon or not for new threads created via this factory.
     * <b>NB: Defaults to true.</b>
     */
    @Value.Default public boolean daemon() {
        return true;
    }

    public ThreadFactory get() {
        final var builder = Thread.ofPlatform()
            .name(namePrefix() + "-", 0)
            .uncaughtExceptionHandler((thread, exception)
                -> logger().error("Thread terminated due to uncaught exception: {}", thread.getName(), exception))
            .daemon(daemon());

        priority().ifPresent(builder::priority);
        logger().info("ThreadFactory created: {}", namePrefix());
        return builder.factory();
    }
}
