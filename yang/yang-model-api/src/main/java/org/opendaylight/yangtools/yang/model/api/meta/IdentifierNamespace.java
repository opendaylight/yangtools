/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Model specific namespace which allows access to specific
 *
 * {@link IdentifierNamespace} serves as common superclass for YANG model namespaces, which are type-captured
 * subclasses. This type capture of namespace allows for handy type-safe reading methods such as
 * {@link EffectiveStatement#get(Class, Object)} and still allows introduction of new namespaces without need to change
 * model APIs.
 *
 * @param <K> Identifier type
 * @param <V> Value type
 */
public interface IdentifierNamespace<K, V> {
    /**
     * Returns value associated with supplied identifier.
     *
     * @param identifier Identifier of value
     * @return value or null, if identifier is not present in namespace.
     */
    @Nullable V get(@NonNull K identifier);
}
