/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

/**
 * A {@link ContainerNode} backed by a binding {@link DataObject}, with lazy instantiation of the ContainerNode view.
 *
 * @param <T> Binding DataObject type
 * @author Robert Varga
 */
@Beta
public interface BindingLazyContainerNode<T extends DataObject> extends ContainerNode, Delegator<ContainerNode> {
    /**
     * Returns the underlying DataObject.
     *
     * @return underlying DataObject.
     */
    @NonNull T getDataObject();
}
