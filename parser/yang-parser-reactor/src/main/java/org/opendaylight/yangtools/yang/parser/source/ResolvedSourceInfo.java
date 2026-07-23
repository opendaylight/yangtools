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
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
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
    public abstract static sealed class Builder implements Mutable permits SourceLinker {
        /**
         * {@return the {@link SourceInfoRef} for which this builder was instantiated}
         */
        public abstract SourceInfoRef infoRef();

        /**
         * {@return the equivalent of {@code infoRef().info{}}}
         */
        public abstract SourceInfo sourceInfo();

        /**
         * {@return the equivalent of {@code sourceId().name{}}}
         */
        public final Unqualified name() {
            return sourceId().name();
        }

        /**
         * {@return the equivalent of {@code sourceId().revision{}}}
         */
        public final @Nullable Revision revision() {
            return sourceId().revision();
        }

        /**
         * {@return a human-friendly identifier composed of {@link #name()} and {@link #revision()}}
         */
        public abstract String humanName();

        /**
         * {@return the equivalent of {@code sourceInfo().sourceId{}}}
         */
        public final SourceIdentifier sourceId() {
            return sourceInfo().sourceId();
        }

        /**
         * {@return the equivalent of {@code sourceInfo().yangVersion{}}}
         */
        public final YangVersion yangVersion() {
            return sourceInfo().yangVersion();
        }

        /**
         * {@return the set of {@link Import}s that remain unresolved}
         */
        public abstract Iterator<Import> missingImports();

        /**
         * {@return the set of {@link Include}s that remain unresolved}
         */
        public abstract Iterator<Include> missingIncludes();

        /**
         * {@return the {@link ResolvedSourceInfo} result of this builder}
         */
        public abstract ResolvedSourceInfo build();
    }

    /**
     * A {@link Builder} for {@link ResolvedModuleInfo}.
     */
    public abstract static non-sealed class ModuleBuilder
            extends SourceLinker<SourceInfoRef.OfModule, ResolvedModuleInfo> {
        protected ModuleBuilder(final SourceInfoRef.OfModule infoRef) {
            super(infoRef);
        }

        @Override
        public final SourceInfo.Module sourceInfo() {
            return infoRef().info();
        }
    }

    /**
     * A {@link Builder} for {@link ResolvedSubmoduleInfo}.
     */
    public abstract static non-sealed class SubmoduleBuilder
            extends SourceLinker<SourceInfoRef.OfSubmodule, ResolvedSubmoduleInfo> {
        protected SubmoduleBuilder(final SourceInfoRef.OfSubmodule infoRef) {
            super(infoRef);
        }

        @Override
        public final SourceInfo.Submodule sourceInfo() {
            return infoRef().info();
        }

        /**
         * {@return the module name specified by this submodule through {@link SourceInfo.Submodule#belongsTo()}}
         */
        public final Unqualified parentName() {
            return sourceInfo().belongsTo().name();
        }
    }
}
