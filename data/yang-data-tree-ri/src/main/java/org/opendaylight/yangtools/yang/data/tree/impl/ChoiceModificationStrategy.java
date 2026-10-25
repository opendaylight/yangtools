/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.AbstractNodeContainerModificationStrategy.Visible;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;

final class ChoiceModificationStrategy extends Visible<ChoiceSchemaNode> implements AutomaticLifecycleMixin {
    private static final NormalizedNodeContainerSupport<NodeIdentifier, ChoiceNode> SUPPORT =
            new NormalizedNodeContainerSupport<>(ChoiceNode.class, BUILDER_FACTORY::newChoiceBuilder,
                BUILDER_FACTORY::newChoiceBuilder);

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
        emptyNode =  BUILDER_FACTORY.newChoiceBuilder()
            .withNodeIdentifier(NodeIdentifier.create(schema.getQName()))
            .build();
    }

    @Override
    TreeNode apply(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        return apply(emptyNode, modification, currentMeta, version);
    }

    @Override
    public TreeNode superApply(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        return super.apply(modification, currentMeta, version);
    }

    @Override
    public TreeNode thisApplyWrite(final ModifiedNode modification, final NormalizedNode newValue,
            final TreeNode currentMeta, final Version version) {
        return applyWrite(modification, newValue, currentMeta, version);
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

    @NonNullByDefault
    private TreeNode enforceCases(final TreeNode tree) {
        enforceCases(tree.data());
        return tree;
    }

    private void enforceCases(final NormalizedNode normalizedNode) {
        if (!(normalizedNode instanceof ChoiceNode choice)) {
            throw new VerifyException("Unexpected " + normalizedNode.contract().getCanonicalName());
        }
        if (choice.isEmpty()) {
            // FIXME: empty choice: these should not be allowed
            return;
        }
        final var firstChild = choice.body().iterator().next().name();
        final var enforcer = caseEnforcers.get(firstChild);
        if (enforcer == null) {
            throw new VerifyException("""
                Case enforcer cannot be null. Most probably, child node %s of choice node %s does not belong in \
                current tree type.""".formatted(firstChild, choice.name()));
        }

        // Make sure no leaves from other cases are present
        final var exclusion = exclusions.get(enforcer);
        if (exclusion == null) {
            throw new VerifyException("no exclusion for " + enforcer);
        }

        for (var other : exclusion) {
            for (var otherChild : other.getChildIdentifiers()) {
                final var child = choice.childByArg(otherChild);
                if (child != null) {
                    throw new IllegalArgumentException(
                        "Child %s (from case %s) implies non-presence of child %s (from case %s), which is %s"
                        .formatted(firstChild, enforcer, otherChild, other, child.contract().getCanonicalName()));
                }
            }
        }

        // Make sure all mandatory children are present
        enforcer.enforceOnChoice(choice);
    }

    @Override
    protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        return enforceCases(super.applyMerge(modification, currentMeta, version));
    }

    @Override
    protected TreeNode applyWrite(final ModifiedNode modification, final NormalizedNode newValue,
            final TreeNode currentMeta, final Version version) {
        return enforceCases(super.applyWrite(modification, newValue, currentMeta, version));
    }

    @Override
    protected TreeNode applyTouch(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        return enforceCases(super.applyTouch(modification, currentMeta, version));
    }
}
