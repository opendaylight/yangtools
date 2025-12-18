/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.HashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.spi.stmt.SchemaNodeIdentifierParser.ModuleResolver;
import org.opendaylight.yangtools.yang.parser.spi.meta.RootStmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

/**
 * A {@link ModuleResolver} operating on a {@link RootStmtContext}. This class is NOT thread-safe.
 */
final class StmtContextModuleResolver implements ModuleResolver {
    private final @NonNull HashMap<@NonNull String, QNameModule> modules = new HashMap<>();
    private final @NonNull RootStmtContext<?, ?, ?> context;

    private QNameModule currentModule;

    @NonNullByDefault
    StmtContextModuleResolver(final RootStmtContext<?, ?, ?> context) {
        this.context = requireNonNull(context);
    }

    @Override
    public QNameModule currentModule() {
        var module = currentModule;
        if (module == null) {
            currentModule = module = verifyNotNull(context.definingModule());
        }
        return module;
    }

    @Override
    public QNameModule lookupModule(final Unqualified prefix) {
        final var str = prefix.getLocalName();
        final var module = modules.get(str);
        return module != null ? module : lookupModule(str);
    }

    private @Nullable QNameModule lookupModule(final @NonNull String prefix) {
        final var module = StmtContextUtils.getModuleQNameByPrefix(context, prefix);
        if (module != null) {
            modules.put(prefix, module);
        }
        return module;
    }

    @Override
    public String toString() {
        int cached = modules.size();
        if (currentModule != null) {
            cached++;
        }
        return MoreObjects.toStringHelper(this).add("context", context).add("cached", cached).toString();
    }
}
