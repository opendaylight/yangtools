/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.YangValidationBundles;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice.ImplicitCaseSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.AugmentToChoiceNamespace;

public abstract class AbstractEffectiveDocumentedDataNodeContainer<A, D extends DeclaredStatement<A>>
        extends AbstractSchemaEffectiveDocumentedNode<A, D> implements DataNodeContainer {

    private final ImmutableMap<QName, DataSchemaNode> childNodes;
    private final ImmutableSet<GroupingDefinition> groupings;
    private final ImmutableSet<UsesNode> uses;
    private final ImmutableSet<TypeDefinition<?>> typeDefinitions;
    private final ImmutableSet<DataSchemaNode> publicChildNodes;

    protected AbstractEffectiveDocumentedDataNodeContainer(final StmtContext<A, D, ?> ctx) {
        super(ctx);

        Map<QName, DataSchemaNode> mutableChildNodes = new LinkedHashMap<>();
        Set<GroupingDefinition> mutableGroupings = new HashSet<>();
        Set<UsesNode> mutableUses = new HashSet<>();
        Set<TypeDefinition<?>> mutableTypeDefinitions = new LinkedHashSet<>();
        Set<DataSchemaNode> mutablePublicChildNodes = new LinkedHashSet<>();

        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DataSchemaNode) {
                final DataSchemaNode dataSchemaNode = (DataSchemaNode) stmt;
                if (mutableChildNodes.containsKey(dataSchemaNode.getQName())) {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, stmt);
                }

                //  Add case short hand when augmenting choice with short hand
                if (this instanceof AugmentationSchemaNode
                        && !(stmt instanceof CaseSchemaNode || stmt instanceof ChoiceSchemaNode)
                        && YangValidationBundles.SUPPORTED_CASE_SHORTHANDS.contains(stmt.statementDefinition())
                        && Boolean.TRUE.equals(ctx.getFromNamespace(AugmentToChoiceNamespace.class, ctx))) {
                    final ImplicitCaseSchemaNode caseShorthand = new ImplicitCaseSchemaNode(dataSchemaNode);
                    mutableChildNodes.put(caseShorthand.getQName(), caseShorthand);
                    mutablePublicChildNodes.add(caseShorthand);
                } else {
                    mutableChildNodes.put(dataSchemaNode.getQName(), dataSchemaNode);
                    mutablePublicChildNodes.add(dataSchemaNode);
                }
            }
            if (stmt instanceof UsesNode) {
                UsesNode usesNode = (UsesNode) stmt;
                if (!mutableUses.contains(usesNode)) {
                    mutableUses.add(usesNode);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, stmt);
                }
            }
            if (stmt instanceof TypedefEffectiveStatement) {
                TypedefEffectiveStatement typeDef = (TypedefEffectiveStatement) stmt;
                TypeDefinition<?> type = typeDef.getTypeDefinition();
                if (!mutableTypeDefinitions.contains(type)) {
                    mutableTypeDefinitions.add(type);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, stmt);
                }
            }
            if (stmt instanceof GroupingDefinition) {
                GroupingDefinition grp = (GroupingDefinition) stmt;
                if (!mutableGroupings.contains(grp)) {
                    mutableGroupings.add(grp);
                } else {
                    throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, stmt);
                }
            }
        }

        this.childNodes = ImmutableMap.copyOf(mutableChildNodes);
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
        // Child nodes are keyed by their container name, so we can do a direct lookup
        return Optional.ofNullable(childNodes.get(requireNonNull(name)));
    }

    @Override
    public Set<UsesNode> getUses() {
        return uses;
    }
}
