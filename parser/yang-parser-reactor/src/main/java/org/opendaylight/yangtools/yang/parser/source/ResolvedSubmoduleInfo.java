/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.model.spi.source.SourceRef;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedBelongsTo;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;

/**
 * A {@link ResolvedSourceInfo} for a {@link SourceInfoRef.OfSubmodule}.
 */
@NonNullByDefault
public final class ResolvedSubmoduleInfo extends ResolvedSourceInfo {
    private final SourceInfoRef.OfSubmodule infoRef;
    private final ResolvedBelongsTo belongsTo;

    ResolvedSubmoduleInfo(final SourceInfoRef.OfSubmodule infoRef, final ResolvedBelongsTo belongsTo,
            final List<ResolvedImport> imports, final List<ResolvedInclude> includes) {
        super(imports, includes);
        this.infoRef = requireNonNull(infoRef);
        this.belongsTo = requireNonNull(belongsTo);

        final var expectedDep = infoRef.info().belongsTo();
        final var actualDep = belongsTo.dependency();
        if (!expectedDep.equals(actualDep)) {
            throw new VerifyException("Expecting " + expectedDep + " actual " + actualDep);
        }
    }

    @Override
    public SourceInfoRef.OfSubmodule infoRef() {
        return infoRef;
    }

    public SourceRef.ToModule belongsToRef() {
        return belongsTo.sourceRef();
    }

    @Override
    public Unqualified prefix() {
        return belongsTo.dependency().prefix();
    }

    @Override
    public QNameModule definingModule() {
        return belongsTo.parentModuleQname();
    }

    @Override
    public int hashCode() {
        return infoRef.hashCode() + belongsTo.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof ResolvedSubmoduleInfo other
            && infoRef.equals(other.infoRef) && belongsTo.equals(other.belongsTo);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("belongsTo", belongsTo);
    }
}