/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.context;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
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
public abstract sealed class AbstractContext implements DataSchemaContext
        permits AbstractPathMixinContext, AbstractCompositeContext, AbstractValueContext, OpaqueContext {
    private final NodeIdentifier pathStep;

    final @NonNull DataSchemaNode dataSchemaNode;

    AbstractContext(final NodeIdentifier pathStep, final DataSchemaNode dataSchemaNode) {
        this.dataSchemaNode = requireNonNull(dataSchemaNode);
        this.pathStep = pathStep;
    }

    @Override
    public final DataSchemaNode dataSchemaNode() {
        return dataSchemaNode;
    }

    @Override
    public final NodeIdentifier pathStep() {
        return pathStep;
    }

    ImmutableSet<QName> qnameIdentifiers() {
        return ImmutableSet.of(dataSchemaNode.getQName());
    }

    /**
     * Push this node into specified {@link SchemaInferenceStack}.
     *
     * @param stack {@link SchemaInferenceStack}
     */
    void pushToStack(final @NonNull SchemaInferenceStack stack) {
        // Accurate for most subclasses
        stack.enterSchemaTree(dataSchemaNode.getQName());
    }

    static AbstractContext fromSchemaAndQNameChecked(final DataNodeContainer schema, final QName child) {
        return lenientOf(findChildSchemaNode(schema, child));
    }

    private static DataSchemaNode findChildSchemaNode(final DataNodeContainer parent, final QName child) {
        final var potential = parent.dataChildByName(child);
        return potential == null ? findChoice(Iterables.filter(parent.getChildNodes(), ChoiceSchemaNode.class), child)
                : potential;
    }

    // FIXME: this looks like it should be a Predicate on a stream with findFirst()
    private static ChoiceSchemaNode findChoice(final Iterable<ChoiceSchemaNode> choices, final QName child) {
        for (var choice : choices) {
            // FIXME: this looks weird: what are we looking for again?
            for (var caze : choice.getCases()) {
                if (findChildSchemaNode(caze, child) != null) {
                    return choice;
                }
            }
        }
        return null;
    }

    public static @NonNull AbstractContext of(final @NonNull DataSchemaNode schema) {
        return switch (schema) {
            case AnydataSchemaNode anydata -> new OpaqueContext(anydata);
            case AnyxmlSchemaNode anyxml -> new OpaqueContext(anyxml);
            case ChoiceSchemaNode choice -> new ChoiceContext(choice);
            case ContainerLike containerLike -> new ContainerContext(containerLike);
            case LeafSchemaNode leaf -> new LeafContext(leaf);
            case LeafListSchemaNode leafList -> new LeafListContext(leafList);
            case ListSchemaNode list -> fromListSchemaNode(list);
            default -> throw new IllegalStateException("Unhandled schema " + schema);
        };
    }

    // FIXME: do we tolerate null argument? do we tolerate unknown subclasses?
    private static @Nullable AbstractContext lenientOf(final @Nullable DataSchemaNode schema) {
        return switch (schema) {
            case AnydataSchemaNode anydata -> new OpaqueContext(anydata);
            case AnyxmlSchemaNode anyxml -> new OpaqueContext(anyxml);
            case ChoiceSchemaNode choice -> new ChoiceContext(choice);
            case ContainerLike containerLike -> new ContainerContext(containerLike);
            case LeafSchemaNode leaf -> new LeafContext(leaf);
            case LeafListSchemaNode leafList -> new LeafListContext(leafList);
            case ListSchemaNode list -> fromListSchemaNode(list);
            case null, default -> null;
        };
    }

    private static @NonNull AbstractContext fromListSchemaNode(final ListSchemaNode potential) {
        final var keyDefinition = potential.getKeyDefinition();
        return keyDefinition.isEmpty() ? new ListContext(potential) : new MapContext(potential);
    }
}
