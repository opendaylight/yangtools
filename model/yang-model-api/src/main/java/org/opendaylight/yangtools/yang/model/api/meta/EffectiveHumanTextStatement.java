/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Common interface for {@link EffectiveStatement}s whose argument represents text meant to be understandable by humans.
 * This includes, for example, {@code contact}, {@code description}, {@code error-message}, {@code organization} and
 * {code reference} statements.
 *
 * @see DeclaredHumanTextStatement
 */
public interface EffectiveHumanTextStatement<D extends DeclaredHumanTextStatement>
        extends EffectiveStatement<String, D> {
    /**
     * {@return the text}
     */
    default @NonNull String text() {
        return argument();
    }
}
