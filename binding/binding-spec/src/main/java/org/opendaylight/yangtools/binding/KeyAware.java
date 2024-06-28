/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Identifiable object, which could be identified by it's key.
 *
 * @param <T> Identifier class for this object
 */
public sealed interface KeyAware<T extends Key<? extends EntryObject<?, T>>>
        permits EntryObject, DataObjectReference.WithKey, KeyStep {
    /**
     * Returns an unique key for the object.
     *
     * @return Key for the object
     */
    @NonNull T key();
}
