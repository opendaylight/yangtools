/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.parser.api;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A factory for creating {@link YangContextParser} instances. An implementation of this interface is expected to
 * be acquired via the container's service injection mechanisms, such as the OSGi Service Registry, or
 * {@link java.util.ServiceLoader}.
 *
 * Implementations of this interface are expected to be thread-safe.
 */
@Beta
@ThreadSafe
public interface YangParserFactory {
    /**
     * Create a {@link YangContextParser}. If the resulting implementation is not thread-safe, this method must allocate
     * a new instance. Thread-safe {@link YangContextParser} implementations may be reused by handing out a singleton
     * instance from this method.
     *
     * @return A YangContextParser instance.
     */
    @Nonnull YangContextParser createYangContextParser();
}
