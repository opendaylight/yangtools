/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Verify;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class ChoiceModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private final Map<PathArgument, ModificationApplyOperation> childNodes;
    // FIXME: enforce leaves not coming from two case statements at the same time
    private final Map<CaseEnforcer, Collection<CaseEnforcer>> exclusions;
    private final Map<PathArgument, CaseEnforcer> caseEnforcers;

    ChoiceModificationStrategy(final ChoiceSchemaNode schemaNode, final DataTreeConfiguration treeConfig) {
        super(ChoiceNode.class, treeConfig);

        final Builder<PathArgument, ModificationApplyOperation> childBuilder = ImmutableMap.builder();
        final Builder<PathArgument, CaseEnforcer> enforcerBuilder = ImmutableMap.builder();
        for (final ChoiceCaseNode caze : schemaNode.getCases()) {
            final CaseEnforcer enforcer = CaseEnforcer.forTree(caze, treeConfig);
            if (enforcer != null) {
                for (final Entry<NodeIdentifier, DataSchemaNode> e : enforcer.getChildEntries()) {
                    childBuilder.put(e.getKey(), SchemaAwareApplyOperation.from(e.getValue(), treeConfig));
                    enforcerBuilder.put(e.getKey(), enforcer);
                }
                for (final Entry<AugmentationIdentifier, AugmentationSchema> e : enforcer.getAugmentationEntries()) {
                    childBuilder.put(e.getKey(), new AugmentationModificationStrategy(e.getValue(), caze, treeConfig));
                    enforcerBuilder.put(e.getKey(), enforcer);
                }
            }
        }
        childNodes = childBuilder.build();
        caseEnforcers = enforcerBuilder.build();

        final Map<CaseEnforcer, Collection<CaseEnforcer>> exclusionsBuilder = new HashMap<>();
        for (final CaseEnforcer e : caseEnforcers.values()) {
            exclusionsBuilder.put(e, ImmutableList.copyOf(
                Collections2.filter(caseEnforcers.values(), Predicates.not(Predicates.equalTo(e)))));
        }
        exclusions = ImmutableMap.copyOf(exclusionsBuilder);
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return Optional.ofNullable(childNodes.get(child));
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected DataContainerNodeBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof ChoiceNode);
        return ImmutableChoiceNodeBuilder.create((ChoiceNode) original);
    }

    @Override
    void verifyStructure(final NormalizedNode<?, ?> writtenValue, final boolean verifyChildren) {
        if (verifyChildrenStructure() && verifyChildren) {
            enforceCases(writtenValue);
        }
        super.verifyStructure(writtenValue, verifyChildren);
    }

    private void enforceCases(final TreeNode tree) {
        enforceCases(tree.getData());
    }

    private void enforceCases(final NormalizedNode<?, ?> normalizedNode) {
        Verify.verify(normalizedNode instanceof ChoiceNode);
        final Collection<DataContainerChild<?, ?>> children = ((ChoiceNode) normalizedNode).getValue();
        if (!children.isEmpty()) {
            final DataContainerChild<?, ?> firstChild = children.iterator().next();
            final CaseEnforcer enforcer = caseEnforcers.get(firstChild.getIdentifier());
            Verify.verifyNotNull(enforcer, "Case enforcer cannot be null. Most probably, child node %s of choice node %s does not belong in current tree type.", firstChild.getIdentifier(), normalizedNode.getIdentifier());

            // Make sure no leaves from other cases are present
            for (final CaseEnforcer other : exclusions.get(enforcer)) {
                for (final PathArgument id : other.getAllChildIdentifiers()) {
                    final Optional<NormalizedNode<?, ?>> maybeChild = NormalizedNodes.getDirectChild(normalizedNode, id);
                    Preconditions.checkArgument(!maybeChild.isPresent(),
                        "Child %s (from case %s) implies non-presence of child %s (from case %s), which is %s",
                        firstChild.getIdentifier(), enforcer, id, other, maybeChild.orElse(null));
                }
            }

            // Make sure all mandatory children are present
            enforcer.enforceOnTreeNode(normalizedNode);
        }
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final TreeNode ret = super.applyMerge(modification, currentMeta, version);
        enforceCases(ret);
        return ret;
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification, final Optional<TreeNode> currentMeta,
            final Version version) {
        final TreeNode ret = super.applyWrite(modification, currentMeta, version);
        enforceCases(ret);
        return ret;
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final TreeNode ret = super.applyTouch(modification, currentMeta, version);
        enforceCases(ret);
        return ret;
    }

    @Override
    protected NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof ChoiceNode);
        return ImmutableChoiceNodeBuilder.create().withNodeIdentifier(((ChoiceNode) original).getIdentifier()).build();
    }
}

