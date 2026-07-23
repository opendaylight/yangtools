/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;

/**
 * A {@link ResolvedSourceInfo} for a {@link SourceInfoRef.OfModule}.
 */
@NonNullByDefault
public final class ResolvedModuleInfo extends ResolvedSourceInfo {
    private final SourceInfoRef.OfModule infoRef;

    ResolvedModuleInfo(final SourceInfoRef.OfModule infoRef, final List<ResolvedImport> imports,
            final List<ResolvedInclude> includes) {
        super(imports, includes);
        this.infoRef = requireNonNull(infoRef);
    }

    @Override
    public SourceInfoRef.OfModule infoRef() {
        return infoRef;
    }

    @Override
    public Unqualified prefix() {
        return infoRef.info().prefix();
    }

    @Override
    public QNameModule definingModule() {
        return infoRef.info().moduleName().getModule();
    }

    @Override
    public int hashCode() {
        return infoRef.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof ResolvedModuleInfo other && infoRef.equals(other.infoRef);
    }
}