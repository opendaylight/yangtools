/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.context;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
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

    Collection<@NonNull QName> qnameIdentifiers() {
        return List.of(dataSchemaNode.getQName());
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

    static final @Nullable DataSchemaNode childByName(final DataNodeContainer parent, final QName child) {
        final var childSchema = parent.dataChildByName(child);
        return childSchema != null ? childSchema : choiceByName(parent, child);
    }

    private static @Nullable ChoiceSchemaNode choiceByName(final DataNodeContainer parent, final QName child) {
        for (var dataChild : parent.getChildNodes()) {
            if (dataChild instanceof ChoiceSchemaNode choice) {
                for (var inCase : choice.getCases()) {
                    if (childByName(inCase, child) != null) {
                        return choice;
                    }
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
            case ListSchemaNode list -> {
                final var keyDefinition = list.getKeyDefinition();
                yield keyDefinition.isEmpty() ? new ListContext(list) : new MapContext(list);
            }
            default -> throw new IllegalStateException("Unhandled schema " + schema);
        };
    }
}
