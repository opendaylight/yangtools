/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;
import org.immutables.value.Value;
import org.slf4j.Logger;

/**
 * Builder for {@link ThreadFactory}. Easier to use than the
 * {@link ThreadFactoryBuilder}, because it enforces setting all required
 * properties through a staged builder.
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Value.Style(stagedBuilder = true, allowedClasspathAnnotations = { Override.class,
        SuppressWarnings.class, SuppressFBWarnings.class,
        NotThreadSafe.class, Immutable.class,
        Generated.class })
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
        ThreadFactoryBuilder guavaBuilder = new ThreadFactoryBuilder();
        guavaBuilder.setNameFormat(namePrefix() + "-%d");
        guavaBuilder.setUncaughtExceptionHandler(LoggingThreadUncaughtExceptionHandler.toLogger(logger()));
        guavaBuilder.setDaemon(daemon());
        priority().ifPresent(guavaBuilder::setPriority);
        logger().info("ThreadFactory created: {}", namePrefix());
        return guavaBuilder.build();
    }
}
