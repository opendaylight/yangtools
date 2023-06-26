/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Common interface for run-time types associated with invokable operations, such as those defined by {@code action} and
 * {@code rpc} statements.
 */
public interface InvokableRuntimeType extends CompositeRuntimeType {
    /**
     * Return the run-time type for this action's input.
     *
     * @return Input run-time type
     */
    @NonNull InputRuntimeType input();

    /**
     * Return the run-time type for this action's output.
     *
     * @return Output run-time type
     */
    @NonNull OutputRuntimeType output();
}
