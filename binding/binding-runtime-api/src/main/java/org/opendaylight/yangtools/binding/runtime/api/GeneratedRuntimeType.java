/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.api;

import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@link Archetype}. This introduces two avenues of acquiring an
 * {@link EffectiveStatement}.
 * <ol>
 *    <li>the {@link #statement()} method, and</li>
 *    <li>the {@link Archetype#statement()} method</li>
 * </ol>
 * These may not be used interchangeably and for run-time purposes, only this interface's {@link #statement()} method
 * must be used. The {@link Archetype} is provided solely for its information about structural properties of the
 * corresponding Java class and should be ignored as an implementation detail.
 */
public interface GeneratedRuntimeType extends RuntimeType, Identifiable<TypeName> {
    @Override
    Archetype javaType();

    @Override
    default TypeName getIdentifier() {
        return javaType().name();
    }
}
