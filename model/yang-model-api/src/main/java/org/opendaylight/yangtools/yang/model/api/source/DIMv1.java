/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.source;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;

/**
 * Serialization proxy for {@link Import}.
 *
 * @since 14.0.23
 */
@NonNullByDefault
record DIMv1(
        Unqualified name,
        Unqualified prefix,
        @Nullable Revision revision,
        @Nullable StatementSourceReference sourceRef) implements Serializable {
    DIMv1 {
        requireNonNull(name);
        requireNonNull(prefix);
    }

    DIMv1(final Import obj) {
        this(obj.name(), obj.prefix(), obj.revision(), DBTv1.serializableRef(obj.sourceRef()));
    }

    @java.io.Serial
    Object readResolve() {
        return new Import(name, prefix, revision, sourceRef);
    }
}
