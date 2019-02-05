/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.NormalizedNodeContainerSupport.Single;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class ChoiceModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private static final Single<NodeIdentifier, ChoiceNode> SUPPORT = new Single<>(ChoiceNode.class,
            ImmutableChoiceNodeBuilder::create, ImmutableChoiceNodeBuilder::create);

    private final ImmutableMap<PathArgument, ModificationApplyOperation> childNodes;
    // FIXME: enforce leaves not coming from two case statements at the same time
    private final ImmutableMap<PathArgument, CaseEnforcer> caseEnforcers;
    private final ChoiceNode emptyNode;

    ChoiceModificationStrategy(final ChoiceSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(SUPPORT, treeConfig);

        final Builder<PathArgument, ModificationApplyOperation> childBuilder = ImmutableMap.builder();
        final Builder<PathArgument, CaseEnforcer> enforcerBuilder = ImmutableMap.builder();
        for (final CaseSchemaNode caze : schema.getCases().values()) {
            final CaseEnforcer enforcer = CaseEnforcer.forTree(caze, treeConfig);
            if (enforcer != null) {
                for (final Entry<NodeIdentifier, DataSchemaNode> e : enforcer.getChildEntries()) {
                    childBuilder.put(e.getKey(), SchemaAwareApplyOperation.from(e.getValue(), treeConfig));
                    enforcerBuilder.put(e.getKey(), enforcer);
                }
                for (final Entry<AugmentationIdentifier, AugmentationSchemaNode> e
                        : enforcer.getAugmentationEntries()) {
                    childBuilder.put(e.getKey(), new AugmentationModificationStrategy(e.getValue(), caze, treeConfig));
                    enforcerBuilder.put(e.getKey(), enforcer);
                }
            }
        }
        childNodes = childBuilder.build();
        caseEnforcers = enforcerBuilder.build();
        emptyNode = ImmutableNodes.choiceNode(schema.getQName());
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        return AutomaticLifecycleMixin.apply(super::apply, this::applyWrite, emptyNode, modification, storeMeta,
            version);
    }

    @Override
    void checkApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        AutomaticLifecycleMixin.checkApplicable(super::checkApplicable, emptyNode, path, modification, current,
            version);
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return Optional.ofNullable(childNodes.get(child));
    }

    @Override
    void additionalVerifyValueChildren(final NormalizedNode<?, ?> writtenValue) {
        enforceCases(writtenValue);
    }

    private void enforceCases(final TreeNode tree) {
        enforceCases(tree.getData());
    }

    private void enforceCases(final NormalizedNode<?, ?> normalizedNode) {
        verify(normalizedNode instanceof ChoiceNode);
        final Collection<DataContainerChild<?, ?>> children = ((ChoiceNode) normalizedNode).getValue();
        if (children.isEmpty()) {
            return;
        }

        final Iterator<DataContainerChild<?, ?>> it = children.iterator();
        final DataContainerChild<?, ?> firstChild = it.next();
        final Object firstId = firstChild.getIdentifier();
        final CaseEnforcer enforcer = verifyNotNull(caseEnforcers.get(firstId), "Case enforcer cannot be null. "
                + "Most probably, child node %s of choice node %s does not belong in current tree type.", firstId,
                normalizedNode.getIdentifier());

        // Make sure all other leaves fall into the same enforcer
        while (it.hasNext()) {
            final DataContainerChild<?, ?> otherChild = it.next();
            final Object otherId = otherChild.getIdentifier();
            final CaseEnforcer otherEnforcer = caseEnforcers.get(otherId);
            checkArgument(enforcer.equals(otherEnforcer),
                "Child %s (from case %s) implies non-presence of child %s (from case %s), which is %s",
                firstId, enforcer, otherId, otherEnforcer, otherChild);
        }

        // Make sure all mandatory children are present
        enforcer.enforceOnTreeNode(normalizedNode);
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final TreeNode ret = super.applyMerge(modification, currentMeta, version);
        enforceCases(ret);
        return ret;
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification,  final NormalizedNode<?, ?> newValue,
            final Optional<TreeNode> currentMeta, final Version version) {
        final TreeNode ret = super.applyWrite(modification, newValue, currentMeta, version);
        enforceCases(ret);
        return ret;
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final TreeNode ret = super.applyTouch(modification, currentMeta, version);
        enforceCases(ret);
        return ret;
    }
}

