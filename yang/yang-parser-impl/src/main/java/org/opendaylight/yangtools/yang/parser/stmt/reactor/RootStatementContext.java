/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.IncludedModuleContext;

/**
 * root statement class for a Yang source
 */
public class RootStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> extends
        StatementContextBase<A, D, E> {

    private final SourceSpecificContext sourceContext;
    private final Collection<NamespaceStorageNode> includedContexts = new ArrayList<>();
    private final A argument;

    RootStatementContext(final ContextBuilder<A, D, E> builder, final SourceSpecificContext sourceContext) {
        super(builder);
        this.sourceContext = sourceContext;
        this.argument = builder.getDefinition().parseArgumentValue(this, builder.getRawArgument());
    }

    RootStatementContext(final RootStatementContext<A, D, E> original, final QNameModule newQNameModule,
        final CopyType typeOfCopy) {
        super(original);

        sourceContext = original.sourceContext;
        this.argument = original.argument;

        copyDeclaredStmts(original, newQNameModule, typeOfCopy);

        copyEffectiveStmts(original, newQNameModule, typeOfCopy);

    }

    /**
     * copies declared statements from original to this' substatements
     *
     * @param typeOfCopy
     *            determines whether copy is used by augmentation or uses
     * @throws org.opendaylight.yangtools.yang.parser.spi.source.SourceException
     */
    private void copyDeclaredStmts(final RootStatementContext<A, D, E> original, final QNameModule newQNameModule,
            final CopyType typeOfCopy) {
        final Collection<StatementContextBase<?, ?, ?>> originalDeclaredSubstatements = original.declaredSubstatements();
        for (final StatementContextBase<?, ?, ?> stmtContext : originalDeclaredSubstatements) {
            if (!StmtContextUtils.areFeaturesSupported(stmtContext)) {
                continue;
            }
            this.addEffectiveSubstatement(stmtContext.createCopy(newQNameModule, this, typeOfCopy));
        }
    }

    /**
     * copies effective statements from original to this' substatements
     *
     * @param typeOfCopy
     *            determines whether copy is used by augmentation or uses
     * @throws org.opendaylight.yangtools.yang.parser.spi.source.SourceException
     */
    private void copyEffectiveStmts(final RootStatementContext<A, D, E> original, final QNameModule newQNameModule,
            final CopyType typeOfCopy) {
        final Collection<? extends StmtContext<?, ?, ?>> originalEffectiveSubstatements = original.effectiveSubstatements();
        for (final StmtContext<?, ?, ?> stmtContext : originalEffectiveSubstatements) {
            this.addEffectiveSubstatement(stmtContext.createCopy(newQNameModule, this, typeOfCopy));
        }
    }

    /**
     * @return null as root cannot have parent
     */
    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        return null;
    }

    /**
     * @return namespace storage of source context
     */
    @Override
    public NamespaceStorageNode getParentNamespaceStorage() {
        return sourceContext;
    }

    /**
     * @return registry of source context
     */
    @Override
    public Registry getBehaviourRegistry() {
        return sourceContext;
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.ROOT_STATEMENT_LOCAL;
    }
    /**
     * @return this as its own root
     */
    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return this;
    }

    SourceSpecificContext getSourceContext() {
        return sourceContext;
    }

    @Override
    public A getStatementArgument() {
        return argument;
    }

    /**
     * @return copy of this considering {@link CopyType} (augment, uses)
     *
     * @throws org.opendaylight.yangtools.yang.parser.spi.source.SourceException instance of SourceException
     */
    @Override
    public StatementContextBase<?, ?, ?> createCopy(final StatementContextBase<?, ?, ?> newParent,
            final CopyType typeOfCopy) {
        return createCopy(null, newParent, typeOfCopy);
    }

    /**
     * @return copy of this considering {@link CopyType} (augment, uses)
     *
     * @throws org.opendaylight.yangtools.yang.parser.spi.source.SourceException instance of SourceException
     */
    @Override
    public StatementContextBase<A, D, E> createCopy(final QNameModule newQNameModule,
            final StatementContextBase<?, ?, ?> newParent, final CopyType typeOfCopy) {
        final RootStatementContext<A, D, E> copy = new RootStatementContext<>(this, newQNameModule, typeOfCopy);

        copy.appendCopyHistory(typeOfCopy, this.getCopyHistory());

        if (this.getOriginalCtx() != null) {
            copy.setOriginalCtx(this.getOriginalCtx());
        } else {
            copy.setOriginalCtx(this);
        }
        definition().onStatementAdded(copy);
        return copy;
    }

    @Override
    public Optional<SchemaPath> getSchemaPath() {
        return Optional.of(SchemaPath.ROOT);
    }

    /**
     * @return true
     */
    @Override
    public boolean isRootContext() {
        return true;
    }

    @Override
    public boolean isConfiguration() {
        return true;
    }

    @Override
    public boolean isEnabledSemanticVersioning() {
        return sourceContext.isEnabledSemanticVersioning();
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> void addToLocalStorage(final Class<N> type, final K key,
            final V value) {
        if (IncludedModuleContext.class.isAssignableFrom(type)) {
            includedContexts.add((NamespaceStorageNode) value);
        }
        super.addToLocalStorage(type, key, value);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V getFromLocalStorage(final Class<N> type, final K key) {
        final V potentialLocal = super.getFromLocalStorage(type, key);
        if (potentialLocal != null) {
            return potentialLocal;
        }
        for (final NamespaceStorageNode includedSource : includedContexts) {
            final V potential = includedSource.getFromLocalStorage(type, key);
            if (potential != null) {
                return potential;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAllFromLocalStorage(final Class<N> type) {
        final Map<K, V> potentialLocal = super.getAllFromLocalStorage(type);
        if (potentialLocal != null) {
            return potentialLocal;
        }
        for (final NamespaceStorageNode includedSource : includedContexts) {
            final Map<K, V> potential = includedSource.getAllFromLocalStorage(type);
            if (potential != null) {
                return potential;
            }
        }
        return null;
    }
}
