/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.spi.stmt.SchemaNodeIdentifierParser.ModuleResolver;

/**
 * A {@link ModuleResolver} operating on a {@link StmtContext}. This class is NOT thread-safe.
 *
 * @since 14.0.22
 */
// FIXME: 15.0.0: relocate this class once we expose SchemaNodeIdentifierParser from StmtContext
@Beta
@NonNullByDefault
public final class StmtContextModuleResolver implements ModuleResolver {
    private final RootStmtContext<?, ?, ?> context;

    public StmtContextModuleResolver(final RootStmtContext<?, ?, ?> context) {
        this.context = requireNonNull(context);
    }

    @Override
    public QNameModule currentModule() {
        return context.definingModule();
    }

    @Override
    public @Nullable QNameModule lookupModule(final Unqualified prefix) {
        return StmtContextUtils.getModuleQNameByPrefix(context, prefix.getPrefix());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("context", context).toString();
    }
}
