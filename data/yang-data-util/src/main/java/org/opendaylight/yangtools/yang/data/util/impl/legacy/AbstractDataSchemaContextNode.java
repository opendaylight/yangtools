/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.legacy;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Schema derived data providing necessary information for mapping between
 * {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode} and serialization format defined in RFC6020,
 * since the mapping is not one-to-one.
 */
public abstract class AbstractDataSchemaContextNode implements DataSchemaContextNode {
    // FIXME: YANGTOOLS-1413: this field should not be needed
    private final @NonNull PathArgument pathArgument;

    final @NonNull DataSchemaNode dataSchemaNode;

    AbstractDataSchemaContextNode(final PathArgument pathArgument, final DataSchemaNode dataSchemaNode) {
        this.dataSchemaNode = requireNonNull(dataSchemaNode);
        this.pathArgument = requireNonNull(pathArgument);
    }

    @Override
    public final DataSchemaNode getDataSchemaNode() {
        return dataSchemaNode;
    }

    @Override
    public final PathArgument pathArgument() {
        return pathArgument;
    }

    @Override
    public boolean isKeyedEntry() {
        return false;
    }

    Set<QName> qnameIdentifiers() {
        return ImmutableSet.of(dataSchemaNode.getQName());
    }

    @Override
    public abstract @Nullable DataSchemaContextNode getChild(PathArgument child);

    @Override
    public abstract @Nullable DataSchemaContextNode getChild(QName child);

    @Override
    public final @Nullable DataSchemaContextNode enterChild(final SchemaInferenceStack stack, final QName child) {
        return enterChild(requireNonNull(child), requireNonNull(stack));
    }

    @Override
    public final @Nullable DataSchemaContextNode enterChild(final SchemaInferenceStack stack,
            final PathArgument child) {
        return enterChild(requireNonNull(child), requireNonNull(stack));
    }

    abstract @Nullable DataSchemaContextNode enterChild(@NonNull QName child, @NonNull SchemaInferenceStack stack);

    abstract @Nullable DataSchemaContextNode enterChild(@NonNull PathArgument child,
        @NonNull SchemaInferenceStack stack);

    /**
     * Push this node into specified {@link SchemaInferenceStack}.
     *
     * @param stack {@link SchemaInferenceStack}
     */
    void pushToStack(final @NonNull SchemaInferenceStack stack) {
        // Accurate for most subclasses
        stack.enterSchemaTree(dataSchemaNode.getQName());
    }

    static DataSchemaNode findChildSchemaNode(final DataNodeContainer parent, final QName child) {
        final DataSchemaNode potential = parent.dataChildByName(child);
        return potential == null ? findChoice(Iterables.filter(parent.getChildNodes(), ChoiceSchemaNode.class), child)
                : potential;
    }

    static AbstractDataSchemaContextNode fromSchemaAndQNameChecked(final DataNodeContainer schema, final QName child) {
        return lenientOf(findChildSchemaNode(schema, child));
    }

    // FIXME: this looks like it should be a Predicate on a stream with findFirst()
    private static ChoiceSchemaNode findChoice(final Iterable<ChoiceSchemaNode> choices, final QName child) {
        for (ChoiceSchemaNode choice : choices) {
            // FIXME: this looks weird: what are we looking for again?
            for (CaseSchemaNode caze : choice.getCases()) {
                if (findChildSchemaNode(caze, child) != null) {
                    return choice;
                }
            }
        }
        return null;
    }

    static @NonNull AbstractDataSchemaContextNode of(final @NonNull DataSchemaNode schema) {
        if (schema instanceof ContainerLike containerLike) {
            return new ContainerContextNode(containerLike);
        } else if (schema instanceof ListSchemaNode list) {
            return fromListSchemaNode(list);
        } else if (schema instanceof LeafSchemaNode leaf) {
            return new LeafContextNode(leaf);
        } else if (schema instanceof ChoiceSchemaNode choice) {
            return new ChoiceNodeContextNode(choice);
        } else if (schema instanceof LeafListSchemaNode leafList) {
            return new LeafListMixinContextNode(leafList);
        } else if (schema instanceof AnydataSchemaNode anydata) {
            return new OpaqueContextNode(anydata);
        } else if (schema instanceof AnyxmlSchemaNode anyxml) {
            return new OpaqueContextNode(anyxml);
        } else {
            throw new IllegalStateException("Unhandled schema " + schema);
        }
    }

    // FIXME: do we tolerate null argument? do we tolerate unknown subclasses?
    static @Nullable AbstractDataSchemaContextNode lenientOf(final @Nullable DataSchemaNode schema) {
        if (schema instanceof ContainerLike containerLike) {
            return new ContainerContextNode(containerLike);
        } else if (schema instanceof ListSchemaNode list) {
            return fromListSchemaNode(list);
        } else if (schema instanceof LeafSchemaNode leaf) {
            return new LeafContextNode(leaf);
        } else if (schema instanceof ChoiceSchemaNode choice) {
            return new ChoiceNodeContextNode(choice);
        } else if (schema instanceof LeafListSchemaNode leafList) {
            return new LeafListMixinContextNode(leafList);
        } else if (schema instanceof AnydataSchemaNode anydata) {
            return new OpaqueContextNode(anydata);
        } else if (schema instanceof AnyxmlSchemaNode anyxml) {
            return new OpaqueContextNode(anyxml);
        } else {
            return null;
        }
    }

    private static @NonNull AbstractDataSchemaContextNode fromListSchemaNode(final ListSchemaNode potential) {
        var keyDefinition = potential.getKeyDefinition();
        if (keyDefinition.isEmpty()) {
            return new ListMixinContextNode(potential);
        } else {
            return new MapMixinContextNode(potential);
        }
    }
}
