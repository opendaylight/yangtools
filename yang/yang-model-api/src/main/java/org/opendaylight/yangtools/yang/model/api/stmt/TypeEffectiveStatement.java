/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

public interface TypeEffectiveStatement<T extends TypeStatement> extends EffectiveStatement<String, T> {
    /**
     * Create an effective statement which extends this particular type and is backed by a different statement. That
     * statement is used to define the effective type.
     *
     * @param stmt Backing effective statement
     * @param path SchemaPath of the resulting type
     * @throws NullPointerException if any of the arguments are null.
     */
    @Nonnull TypeEffectiveStatement<T> derive(@Nonnull EffectiveStatement<?, T> stmt, @Nonnull SchemaPath path);
}
