/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.util.impl.legacy.AbstractMixinContextNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Schema derived data providing necessary information for mapping between
 * {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode} and serialization format defined in RFC6020,
 * since the mapping is not one-to-one.
 */
// FIXME: YANGTOOLS-1413: this really should be an interface, as there is a ton of non-trivial composition going on:
//        - getDataSchemaNode() cannot return AugmentationSchemaNode, which is guarded by isMixinNode() and users should
//          not be touching mixin details anyway
public interface DataSchemaContextNode {
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
    sealed interface PathMixin permits AbstractMixinContextNode {
        /**
         * The mixed-in {@link PathArgument}.
         *
         * @return Mixed-in PathArgument
         */
        @NonNull PathArgument mixinPathArgument();
    }

    @NonNull DataSchemaNode getDataSchemaNode();

    // FIXME: YANGTOOLS-1413: this idea is wrong -- if does the wrong thing for items of leaf-list and keyed list
    //                        because those identifiers need a value.
    @NonNull PathArgument pathArgument();

    // FIXME: YANGTOOLS-1413: document this method and (most likely) split it out to a separate interface
    boolean isKeyedEntry();

    // FIXME: YANGTOOLS-1413: this is counter-intuitive: anydata/anyxml are considered non-leaf. This method needs
    //                        a better name and a proper description.
    boolean isLeaf();

    /**
     * Find a child node identifier by its {@link PathArgument}.
     *
     * @param child Child path argument
     * @return A child node, or null if not found
     */
    // FIXME: YANGTOOLS-1413: document PathArgument type mismatch, nullness and also rename it to 'childForArg'
    @Nullable DataSchemaContextNode getChild(PathArgument child);

    /**
     * Find a child node identifier by its {code data tree} {@link QName}. This method returns intermediate nodes
     * significant from {@link YangInstanceIdentifier} hierarchy of {@link PathArgument}s. If the returned node
     * indicates {@code true} via {@link #isMixin()}, it represents a {@link NormalizedNode} encapsulation which is
     * not visible in RFC7950 XML encoding, and a further call to this method with the same {@code child} argument will
     * provide the next step.
     *
     * @param child Child data tree QName
     * @return A child node, or null if not found
     */
    // FIXME: YANGTOOLS-1413: document child == null, also rename to 'childForQName'
    @Nullable DataSchemaContextNode getChild(QName child);

    /**
     * Find a child node as identified by a {@link YangInstanceIdentifier} relative to this node.
     *
     * @param path Path towards the child node
     * @return Child node if present, or empty when corresponding child is not found.
     * @throws NullPointerException if {@code path} is null
     */
    // FIXME: YANGTOOLS-1413: rename add a childForPath() method and rename this to 'findChildForPath'
    default @NonNull Optional<@NonNull DataSchemaContextNode> findChild(final @NonNull YangInstanceIdentifier path) {
        var currentOp = this;
        for (var arg : path.getPathArguments()) {
            currentOp = currentOp.getChild(arg);
            if (currentOp == null) {
                return Optional.empty();
            }
        }
        return Optional.of(currentOp);
    }

    /**
     * Attempt to enter a child {@link DataSchemaContextNode} towards the {@link DataSchemaNode} child identified by
     * specified {@code data tree} {@link QName}, adjusting provided {@code stack} with inference steps corresponding to
     * the transition to the returned node. The stack is expected to be correctly pointing at this node's schema,
     * otherwise the results of this method are undefined.
     *
     * @param stack {@link SchemaInferenceStack} to update
     * @param child Child QName
     * @return A DataSchemaContextNode on the path towards the specified child
     * @throws NullPointerException if any argument is {@code null}
     */
    @Nullable DataSchemaContextNode enterChild(SchemaInferenceStack stack, QName child);

    /**
     * Attempt to enter a child {@link DataSchemaContextNode} towards the {@link DataSchemaNode} child identified by
     * specified {@link PathArgument}, adjusting provided {@code stack} with inference steps corresponding to
     * the transition to the returned node. The stack is expected to be correctly pointing at this node's schema,
     * otherwise the results of this method are undefined.
     *
     * @param stack {@link SchemaInferenceStack} to update
     * @param child Child path argument
     * @return A DataSchemaContextNode for the specified child
     * @throws NullPointerException if any argument is {@code null}
     */
    @Nullable DataSchemaContextNode enterChild(SchemaInferenceStack stack, PathArgument child);
}
