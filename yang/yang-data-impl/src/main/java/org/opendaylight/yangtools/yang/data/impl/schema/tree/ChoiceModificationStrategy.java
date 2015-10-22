/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Verify;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class ChoiceModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private final Map<PathArgument, ModificationApplyOperation> childNodes;
    // FIXME: enforce leaves not coming from two case statements at the same time
    private final Map<CaseEnforcer, Collection<CaseEnforcer>> exclusions;
    private final Map<PathArgument, CaseEnforcer> caseEnforcers;

    ChoiceModificationStrategy(final ChoiceSchemaNode schemaNode, final TreeType treeType) {
        super(ChoiceNode.class, treeType);

        final Builder<PathArgument, ModificationApplyOperation> childBuilder = ImmutableMap.builder();
        final Builder<PathArgument, CaseEnforcer> enforcerBuilder = ImmutableMap.builder();
        for (final ChoiceCaseNode caze : schemaNode.getCases()) {
            final CaseEnforcer enforcer = CaseEnforcer.forTree(caze, treeType);
            if (enforcer != null) {
                for (final Entry<NodeIdentifier, DataSchemaNode> e : enforcer.getChildEntries()) {
                    childBuilder.put(e.getKey(), SchemaAwareApplyOperation.from(e.getValue(), treeType));
                    enforcerBuilder.put(e.getKey(), enforcer);
                }
            }
        }
        childNodes = childBuilder.build();
        caseEnforcers = enforcerBuilder.build();

        final Builder<CaseEnforcer, Collection<CaseEnforcer>> exclusionsBuilder = ImmutableMap.builder();
        for (CaseEnforcer e : caseEnforcers.values()) {
            exclusionsBuilder.put(e, ImmutableList.copyOf(
                Collections2.filter(caseEnforcers.values(), Predicates.not(Predicates.equalTo(e)))));
        }
        exclusions = exclusionsBuilder.build();
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return Optional.fromNullable(childNodes.get(child));
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected DataContainerNodeBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof ChoiceNode);
        return ImmutableChoiceNodeBuilder.create((ChoiceNode) original);
    }

    private void enforceCases(final NormalizedNode<?, ?> normalizedNode) {
        Verify.verify(normalizedNode instanceof ChoiceNode);
        final Collection<DataContainerChild<?, ?>> children = ((ChoiceNode) normalizedNode).getValue();
        if (!children.isEmpty()) {
            final DataContainerChild<?, ?> firstChild = children.iterator().next();
            final CaseEnforcer enforcer = caseEnforcers.get(firstChild.getIdentifier());
            Verify.verifyNotNull(enforcer);

            // Make sure no leaves from other cases are present
            for (CaseEnforcer other : exclusions.get(enforcer)) {
                for (NodeIdentifier id : other.getChildIdentifiers()) {
                    final Optional<NormalizedNode<?, ?>> maybeChild = NormalizedNodes.getDirectChild(normalizedNode, id);
                    Preconditions.checkArgument(!maybeChild.isPresent(),
                        "Child %s (from case %s) implies non-presence of child %s (from case %s), which is %s",
                        firstChild.getIdentifier(), enforcer, id, other, maybeChild.get());
                }
            }
        }
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final TreeNode ret = super.applyMerge(modification, currentMeta, version);
        enforceCases(ret.getData());
        return ret;
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification, final Optional<TreeNode> currentMeta,
            final Version version) {
        final TreeNode ret = super.applyWrite(modification, currentMeta, version);
        enforceCases(ret.getData());
        return ret;
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final TreeNode ret = super.applyTouch(modification, currentMeta, version);
        enforceCases(ret.getData());
        return ret;
    }
}
