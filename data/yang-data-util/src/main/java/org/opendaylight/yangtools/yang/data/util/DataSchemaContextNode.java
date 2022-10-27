/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Schema derived data providing necessary information for mapping between
 * {@link org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode} and serialization format defined in RFC6020,
 * since the mapping is not one-to-one.
 *
 * @param <T> Path Argument type
 */
// FIXME: YANGTOOLS-1413: this really should be an interface, as there is a ton of non-trivial composition going on:
//        - getDataSchemaNode() cannot return AugmentationSchemaNode, which is guarded by isMixinNode() and users should
//          not be touching mixin details anyway
//        - the idea of getIdentifier() is wrong -- if does the wrong thing for items of leaf-list and keyed list
//          because those identifiers need a value. We also do not expect users to store the results in a Map, which
//          defeats the idea of Identifiable
//        - the generic argument is really an implementation detail and we really would like to also make dataSchemaNode
//          (or rather: underlying SchemaNode) an argument. Both of these are not something users can influence and
//          therefore we should not burden them with <?> on each reference to this class
public abstract class DataSchemaContextNode<T extends PathArgument> extends AbstractSimpleIdentifiable<T> {
    // FIXME: this can be null only for AugmentationContextNode and in that case the interior part is handled by a
    //        separate field in DataContainerContextNode. We need to re-examine our base interface class hierarchy
    //        so that the underlying (effective in augment's case) SchemaNode is always available.
    private final DataSchemaNode dataSchemaNode;

    DataSchemaContextNode(final T identifier, final DataSchemaNode schema) {
        super(identifier);
        this.dataSchemaNode = schema;
    }

