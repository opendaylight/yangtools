/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.tree;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.AbstractExplicitGenerator;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * An object reflecting a YANG {@code schema node}.
 *
 * @param <S> Concrete {@link SchemaTreeEffectiveStatement} type
 * @param <G> Concrete {@link AbstractExplicitGenerator} type
 */
// FIXME: do not reference Generator once we have the codegen and runtime views well-defined
public interface SchemaTreeChild<S extends SchemaTreeEffectiveStatement<?>,
        G extends AbstractExplicitGenerator<S> & SchemaTreeChild<S, G>> extends Identifiable<QName> {
    @Override
    default QName getIdentifier() {
        return statement().argument();
    }

    /**
     * Return the effective YANG statement being represented by this object.
     *
     * @return A YANG statement
     */
    @NonNull S statement();

    /**
     * Return the generator responsible for handling the binding type view of this statement. Note that the statement
     * returned by {@code generator().statement()} may differ from the statement returned by {@link #statement()}.
     *
     * @return Underlying binding generator
     * @throws IllegalStateException if the generator has not been resolved yet
     */
    @NonNull G generator();
}
