/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public abstract class AbstractEffectiveDocumentedDataNodeContainer<A, D extends DeclaredStatement<A>>
        extends AbstractSchemaEffectiveDocumentedNode<A, D> implements DataNodeContainer {

    private final ImmutableSet<GroupingDefinition> groupings;
    private final ImmutableSet<UsesNode> uses;
    private final ImmutableSet<TypeDefinition<?>> typeDefinitions;
    private final ImmutableSet<DataSchemaNode> publicChildNodes;

    protected AbstractEffectiveDocumentedDataNodeContainer(final StmtContext<A, D, ?> ctx) {
        super(ctx);

        Set<GroupingDefinition> mutableGroupings = new HashSet<>();
        Set<UsesNode> mutableUses = new HashSet<>();
        Set<TypeDefinition<?>> mutableTypeDefinitions = new LinkedHashSet<>();
        Set<DataSchemaNode> mutablePublicChildNodes = new LinkedHashSet<>();

        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DataSchemaNode) {
                mutablePublicChildNodes.add((DataSchemaNode) stmt);
            }
            if (stmt instanceof UsesNode && !mutableUses.add((UsesNode) stmt)) {
                throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, stmt);
            }
            if (stmt instanceof TypedefEffectiveStatement
                    && !mutableTypeDefinitions.add(((TypedefEffectiveStatement) stmt).getTypeDefinition())) {
                throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, stmt);
            }
            if (stmt instanceof GroupingDefinition && !mutableGroupings.add((GroupingDefinition) stmt)) {
                throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, stmt);
            }
        }

        this.groupings = ImmutableSet.copyOf(mutableGroupings);
        this.publicChildNodes = ImmutableSet.copyOf(mutablePublicChildNodes);
        this.typeDefinitions = ImmutableSet.copyOf(mutableTypeDefinitions);
        this.uses = ImmutableSet.copyOf(mutableUses);
    }

    @Override
    public final Set<TypeDefinition<?>> getTypeDefinitions() {
        return typeDefinitions;
    }

    @Override
    public final Set<DataSchemaNode> getChildNodes() {
        return publicChildNodes;
    }

    @Override
    public final Set<GroupingDefinition> getGroupings() {
        return groupings;
    }

    @Override
    public final Optional<DataSchemaNode> findDataChildByName(final QName name) {
        return findDataSchemaNode(name);
    }

    @Override
    public Set<UsesNode> getUses() {
        return uses;
    }
}