    // FIXME: remove this constructor. Once we do, adjust 'enterChild' visibility to package-private
    @Deprecated(forRemoval = true, since = "8.0.2")
    protected DataSchemaContextNode(final T identifier, final SchemaNode schema) {
        this(identifier, schema instanceof DataSchemaNode ? (DataSchemaNode) schema : null);
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
     * @return {@code} false if this node corresponds to an XML element, or {@code true} if it is an encapsulation node.
     */
    public boolean isMixin() {
        return false;
    }

    // FIXME: document this method
    public boolean isKeyedEntry() {
        return false;
    }

    // FIXME: this is counter-intuitive: anydata/anyxml are considered non-leaf. This method needs a better name and
    //        a proper description.
    public abstract boolean isLeaf();

    protected Set<QName> getQNameIdentifiers() {
        return ImmutableSet.of(getIdentifier().getNodeType());
    }

    /**
     * Find a child node identifier by its {@link PathArgument}.
     *
     * @param child Child path argument
     * @return A child node, or null if not found
     */
    // FIXME: document PathArgument type mismatch
    public abstract @Nullable DataSchemaContextNode<?> getChild(PathArgument child);

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
    // FIXME: document child == null
    public abstract @Nullable DataSchemaContextNode<?> getChild(QName child);

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
    public final @Nullable DataSchemaContextNode<?> enterChild(final SchemaInferenceStack stack, final QName child) {
        return enterChild(requireNonNull(child), requireNonNull(stack));
    }

    // FIXME: make this method package-private once the protected constructor is gone
    protected abstract @Nullable DataSchemaContextNode<?> enterChild(@NonNull QName child,
        @NonNull SchemaInferenceStack stack);

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
    public final @Nullable DataSchemaContextNode<?> enterChild(final SchemaInferenceStack stack,
            final PathArgument child) {
        return enterChild(requireNonNull(child), requireNonNull(stack));
    }

    // FIXME: make this method package-private once the protected constructor is gone
    protected abstract @Nullable DataSchemaContextNode<?> enterChild(@NonNull PathArgument child,
        @NonNull SchemaInferenceStack stack);

    /**
     * Push this node into specified {@link SchemaInferenceStack}.
     *
     * @param stack {@link SchemaInferenceStack}
     */
    // FIXME: make this method package-private once the protected constructor is gone
    protected void pushToStack(final @NonNull SchemaInferenceStack stack) {
        // Accurate for most subclasses
        stack.enterSchemaTree(getIdentifier().getNodeType());
    }

    // FIXME: final
    public @Nullable DataSchemaNode getDataSchemaNode() {
        return dataSchemaNode;
    }

    /**
     * Find a child node as identified by a {@link YangInstanceIdentifier} relative to this node.
     *
     * @param path Path towards the child node
     * @return Child node if present, or empty when corresponding child is not found.
     * @throws NullPointerException if {@code path} is null
     */
    public final @NonNull Optional<@NonNull DataSchemaContextNode<?>> findChild(
            final @NonNull YangInstanceIdentifier path) {
        DataSchemaContextNode<?> currentOp = this;
        for (PathArgument arg : path.getPathArguments()) {
            currentOp = currentOp.getChild(arg);
            if (currentOp == null) {
                return Optional.empty();
            }
        }
        return Optional.of(currentOp);
    }

    static DataSchemaNode findChildSchemaNode(final DataNodeContainer parent, final QName child) {
        final DataSchemaNode potential = parent.dataChildByName(child);
        return potential == null ? findChoice(Iterables.filter(parent.getChildNodes(), ChoiceSchemaNode.class), child)
                : potential;
    }

    static DataSchemaContextNode<?> fromSchemaAndQNameChecked(final DataNodeContainer schema, final QName child) {
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

    static @NonNull DataSchemaContextNode<?> of(final @NonNull DataSchemaNode schema) {
        if (schema instanceof ContainerLike) {
            return new ContainerContextNode((ContainerLike) schema);
        } else if (schema instanceof ListSchemaNode) {
            return fromListSchemaNode((ListSchemaNode) schema);
        } else if (schema instanceof LeafSchemaNode) {
            return new LeafContextNode((LeafSchemaNode) schema);
        } else if (schema instanceof ChoiceSchemaNode) {
            return new ChoiceNodeContextNode((ChoiceSchemaNode) schema);
        } else if (schema instanceof LeafListSchemaNode) {
            return fromLeafListSchemaNode((LeafListSchemaNode) schema);
        } else if (schema instanceof AnydataSchemaNode) {
            return new AnydataContextNode((AnydataSchemaNode) schema);
        } else if (schema instanceof AnyxmlSchemaNode) {
            return new AnyXmlContextNode((AnyxmlSchemaNode) schema);
        } else {
            throw new IllegalStateException("Unhandled schema " + schema);
        }
    }

    // FIXME: do we tolerate null argument? do we tolerate unknown subclasses?
    static @Nullable DataSchemaContextNode<?> lenientOf(final @Nullable DataSchemaNode schema) {
        if (schema instanceof ContainerLike) {
            return new ContainerContextNode((ContainerLike) schema);
        } else if (schema instanceof ListSchemaNode) {
            return fromListSchemaNode((ListSchemaNode) schema);
        } else if (schema instanceof LeafSchemaNode) {
            return new LeafContextNode((LeafSchemaNode) schema);
        } else if (schema instanceof ChoiceSchemaNode) {
            return new ChoiceNodeContextNode((ChoiceSchemaNode) schema);
        } else if (schema instanceof LeafListSchemaNode) {
            return fromLeafListSchemaNode((LeafListSchemaNode) schema);
        } else if (schema instanceof AnydataSchemaNode) {
            return new AnydataContextNode((AnydataSchemaNode) schema);
        } else if (schema instanceof AnyxmlSchemaNode) {
            return new AnyXmlContextNode((AnyxmlSchemaNode) schema);
        } else {
            return null;
        }
    }

    /**
     * Get a {@link DataSchemaContextNode} for a particular {@link DataSchemaNode}.
     *
     * @param potential Backing DataSchemaNode
     * @return A {@link DataSchemaContextNode}, or null if the input is {@code null} or of unhandled type
     */
    @Deprecated(forRemoval = true, since = "8.0.2")
    public static @Nullable DataSchemaContextNode<?> fromDataSchemaNode(final DataSchemaNode potential) {
        return lenientOf(potential);
    }

    private static @NonNull DataSchemaContextNode<?> fromListSchemaNode(final ListSchemaNode potential) {
        var keyDefinition = potential.getKeyDefinition();
        if (keyDefinition.isEmpty()) {
            return new UnkeyedListMixinContextNode(potential);
        } else if (potential.isUserOrdered()) {
            return new OrderedMapMixinContextNode(potential);
        } else {
            return new UnorderedMapMixinContextNode(potential);
        }
    }

    private static @NonNull DataSchemaContextNode<?> fromLeafListSchemaNode(final LeafListSchemaNode potential) {
        if (potential.isUserOrdered()) {
            return new OrderedLeafListMixinContextNode(potential);
        }
        return new UnorderedLeafListMixinContextNode(potential);
    }

    /**
     * Return a DataSchemaContextNode corresponding to specified {@link EffectiveModelContext}.
     *
     * @param ctx EffectiveModelContext
     * @return A DataSchemaContextNode
     * @throws NullPointerException if {@code ctx} is null
     * @deprecated Use {@link DataSchemaContextTree#from(EffectiveModelContext)} and
     *             {@link DataSchemaContextTree#getRoot()} instead.
     */
    @Deprecated(forRemoval = true, since = "8.0.2")
    public static @NonNull DataSchemaContextNode<?> from(final EffectiveModelContext ctx) {
        return new ContainerContextNode(ctx);
    }
}
