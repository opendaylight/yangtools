/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract base class for an {@link Implementation} of a method.
 */
@NonNullByDefault
abstract class MethodImplementation implements Implementation {
    final TypeDescription retType;
    // getFoo, usually
    final String methodName;

    MethodImplementation(final String methodName, final TypeDescription retType) {
        this.methodName = requireNonNull(methodName);
        this.retType = requireNonNull(retType);
    }
}