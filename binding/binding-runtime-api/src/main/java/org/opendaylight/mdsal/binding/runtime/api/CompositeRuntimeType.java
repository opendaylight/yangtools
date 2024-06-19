/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

/**
 * A {@link RuntimeType} which is also a {@link RuntimeTypeContainer}. This is a pure composition interface and does not
 * imply further contract.
 */
public interface CompositeRuntimeType extends GeneratedRuntimeType, RuntimeTypeContainer {
    // Pure contract composition
}
