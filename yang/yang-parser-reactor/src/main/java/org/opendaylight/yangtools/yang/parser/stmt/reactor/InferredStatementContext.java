/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.GlobalNamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A statement which has been inferred to exist. Functionally it is equivalent to a SubstatementContext, but it is not
 * backed by a declaration (and declared statements). It is backed by a prototype StatementContextBase and has only
 * effective substatements, which are either transformed from that prototype or added by inference.
 */
final class InferredStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementContextBase<A, D, E> {
    private static final Logger LOG = LoggerFactory.getLogger(InferredStatementContext.class);

    private final @NonNull StatementContextBase<A, D, E> prototype;
    private final @NonNull StatementContextBase<?, ?, ?> parent;
    private final @NonNull StmtContext<A, D, E> originalCtx;
    private final @NonNull CopyType childCopyType;
    private final QNameModule targetModule;
    private final A argument;

    private InferredStatementContext(final InferredStatementContext<A, D, E> original,
            final StatementContextBase<?, ?, ?> parent) {
        super(original);
        this.parent = requireNonNull(parent);
        this.childCopyType = original.childCopyType;
        this.targetModule = original.targetModule;
        this.prototype = original.prototype;
        this.originalCtx = original.originalCtx;
        this.argument = original.argument;
    }

    InferredStatementContext(final StatementContextBase<?, ?, ?> parent, final StatementContextBase<A, D, E> prototype,
            final CopyType myCopyType, final CopyType childCopyType, final QNameModule targetModule) {
        super(prototype.definition(), CopyHistory.of(myCopyType, prototype.getCopyHistory()));
        this.parent = requireNonNull(parent);
        this.prototype = requireNonNull(prototype);
        this.argument = targetModule == null ? prototype.getStatementArgument()
                : prototype.definition().adaptArgumentValue(prototype, targetModule);
        this.childCopyType = requireNonNull(childCopyType);
        this.targetModule = targetModule;
        this.originalCtx = prototype.getOriginalCtx().orElse(prototype);

        // FIXME: YANGTOOLS-784: instantiate these lazily
        addEffectiveSubstatements(createEffective());
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        return ImmutableList.of();
    }

    @Override
    public Iterable<? extends StmtContext<?, ?, ?>> allSubstatements() {
        // No need to concat with declared
        return effectiveSubstatements();
    }

    @Override
    public Stream<? extends StmtContext<?, ?, ?>> allSubstatementsStream() {
        // No need to concat with declared
        return effectiveSubstatements().stream();
    }

    @Override
    public StatementSourceReference getStatementSourceReference() {
        return originalCtx.getStatementSourceReference();
    }

    @Override
    public String rawStatementArgument() {
        return originalCtx.rawStatementArgument();
    }

    @Override
    public Optional<StmtContext<A, D, E>> getOriginalCtx() {
        return Optional.of(originalCtx);
    }

    @Override
    public Optional<StmtContext<A, D, E>> getPreviousCopyCtx() {
        return Optional.of(prototype);
    }

    @Override
    public D buildDeclared() {
        /*
         * Share original instance of declared statement between all effective statements which have been copied or
         * derived from this original declared statement.
         */
        return originalCtx.buildDeclared();
    }

    @Override
    InferredStatementContext<A, D, E> reparent(final StatementContextBase<?, ?, ?> newParent) {
        return new InferredStatementContext<>(this, newParent);
    }

    @Override
    boolean hasEmptySubstatements() {
        return hasEmptyEffectiveSubstatements();
    }

    // Instantiate this statement's effective substatements. Note this method has side-effects in namespaces and overall
    // BuildGlobalContext, hence it must be called at most once.
    private List<Mutable<?, ?, ?>> createEffective() {
        final Collection<? extends StatementContextBase<?, ?, ?>> declared = prototype.mutableDeclaredSubstatements();
        final Collection<? extends Mutable<?, ?, ?>> effective = prototype.mutableEffectiveSubstatements();
        final List<Mutable<?, ?, ?>> buffer = new ArrayList<>(declared.size() + effective.size());

        for (final Mutable<?, ?, ?> stmtContext : declared) {
            if (stmtContext.isSupportedByFeatures()) {
                copySubstatement(stmtContext, buffer);
            }
        }
        for (final Mutable<?, ?, ?> stmtContext : effective) {
            copySubstatement(stmtContext, buffer);
        }

        return buffer;
    }

    // Statement copy mess starts here
    //
    // FIXME: This is messy and is probably wrong in some corner case. Even if it is correct, the way how it is correct
    //        relies on hard-coded maps. At the end of the day, the logic needs to be controlled by statement's
    //        StatementSupport.
    // FIXME: YANGTOOLS-652: this map looks very much like UsesStatementSupport.TOP_REUSED_DEF_SET
    private static final ImmutableSet<YangStmtMapping> REUSED_DEF_SET = ImmutableSet.of(
        YangStmtMapping.TYPE,
        YangStmtMapping.TYPEDEF,
        YangStmtMapping.USES);

    private void copySubstatement(final Mutable<?, ?, ?> substatement, final Collection<Mutable<?, ?, ?>> buffer) {
        final StatementDefinition def = substatement.getPublicDefinition();

        // FIXME: YANGTOOLS-652: formerly known as "isReusedByUses"
        if (REUSED_DEF_SET.contains(def)) {
            LOG.debug("Reusing substatement {} for {}", substatement, this);
            buffer.add(substatement);
            return;
        }

        substatement.copyAsChildOf(this, childCopyType, targetModule).ifPresent(buffer::add);
    }

    // Statement copy mess ends here

    /*
     * KEEP THINGS ORGANIZED!
     *
     * below methods exist in the same form in SubstatementContext. If any adjustment is made here, make sure it is
     * properly updated there.
     */
    @Override
    @Deprecated
    public Optional<SchemaPath> getSchemaPath() {
        return substatementGetSchemaPath();
    }

    @Override
    public A getStatementArgument() {
        return argument;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        return parent;
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.STATEMENT_LOCAL;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentNamespaceStorage() {
        return parent;
    }

    @Override
    public GlobalNamespaceStorageNode getGlobalNamespaceStorage() {
        return parent.getGlobalNamespaceStorage();
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return parent.getRoot();
    }

    @Override
    public boolean isConfiguration() {
        return isConfiguration(parent);
    }

    @Override
    protected boolean isIgnoringIfFeatures() {
        return isIgnoringIfFeatures(parent);
    }

    @Override
    protected boolean isIgnoringConfig() {
        return isIgnoringConfig(parent);
    }

    @Override
    protected boolean isParentSupportedByFeatures() {
        return parent.isSupportedByFeatures();
    }
}
