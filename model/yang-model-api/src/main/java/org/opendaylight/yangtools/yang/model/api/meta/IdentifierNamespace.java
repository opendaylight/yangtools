/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Common base class for various YANG statement namespaces.
 *
 * @param <K> Identifier type
 * @param <V> Value type
 */
// FIXME: make this class final and switch addressing to using objects instances instead of
//        Class<? extends IdentifierNamespace>
// FIXME: also consider renaming this to a friendlier name, like YangNamespace or similar
@NonNullByDefault
public abstract class IdentifierNamespace<K, V> {
    protected IdentifierNamespace() {
        throw new UnsupportedOperationException(getClass() + " should never be instantiated");
    }
}
