/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Supports building the {@link DeclaredStatement}.
 *
 * @param <A> Argument type
 * @param <D> Class representing declared version of this statement
 */
public interface Declarable<A, D extends DeclaredStatement<A>> {

    /**
     * Builds {@link DeclaredStatement} for statement context.
     */
    @NonNull D declared();
}
