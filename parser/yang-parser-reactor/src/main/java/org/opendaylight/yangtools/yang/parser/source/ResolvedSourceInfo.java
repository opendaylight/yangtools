/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedImport;
import org.opendaylight.yangtools.yang.parser.source.ResolvedDependency.ResolvedInclude;

/**
 * DTO containing all the linkage information which needs to be supplied to a RootStatementContext. This info will be
 * used to construct linkage substatements like imports, includes, belongs-to etc...
*/
@NonNullByDefault
public abstract sealed class ResolvedSourceInfo implements Immutable permits ResolvedModuleInfo, ResolvedSubmoduleInfo {
    private final List<ResolvedImport> imports;
    private final List<ResolvedInclude> includes;

    ResolvedSourceInfo(final List<ResolvedImport> imports, final List<ResolvedInclude> includes) {
        this.imports = List.copyOf(imports);
        this.includes = List.copyOf(includes);
    }

    public abstract SourceInfoRef infoRef();

    public abstract QNameModule definingModule();

    public abstract Unqualified prefix();

    public final List<ResolvedImport> imports() {
        return imports;
    }

    public final List<ResolvedInclude> includes() {
        return includes;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("infoRef", infoRef());
    }

    /**
     * A builder of {@link ResolvedSourceInfo} instances.
     */
    abstract static sealed class Builder implements Mutable permits SourceLinker {

        final String humanName() {
            final var sourceId = sourceId();
            return humanName(sourceId.name(), sourceId.revision());
        }

        static final String humanName(final Unqualified name, final @Nullable Revision revision) {
            final var localName = name.getLocalName();
            return revision == null ? localName : localName + "@" + revision;
        }

        final Unqualified name() {
            return sourceId().name();
        }

        final @Nullable Revision revision() {
            return sourceId().revision();
        }

        final SourceIdentifier sourceId() {
            return sourceInfo().sourceId();
        }

        final YangVersion yangVersion() {
            return sourceInfo().yangVersion();
        }

        /**
         * {@return the {@link SourceInfoRef} for which this builder was instantiated}
         */
        abstract SourceInfoRef infoRef();

        abstract SourceInfo sourceInfo();

        /**
         * {@return the {@link ResolvedSourceInfo} result of this builder}
         */
        abstract ResolvedSourceInfo build();

        @Override
        public abstract String toString();
    }
}
