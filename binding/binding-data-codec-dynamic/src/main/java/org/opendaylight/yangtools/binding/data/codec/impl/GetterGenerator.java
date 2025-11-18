/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import net.bytebuddy.dynamic.DynamicType.Builder;
import org.opendaylight.yangtools.binding.data.codec.impl.ClassGeneratorBridge.BridgeProvider;

/**
 * Abstract base class for generating getter methods. This forms the baseline for implementations of the various
 * {@link BridgeProvider}s.
 */
abstract sealed class GetterGenerator implements BridgeProvider permits FixedGetterGenerator, ReusableGetterGenerator {
    /**
     * Generate getter methods for target type.
     *
     * @param <T> target type
     * @param builder target type {@link Builder}
     * @return a {@link Builder}
     */
    abstract <T> Builder<T> generateGetters(Builder<T> builder);
}
