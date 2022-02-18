/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.generator.impl.tree.SchemaTreeChild;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * A placeholder {@link SchemaTreeChild}.
 *
 */
final class SchemaTreePlaceholder<S extends SchemaTreeEffectiveStatement<?>,
        G extends AbstractExplicitGenerator<S> & SchemaTreeChild<S, G>> implements SchemaTreeChild<S, G> {
    private final @NonNull Class<G> generatorType;
    private final @NonNull S statement;

    private @Nullable G generator;

    SchemaTreePlaceholder(final S statement, final Class<G> generatorType) {
        this.statement = requireNonNull(statement);
        this.generatorType = requireNonNull(generatorType);
    }

    @Override
    public S statement() {
        return statement;
    }

    @Override
    public G generator() {
        final var local = generator;
        if (local == null) {
            throw new IllegalStateException("Unresolved generator in " + this);
        }
        return local;
    }

    void setGenerator(final AbstractCompositeGenerator<?> parent) {
        verify(generator == null, "Attempted to set generator for %s", this);
        final var qname = getIdentifier();
        generator = generatorType.cast(verifyNotNull(parent.findSchemaTreeGenerator(qname),
            "Failed to find generator for child %s in %s", qname, parent));
    }
}
