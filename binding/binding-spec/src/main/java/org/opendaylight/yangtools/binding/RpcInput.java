/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * Marker interface for all interfaces generated for {@code input} statement within an {@code action} or an {@code rpc}
 * statement.
 *
 * @param <T> concrete {@link RpcOutput} type
 */
// FIXME: YANGTOOLS-1921: not DataObject
public non-sealed interface RpcInput<T extends RpcInput<T>> extends Augmentable<T>, DataObject, ParentObject<T> {
    // nothing else
}
