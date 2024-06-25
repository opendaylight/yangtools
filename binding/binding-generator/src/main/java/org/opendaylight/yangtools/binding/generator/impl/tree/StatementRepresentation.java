/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.tree;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * An object representing a view on a particular {@link EffectiveStatement}.
 *
 * @param <S> Statement type
 */
public interface StatementRepresentation<S extends EffectiveStatement<?, ?>> {
    /**
     * Return the effective YANG statement being represented by this object.
     *
     * @return A YANG statement
     */
    @NonNull S statement();
}
