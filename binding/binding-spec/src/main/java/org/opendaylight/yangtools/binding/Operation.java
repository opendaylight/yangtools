/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * Common interface capturing the contract of being an operation that can be invoked.
 *
 * @since 16.0.0
 */
public sealed interface Operation<T extends Operation<T>> extends BindingContract<T> permits Action, Rpc {
    // nothing else
}
