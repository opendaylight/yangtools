/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;

/**
 * An opaque object. This interface supports code generation for both {@code anyxml} and {@code anydata}. Both of these
 * statements essentially share the same characteristic of storing data whose actual schema and representation is not
 * known at compile-time. Schema may be unknown even at runtime, and furthermore the representation may vary during
 * run-time, based on source of the data.
 *
 * <p>
 * The code generation is therefore limited to a single interface, which only provides the default implementation
 * of {@link #implementedInterface()} bound to itself. The value is communicated through {@link #getValue()}, which
 * is only an encapsulation holding information about the object model and the data in that object model.
 *
 * <p>
 * Implementations are strongly encouraged to use {@link AbstractOpaqueObject} as their base implementation class.
 *
 * @param <T> Generated interface
 */
@Beta
public interface OpaqueObject<T extends OpaqueObject<T>> extends BindingObject, DataContainer,
        ValueAware<OpaqueData<?>> {
    @Override
    Class<T> implementedInterface();

    /**
     * Hash code value of this object. This is a combination of {@link #implementedInterface()} and the value being
     * held. The canonical computation algorithm is defined in {@link AbstractOpaqueObject#hashCode()}.
     *
     * @return a hash code value for this object.
     */
    @Override
    int hashCode();

    /**
     * Compare this object to another object. The comparison needs to take into account {@link #implementedInterface()}
     * first and then follow comparison on {@link #getValue()}. For canonical algorithm please refer to
     * {@link AbstractOpaqueObject#equals(Object)}.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    boolean equals(Object obj);
}
