/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Implementation of this interface delegates all its calls to the delegate if not specified otherwise.
 *
 * @param <T> Type of delegate
 */
public interface Delegator<T> {
    /**
     * Return underlying delegate.
     *
     * @return underlying delegate.
     */
    @NonNull T getDelegate();
}
