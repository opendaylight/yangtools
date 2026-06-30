/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
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
 * DTO containing all the linkage information which needs to be supplied to a RootStatementContext. This info will be
 * used to construct linkage substatements like imports, includes, belongs-to etc...
*/
@NonNullByDefault
public abstract sealed class ResolvedSourceInfo {
    /**
     * A {@link ResolvedSourceInfo} for a {@link SourceInfoRef.OfModule}.
     */
    public static final class Module extends ResolvedSourceInfo {
        private final SourceInfoRef.OfModule infoRef;

        Module(final SourceInfoRef.OfModule infoRef, final List<ResolvedImport> imports,
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
            return obj == this || obj instanceof Module other && infoRef.equals(other.infoRef);
        }
    }

    /**
     * A {@link ResolvedSourceInfo} for a {@link SourceInfoRef.OfSubmodule}.
     */
    public static final class Submodule extends ResolvedSourceInfo {
        private final SourceInfoRef. OfSubmodule infoRef;
        private final ResolvedBelongsTo belongsTo;

        Submodule(final SourceInfoRef.OfSubmodule infoRef, final ResolvedBelongsTo belongsTo,
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
            return obj == this || obj instanceof Submodule other
                && infoRef.equals(other.infoRef) && belongsTo.equals(other.belongsTo);
        }

        @Override
        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("belongsTo", belongsTo);
        }
    }

    private final List<ResolvedImport> imports;
    private final List<ResolvedInclude> includes;

    private ResolvedSourceInfo(final List<ResolvedImport> imports, final List<ResolvedInclude> includes) {
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
}
