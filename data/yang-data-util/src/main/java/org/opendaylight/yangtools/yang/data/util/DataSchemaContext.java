/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.ValueNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.Composite;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.SimpleValue;
import org.opendaylight.yangtools.yang.data.util.impl.context.AbstractCompositeContext;
import org.opendaylight.yangtools.yang.data.util.impl.context.AbstractContext;
import org.opendaylight.yangtools.yang.data.util.impl.context.AbstractPathMixinContext;
import org.opendaylight.yangtools.yang.data.util.impl.context.AbstractValueContext;
import org.opendaylight.yangtools.yang.data.util.impl.context.ContainerContext;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Schema derived data providing necessary information for mapping between
 * {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode} and serialization format defined in RFC6020,
 * since the mapping is not one-to-one.
 */
public sealed interface DataSchemaContext permits AbstractContext, Composite, SimpleValue {
    /**
     * A {@link DataSchemaContext} containing other {@link DataSchemaContext}s.
     */
    sealed interface Composite extends DataSchemaContext
            permits PathMixin, AbstractCompositeContext, MandatoryEnforcementPoint {
        /**
         * Find a child node identifier by its {@link PathArgument}.
         *
         * @param arg Child path argument
         * @return A child node, or {@code null} if not found
         * @throws NullPointerException if {@code arg} is {@code null}
         */
        @Nullable DataSchemaContext childByArg(PathArgument arg);

        /**
         * Find a child node identifier by its {code data tree} {@link QName}. This method returns intermediate nodes
         * significant from {@link YangInstanceIdentifier} hierarchy of {@link PathArgument}s. If the returned node
         * indicates is also a {@link PathMixin}, it represents a {@link NormalizedNode} encapsulation which is not
         * visible in RFC7950 XML encoding, and a further call to this method with the same {@code child} argument will
         * provide the next step.
         *
         * @param qname Child data tree QName
         * @return A child node, or {@code null} if not found
         * @throws NullPointerException if {@code arg} is {@code null}
         */
        @Nullable DataSchemaContext childByQName(QName qname);

        /**
         * Find a child node as identified by a {@link YangInstanceIdentifier} relative to this node.
         *
         * @param path Path towards the child node
         * @return Child node if present, or empty when corresponding child is not found.
         * @throws NullPointerException if {@code path} is {@code null}
         */
        default @Nullable DataSchemaContext childByPath(final @NonNull YangInstanceIdentifier path) {
            final var it = path.getPathArguments().iterator();
            if (!it.hasNext()) {
                return this;
            }

            var current = this;
            while (true) {
                final var child = current.childByArg(it.next());
                if (child == null || !it.hasNext()) {
                    return child;
                }
                if (!(child instanceof Composite compositeChild)) {
                    return null;
                }
                current = compositeChild;
            }
        }

        /**
         * Attempt to enter a child {@link DataSchemaContext} towards the {@link DataSchemaNode} child identified by
         * specified {@code data tree} {@link QName}, adjusting provided {@code stack} with inference steps
         * corresponding to the transition to the returned node. The stack is expected to be correctly pointing at this
         * node's schema, otherwise the results of this method are undefined.
         *
         * @param stack {@link SchemaInferenceStack} to update
         * @param child Child QName
         * @return A DataSchemaContextNode on the path towards the specified child
         * @throws NullPointerException if any argument is {@code null}
         */
        @Nullable DataSchemaContext enterChild(SchemaInferenceStack stack, QName child);

        /**
         * Attempt to enter a child {@link DataSchemaContext} towards the {@link DataSchemaNode} child identified by
         * specified {@link PathArgument}, adjusting provided {@code stack} with inference steps corresponding to
         * the transition to the returned node. The stack is expected to be correctly pointing at this node's schema,
         * otherwise the results of this method are undefined.
         *
         * @param stack {@link SchemaInferenceStack} to update
         * @param child Child path argument
         * @return A DataSchemaContextNode for the specified child
         * @throws NullPointerException if any argument is {@code null}
         */
        @Nullable DataSchemaContext enterChild(SchemaInferenceStack stack, PathArgument child);
    }

    /**
    * This node is a {@link NormalizedNode} intermediate, not represented in RFC7950 XML encoding. This is typically
    * one of
    * <ul>
    *   <li>{@link ChoiceNode} backed by a {@link ChoiceSchemaNode}, or</li>
    *   <li>{@link LeafSetNode} backed by a {@link LeafListSchemaNode}, or</li>
    *   <li>{@link MapNode} backed by a {@link ListSchemaNode} with a non-empty
    *       {@link ListSchemaNode#getKeyDefinition()}, or</li>
    *   <li>{@link UnkeyedListNode} backed by a {@link ListSchemaNode} with an empty
    *       {@link ListSchemaNode#getKeyDefinition()}</li>
    * </ul>
    *
    * <p>
    * This trait is important for XML codec, but also for JSON encoding of {@link YangInstanceIdentifier}.
    */
    sealed interface PathMixin extends Composite permits AbstractPathMixinContext {
        /**
         * The mixed-in {@link NodeIdentifier}.
         *
         * @return Mixed-in NodeIdentifier
         */
        default @NonNull NodeIdentifier mixinPathStep() {
            return getPathStep();
        }
    }

    /**
     * Marker interface for contexts which boil down to a simple, not-structured {@link ValueNode}. This can be one of
     * <ul>
    *   <li>{@link LeafNode} backed by a {@link LeafSchemaNode}, or</li>
    *   <li>{@link LeafSetNode} backed by a {@link LeafListSchemaNode}</li>
     * </ul>
     *
     * <p>
     * This trait interface is exposed for determining that the corresponding {@link TypeDefinition} of the normalized
     * body.
     */
    sealed interface SimpleValue extends DataSchemaContext permits AbstractValueContext {
        /**
         * Return the {@link TypeDefinition} of the type backing this context.
         *
         * @return A {@link TypeDefinition}
         */
        // FIXME: YANGTOOLS-1528: return yang.data.api.type.NormalizedType
        @NonNull TypeDefinition<?> type();
    }

    sealed interface MandatoryEnforcementPoint extends Composite permits ContainerContext.WithMandatory {

        void enforceMandatory(boolean configFalse, NormalizedNode data);
    }

    /**
     * Get a {@link DataSchemaContext} for a particular {@link DataSchemaNode}.
     *
     * @param schema Backing DataSchemaNode
     * @return A {@link DataSchemaContext}
     * @throws NullPointerException if {@code schema} is {@code null}
     * @throws IllegalStateException if {@code schema} is not handled
     */
    static @NonNull DataSchemaContext of(final DataSchemaNode schema) {
        return AbstractContext.of(requireNonNull(schema));
    }

    @NonNull DataSchemaNode dataSchemaNode();

    /**
     * Return the fixed {@link YangInstanceIdentifier} step, if available. This method returns {@code null} for contexts
     * like {@link MapEntryNode} and {@link LeafSetEntryNode}, where the step depends on the actual node value.
     *
     * @return A {@link NodeIdentifier}, or {@code null}
     */
    @Nullable NodeIdentifier pathStep();

    /**
     * Return the fixed {@link YangInstanceIdentifier} step.
     *
     * @return A {@link NodeIdentifier}
     * @throws UnsupportedOperationException if this node does not have fixed step
     */
    default @NonNull NodeIdentifier getPathStep() {
        final var arg = pathStep();
        if (arg != null) {
            return arg;
        }
        throw new UnsupportedOperationException(this + " does not have a fixed path step");
    }
}
