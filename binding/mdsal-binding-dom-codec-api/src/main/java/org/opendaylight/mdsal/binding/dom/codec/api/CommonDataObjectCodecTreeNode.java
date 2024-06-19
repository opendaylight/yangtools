/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Common interface shared between {@link BindingDataObjectCodecTreeNode} and {@link BindingAugmentationCodecTreeNode}.
 * This interface should never be implemented on its own.
 *
 * @param <T> DataObject type
 */
@Beta
public interface CommonDataObjectCodecTreeNode<T extends DataObject>
        extends BindingDataObjectCodecTreeParent<Empty>, BindingObjectCodecTreeNode<T> {
    /**
     * Returns binding class of interface which represents API of current schema node. The result is same as invoking
     * {@link DataObject#implementedInterface()} on instance of data.
     *
     * @return interface which defines API of binding representation of data.
     */
    @Override
    @NonNull Class<T> getBindingClass();

    /**
     * Returns child context as if it was walked by {@link BindingStreamEventWriter}. This means that to enter case,
     * one must issue getChild(ChoiceClass).getChild(CaseClass).
     *
     * <p>
     * This method differs from {@link #getStreamChild(Class)}, that is less strict for interfaces representing
     * augmentation and cases, that may return {@link BindingCodecTreeNode} even if augmentation interface containing
     * same data was supplied and does not represent augmentation of this node.
     *
     * @param childClass Child class by Binding Stream navigation
     * @return Context of child or {@code null} is supplied class is not applicable in context.
     * @throws NullPointerException if {@code childClass} is {@code null}
     */
    <E extends DataObject> @Nullable CommonDataObjectCodecTreeNode<E> streamChild(@NonNull Class<E> childClass);

    /**
     * Returns nested node context using supplied YANG Instance Identifier.
     *
     * @param child
     *            Yang Instance Identifier Argument
     * @return Context of child
     * @throws IllegalArgumentException
     *             If supplied argument does not represent valid child.
     */
    @NonNull BindingCodecTreeNode yangPathArgumentChild(YangInstanceIdentifier.@NonNull PathArgument child);

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
    @NonNull CommonDataObjectCodecTreeNode<?> bindingPathArgumentChild(InstanceIdentifier.@NonNull PathArgument arg,
            @Nullable List<YangInstanceIdentifier.PathArgument> builder);

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
    YangInstanceIdentifier.@Nullable PathArgument serializePathArgument(InstanceIdentifier.@Nullable PathArgument arg);

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
    InstanceIdentifier.@Nullable PathArgument deserializePathArgument(
            YangInstanceIdentifier.@Nullable PathArgument arg);

    /**
     * Return a summary of addressability of potential children. Binding specification does not allow all DOM tree
     * elements to be directly addressed, which means some recursive tree operations, like data tree changes do not
     * have a one-to-one mapping from DOM to binding in all cases. This method provides an optimization hint to guide
     * translation of data structures, allowing for fast paths when all children are known to either be addressable
     * or non-addressable.
     *
     * @return Summary children addressability.
     */
    @NonNull ChildAddressabilitySummary getChildAddressabilitySummary();

    /**
     * Enumeration of possible addressability attribute of all children.
     */
    enum ChildAddressabilitySummary {
        /**
         * All children are addressable.
         */
        ADDRESSABLE,
        /**
         * All children are non-addressable, including the case when this node does not have any children.
         */
        UNADDRESSABLE,
        /**
         * Mixed children, some are addressable and some are not.
         */
        MIXED
    }
}
