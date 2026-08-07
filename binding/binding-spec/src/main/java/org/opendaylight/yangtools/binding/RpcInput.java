/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.opendaylight.yangtools.binding.lib.JavaDataContainer;

/**
 * Marker interface for all interfaces generated for {@code input} statement within an {@code action} or an {@code rpc}
 * statement.
 *
 * @param <T> concrete {@link RpcOutput} type
 */
// FIXME: YANGTOOLS-1921: extends ParentObject<T>
public non-sealed interface RpcInput<T extends RpcInput<T>> extends Augmentable<T>, JavaDataContainer<T>, DataObject {
    @Override
    Class<T> implementedInterface();
}
