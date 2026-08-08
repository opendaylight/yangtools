/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.OperationArchetype;

/**
 * Common interface for run-time types associated with invokable operations, such as those defined by {@code action} and
 * {@code rpc} statements.
 */
@NonNullByDefault
public sealed interface OperationRuntimeType extends CompositeRuntimeType permits ActionRuntimeType, RpcRuntimeType {
    @Override
    OperationArchetype javaType();
}
