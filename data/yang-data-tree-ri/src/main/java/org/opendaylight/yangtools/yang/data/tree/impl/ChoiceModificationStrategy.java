/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.AbstractNodeContainerModificationStrategy.Visible;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;

final class ChoiceModificationStrategy extends Visible<ChoiceSchemaNode> {
    private static final NormalizedNodeContainerSupport<NodeIdentifier, ChoiceNode> SUPPORT =
            new NormalizedNodeContainerSupport<>(ChoiceNode.class, ImmutableChoiceNodeBuilder::create,
                    ImmutableChoiceNodeBuilder::new);

    private final ImmutableMap<PathArgument, ModificationApplyOperation> childNodes;
    // FIXME: enforce leaves not coming from two case statements at the same time
    private final ImmutableMap<CaseEnforcer, ImmutableList<CaseEnforcer>> exclusions;
    private final ImmutableMap<PathArgument, CaseEnforcer> caseEnforcers;
    private final @NonNull ChoiceNode emptyNode;

    ChoiceModificationStrategy(final ChoiceSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(SUPPORT, treeConfig, schema);

        final var childBuilder = ImmutableMap.<PathArgument, ModificationApplyOperation>builder();
        final var enforcerBuilder = ImmutableMap.<PathArgument, CaseEnforcer>builder();
        for (var caze : schema.getCases()) {
            final var enforcer = CaseEnforcer.forTree(caze, treeConfig);
            if (enforcer != null) {
                for (var entry : enforcer.getChildEntries()) {
                    final ModificationApplyOperation childOper;
                    try {
                        childOper = SchemaAwareApplyOperation.from(entry.getValue(), treeConfig);
                    } catch (ExcludedDataSchemaNodeException e) {
                        // This should never happen as enforcer performs filtering
                        throw new IllegalStateException("Enforcer references out-of-tree child " + entry, e);
                    }

                    childBuilder.put(entry.getKey(), childOper);
                    enforcerBuilder.put(entry.getKey(), enforcer);
                }
            }
        }
        childNodes = childBuilder.build();
        caseEnforcers = enforcerBuilder.build();

        final var exclusionsBuilder = new HashMap<CaseEnforcer, ImmutableList<CaseEnforcer>>();
        for (var key : caseEnforcers.values()) {
            exclusionsBuilder.put(key, caseEnforcers.values().stream()
                .filter(enforcer -> !key.equals(enforcer))
                .collect(ImmutableList.toImmutableList()));
        }
        exclusions = ImmutableMap.copyOf(exclusionsBuilder);
        emptyNode = ImmutableNodes.choiceNode(schema.getQName());
    }

    @Override
    TreeNode apply(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        return AutomaticLifecycleMixin.apply(super::apply, this::applyWrite, emptyNode, modification, currentMeta,
            version);
    }

    @Override
    TreeNode defaultTreeNode() {
        return defaultTreeNode(emptyNode);
    }

    @Override
    public ModificationApplyOperation childByArg(final PathArgument arg) {
        return childNodes.get(arg);
    }

    @Override
    void optionalVerifyValueChildren(final DistinctNodeContainer<?, ?> writtenValue) {
        enforceCases(writtenValue);
    }

    private void enforceCases(final TreeNode tree) {
        enforceCases(tree.getData());
    }

    private void enforceCases(final NormalizedNode normalizedNode) {
        verify(normalizedNode instanceof ChoiceNode);
        final var choice = (ChoiceNode) normalizedNode;
        if (!choice.isEmpty()) {
            final var firstChild = choice.body().iterator().next();
            final var enforcer = verifyNotNull(caseEnforcers.get(firstChild.name()),
                "Case enforcer cannot be null. Most probably, child node %s of choice node %s does not belong "
                + "in current tree type.", firstChild.name(), normalizedNode.name());

            // Make sure no leaves from other cases are present
            for (var other : verifyNotNull(exclusions.get(enforcer))) {
                for (var id : other.getChildIdentifiers()) {
                    final var child = choice.childByArg(id);
                    checkArgument(child == null,
                        "Child %s (from case %s) implies non-presence of child %s (from case %s), which is %s",
                        firstChild.name(), enforcer, id, other, child);
                }
            }

            // Make sure all mandatory children are present
            enforcer.enforceOnChoice(choice);
        }
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final var ret = super.applyMerge(modification, currentMeta, version);
        enforceCases(ret);
        return ret;
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification, final NormalizedNode newValue,
            final TreeNode currentMeta, final Version version) {
        final var ret = super.applyWrite(modification, newValue, currentMeta, version);
        enforceCases(ret);
        return ret;
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        final var ret = super.applyTouch(modification, currentMeta, version);
        enforceCases(ret);
        return ret;
    }
}

