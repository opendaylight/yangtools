/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.source;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

/**
 * The equivalent of {@link StatementDeclaration} when we only have a {@link SourceIdentifier}.
 * @since 15.0.0
 */
@Beta
@NonNullByDefault
public final class DeclarationInSource extends StatementSourceReference implements DeclarationReference {
    private final SourceIdentifier sourceId;

    DeclarationInSource(final SourceIdentifier sourceId) {
        this.sourceId = requireNonNull(sourceId);
    }

    @Override
    public StatementOrigin statementOrigin() {
        return StatementOrigin.DECLARATION;
    }

    @Override
    public @NonNull DeclarationReference declarationReference() {
        return this;
    }

    @Override
    public String toHumanReadable() {
        return "somewhere in " + sourceId.toYangFilename();
    }

    @Override
    public String toString() {
        return toHumanReadable();
    }
}
