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
 * @param <T> Generated interface
 */
@Beta
public interface OpaqueObject<T extends OpaqueObject<T>> extends BindingObject, DataContainer,
        ValueAware<OpaqueData<?>> {
    @Override
    Class<T> implementedInterface();
}
