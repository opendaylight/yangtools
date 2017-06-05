/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents declared statement.
 *
 * @param <A> Argument type ({@link Void} if statement does not have argument.)
 */
public interface DeclaredStatement<A> extends ModelStatement<A> {

    /**
     * Returns statement argument as was present in original source.
     *
     * @return statement argument as was present in original source or null, if statement does not take argument.
     */
    @Nullable String rawArgument();

    /**
     * Returns collection of explicitly declared child statements, while preserving its original
     * ordering from original source.
     *
     * @return Collection of statements, which were explicitly declared in
     *         source of model.
     */
    @Nonnull Collection<? extends DeclaredStatement<?>> declaredSubstatements();
}
