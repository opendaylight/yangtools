/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.impl.ConcreteTypeImpl;

/**
 * Central mapping of types.
 */
// FIXME: YANGTOOLS-1910: these are used to MethodSignature.getReturnType and should be properly modeled there
public final class Types {
    private static final @NonNull ConcreteType LIST_TYPE = new ConcreteTypeImpl(List.class);
    private static final @NonNull ParameterizedType LIST_TYPE_WILDCARD = ParameterizedType.of(LIST_TYPE);

    private Types() {
        // hidden on purpose
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed {@link List}&lt;V&gt; with concrete type
     * of value.
     *
     * @param valueType Value Type
     * @return Description of type instance of List
     */
    @NonNullByDefault
    public static ParameterizedType listTypeFor(final Type valueType) {
        return ParameterizedType.of(LIST_TYPE, valueType);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed {@link List}&lt;?&gt;.
     *
     * @return Description of type instance of List
     */
    public static @NonNull ParameterizedType listTypeWildcard() {
        return LIST_TYPE_WILDCARD;
    }

    public static boolean isListType(final ParameterizedType type) {
        return LIST_TYPE.equals(type.getRawType());
    }
}
