/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * A piece of YANG-modeled data, as defined by <a href="https://www.rfc-editor.org/rfc/rfc8040">RESTCONF</a>
 * {@code yang-data extension}.
 *
 * <p>
 * This is quite similar to a {@link DataObject}, but it cannot be directly stored in a data store. Its data subtree may
 * actually have a relationship to a data store subtree -- but that part is dependent on the context in which this
 * object is used.
 *
 * <p>
 * As per the extension definition, the structure of children is such that there is a single {@code container} which
 * represents the content. This is variadic and may contain, for example, a modeling indirection through a number of
 * {@code choice} and {@code case} statements. Unlike a {@link DataObject}, though, this construct is not subject to
 * {@code augment} statements nor any sort of extensibility.
 *
 * <p>
 * Furthermore {@code rc:yang-data} can only appear as a top-level module contract and therefore the code generation is
 * limited to a single interface, which only provides the default implementation of {@link #implementedInterface()}
 * bound to itself.
 *
 * @param <T> Generated interface type
 */
public non-sealed interface YangData<T extends YangData<T>> extends BindingObject, DataContainer {
    @Override
    Class<T> implementedInterface();
}
