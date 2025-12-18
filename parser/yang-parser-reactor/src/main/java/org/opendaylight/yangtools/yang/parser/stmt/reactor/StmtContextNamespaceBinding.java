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
<<<<<<<< HEAD:parser/yang-parser-spi/src/main/java/org/opendaylight/yangtools/yang/parser/spi/meta/StmtContextNamespaceBinding.java
import org.opendaylight.yangtools.yang.model.spi.stmt.NamespaceBinding;
|||||||| parent of 0e1775ed9cc (WIP: Add StmtContext.schemaNodeIdentifierParser()):parser/yang-parser-spi/src/main/java/org/opendaylight/yangtools/yang/parser/spi/meta/StmtContextModuleResolver.java
import org.opendaylight.yangtools.yang.model.spi.stmt.SchemaNodeIdentifierParser.ModuleResolver;
========
import org.opendaylight.yangtools.yang.model.spi.stmt.SchemaNodeIdentifierParser.ModuleResolver;
import org.opendaylight.yangtools.yang.parser.spi.meta.RootStmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
>>>>>>>> 0e1775ed9cc (WIP: Add StmtContext.schemaNodeIdentifierParser()):parser/yang-parser-reactor/src/main/java/org/opendaylight/yangtools/yang/parser/stmt/reactor/StmtContextModuleResolver.java

/**
<<<<<<<< HEAD:parser/yang-parser-spi/src/main/java/org/opendaylight/yangtools/yang/parser/spi/meta/StmtContextNamespaceBinding.java
 * A {@link NamespaceBinding} operating on a {@link StmtContext}. This class is NOT thread-safe.
 *
 * @since 14.0.22
|||||||| parent of 0e1775ed9cc (WIP: Add StmtContext.schemaNodeIdentifierParser()):parser/yang-parser-spi/src/main/java/org/opendaylight/yangtools/yang/parser/spi/meta/StmtContextModuleResolver.java
 * A {@link ModuleResolver} operating on a {@link StmtContext}. This class is NOT thread-safe.
 *
 * @since 14.0.22
========
 * A {@link ModuleResolver} operating on a {@link RootStmtContext}. This class is NOT thread-safe.
>>>>>>>> 0e1775ed9cc (WIP: Add StmtContext.schemaNodeIdentifierParser()):parser/yang-parser-reactor/src/main/java/org/opendaylight/yangtools/yang/parser/stmt/reactor/StmtContextModuleResolver.java
 */
<<<<<<<< HEAD:parser/yang-parser-spi/src/main/java/org/opendaylight/yangtools/yang/parser/spi/meta/StmtContextNamespaceBinding.java
// FIXME: 15.0.0: relocate this class once we expose CommontArgumentParsers from StmtContext
@Beta
public final class StmtContextNamespaceBinding implements NamespaceBinding {
|||||||| parent of 0e1775ed9cc (WIP: Add StmtContext.schemaNodeIdentifierParser()):parser/yang-parser-spi/src/main/java/org/opendaylight/yangtools/yang/parser/spi/meta/StmtContextModuleResolver.java
// FIXME: 15.0.0: relocate this class once we expose SchemaNodeIdentifierParser from StmtContext
@Beta
public final class StmtContextModuleResolver implements ModuleResolver {
========
final class StmtContextModuleResolver implements ModuleResolver {
>>>>>>>> 0e1775ed9cc (WIP: Add StmtContext.schemaNodeIdentifierParser()):parser/yang-parser-reactor/src/main/java/org/opendaylight/yangtools/yang/parser/stmt/reactor/StmtContextModuleResolver.java
    private final @NonNull HashMap<@NonNull String, QNameModule> modules = new HashMap<>();
    private final @NonNull RootStmtContext<?, ?, ?> context;

    private QNameModule currentModule;

    @NonNullByDefault
<<<<<<<< HEAD:parser/yang-parser-spi/src/main/java/org/opendaylight/yangtools/yang/parser/spi/meta/StmtContextNamespaceBinding.java
    public StmtContextNamespaceBinding(final RootStmtContext<?, ?, ?> context) {
|||||||| parent of 0e1775ed9cc (WIP: Add StmtContext.schemaNodeIdentifierParser()):parser/yang-parser-spi/src/main/java/org/opendaylight/yangtools/yang/parser/spi/meta/StmtContextModuleResolver.java
    public StmtContextModuleResolver(final RootStmtContext<?, ?, ?> context) {
========
    StmtContextModuleResolver(final RootStmtContext<?, ?, ?> context) {
>>>>>>>> 0e1775ed9cc (WIP: Add StmtContext.schemaNodeIdentifierParser()):parser/yang-parser-reactor/src/main/java/org/opendaylight/yangtools/yang/parser/stmt/reactor/StmtContextModuleResolver.java
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
