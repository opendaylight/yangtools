/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Base interface for all run-time type information about a particular {@link Type}.
 */
public interface RuntimeType extends Immutable {
    /**
     * Java type associated with this run-time type.
     *
     * @return Java Type
     */
    @NonNull Type javaType();

    /**
     * Return the {@link EffectiveStatement} associated with this run-time type.
     *
     * @return Effective statement
     */
    @NonNull EffectiveStatement<?, ?> statement();
}
