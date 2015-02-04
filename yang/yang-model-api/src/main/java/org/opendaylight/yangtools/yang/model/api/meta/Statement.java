/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Immutable;

public interface Statement<F extends Statement<F>> extends Immutable, StatementSupplier<F> {

    @Nonnull StatementDefinition statementDefinition();

    /**
     *
     * Returns collection of declared child statements, which is preserves
     * ordering from original source.
     *
     * @return Collection of statements, which were explicitly declared in
     *         source of model.
     */
    Iterable<? extends Statement<?>> declaredSubstatements();


    /**
    *
    * Returns collection of declared child statements, which is preserves
    * ordering from original source.
    *
    * @return Collection of statements, which were explicitly declared in
    *         source of model.
    */
   Iterable<? extends Statement<?>> getEffectiveChildStatements();

    /**
     * Returns this instance
     *
     * @return this instance
     *
     */
    @Override
    public F get();
}
