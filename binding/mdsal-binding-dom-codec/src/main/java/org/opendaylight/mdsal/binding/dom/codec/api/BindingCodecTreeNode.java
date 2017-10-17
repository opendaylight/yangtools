/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

/**
 * Subtree codec specific to model subtree between Java Binding and NormalizedNode.
 */
@Beta
public interface BindingCodecTreeNode<T extends DataObject> extends BindingNormalizedNodeCodec<T> {

    /**
     * Returns binding class of interface which represents API of current schema node. The result is same as invoking
     * {@link DataObject#getImplementedInterface()} on instance of data.
     *
     * @return interface which defines API of binding representation of data.
     */
    @Nonnull
    Class<T> getBindingClass();

    /**
     * Returns child context as if it was walked by {@link BindingStreamEventWriter}. This means that to enter case,
     * one must issue getChild(ChoiceClass).getChild(CaseClass).
     *
     * @param childClass Child class by Biding Stream navigation
     * @return Context of child
     * @throws IllegalArgumentException
     *             If supplied child class is not valid in specified context.
     */
    @Nonnull
    <E extends DataObject> BindingCodecTreeNode<E> streamChild(@Nonnull Class<E> childClass);

    /**
     * Returns child context as if it was walked by {@link BindingStreamEventWriter}. This means that to enter case,
     * one must issue getChild(ChoiceClass).getChild(CaseClass).
     *
     * <p>
     * This method differs from {@link #streamChild(Class)}, that is less strict for interfaces representing
     * augmentation and cases, that may return {@link BindingCodecTreeNode} even if augmentation interface containing
     * same data was supplied and does not represent augmentation of this node.
     *
     * @param childClass
     * @return Context of child or Optional absent is supplied class is not
     *         applicable in context.
     */
    <E extends DataObject> Optional<? extends BindingCodecTreeNode<E>> possibleStreamChild(
            @Nonnull Class<E> childClass);

    /**
     * Returns nested node context using supplied YANG Instance Identifier.
     *
     * @param child
     *            Yang Instance Identifier Argument
     * @return Context of child
     * @throws IllegalArgumentException
     *             If supplied argument does not represent valid child.
     */
    @Nonnull
    BindingCodecTreeNode<?> yangPathArgumentChild(@Nonnull YangInstanceIdentifier.PathArgument child);

    /**
     * Returns nested node context using supplied Binding Instance Identifier and adds YANG instance identifiers to
     * the supplied list.
     *
     * @param arg
     *            Binding Instance Identifier Argument
     * @param builder
     *            Mutable instance of list, which is appended by YangInstanceIdentifiers
     *            as tree is walked. Use null if such side-product is not needed.
     * @return Context of child
     * @throws IllegalArgumentException
     *             If supplied argument does not represent valid child.
     */
    @Nonnull
    BindingCodecTreeNode<?> bindingPathArgumentChild(@Nonnull InstanceIdentifier.PathArgument arg,
            @Nullable List<YangInstanceIdentifier.PathArgument> builder);

    /**
     * Returns codec which uses caches serialization / deserialization results.
     *
     * <p>
     * Caching may introduce performance penalty to serialization / deserialization
     * but may decrease use of heap for repetitive objects.
     *
     * @param cacheSpecifier Set of objects, for which cache may be in place
     * @return Codec whihc uses cache for serialization / deserialization.
     */
    @Nonnull
    BindingNormalizedNodeCachingCodec<T> createCachingCodec(@Nonnull
            ImmutableCollection<Class<? extends DataObject>> cacheSpecifier);

    @Beta
    void writeAsNormalizedNode(T data, NormalizedNodeStreamWriter writer);

    /**
     * Serializes path argument for current node.
     *
     * @param arg Binding Path Argument, may be null if Binding Instance Identifier does not have
     *        representation for current node (e.g. choice or case).
     * @return Yang Path Argument, may be null if Yang Instance Identifier does not have
     *         representation for current node (e.g. case).
     * @throws IllegalArgumentException If supplied {@code arg} is not valid.
     */
    @Beta
    @Nullable YangInstanceIdentifier.PathArgument serializePathArgument(@Nullable InstanceIdentifier.PathArgument arg);

    /**
     * Deserializes path argument for current node.
     *
     * @param arg Yang Path Argument, may be null if Yang Instance Identifier does not have
     *         representation for current node (e.g. case).
     * @return Binding Path Argument, may be null if Binding Instance Identifier does not have
     *        representation for current node (e.g. choice or case).
     * @throws IllegalArgumentException If supplied {@code arg} is not valid.
     */
    @Beta
    @Nullable InstanceIdentifier.PathArgument deserializePathArgument(
            @Nullable YangInstanceIdentifier.PathArgument arg);

    Object getSchema();
}
