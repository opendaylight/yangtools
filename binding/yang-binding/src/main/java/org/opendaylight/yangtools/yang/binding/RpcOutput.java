/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * Marker interface for all interfaces generated for {@code output} statement within an {@code action} or an {@code rpc}
 * statement.
 */
// FIXME: this should not really be a DataObject, but a separate DataContainer and a BindingObject
public interface RpcOutput extends DataObject {
    @Override
    Class<? extends RpcOutput> implementedInterface();
}
