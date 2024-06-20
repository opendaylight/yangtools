/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.lib.Augmentation;
import org.opendaylight.yangtools.binding.lib.DataObject;
import org.opendaylight.yangtools.binding.lib.InstanceIdentifier;
import org.opendaylight.yangtools.binding.lib.YangData;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Navigable tree representing hierarchy of Binding to Normalized Node codecs. This navigable tree is associated to
 * a concrete set of YANG models, represented by SchemaContext and provides access to subtree specific serialization
 * context.
 */
// TODO: Add more detailed documentation
public interface BindingCodecTree extends BindingDataObjectCodecTreeParent<Empty> {
    /**
     * A DTO holding a {@link CommonDataObjectCodecTreeNode} and the corresponding {@link YangInstanceIdentifier}.
     *
     * @param <T> {@link DataObject} type
     * @param codec A CommonDataObjectCodecTreeNode handling the translation
     * @param path A {@link YangInstanceIdentifier} corresponding to the backing Normalized Node
     */
    record CodecWithPath<T extends DataObject>(
            @NonNull CommonDataObjectCodecTreeNode<T> codec,
            @NonNull YangInstanceIdentifier path) {
        public CodecWithPath {
            requireNonNull(codec);
            requireNonNull(path);
        }
    }

    /**
     * Look up the codec for specified augmentation path.
     *
     * @param <A> DataObject type
     * @param path Binding path
     * @return A {@link BindingAugmentationCodecTreeNode}
     * @throws NullPointerException if {@code path} is {@code null}
     * @throws IllegalArgumentException if the codec cannot be resolved
     */
    <A extends Augmentation<?>> @NonNull BindingAugmentationCodecTreeNode<A> getAugmentationCodec(
        InstanceIdentifier<A> path);

    /**
     * Look up the codec for specified ordinary DataObject path.
     *
     * @param <T> DataObject type
     * @param path Binding path
     * @return A {@link BindingDataObjectCodecTreeNode}
     * @throws NullPointerException if {@code path} is {@code null}
     * @throws IllegalArgumentException if the codec cannot be resolved or refers to an Augmentation
     */
    <T extends DataObject> @NonNull BindingDataObjectCodecTreeNode<T> getDataObjectCodec(InstanceIdentifier<T> path);

    /**
     * Look up the codec for specified path, constructing the {@link YangInstanceIdentifier} corresponding to it.
     *
     * @param <T> DataObject type
     * @param path Binding path
     * @return A {@link CodecWithPath}
     * @throws NullPointerException if {@code path} is {@code null}
     * @throws IllegalArgumentException if the codec cannot be resolved
     */
    <T extends DataObject> @NonNull CodecWithPath<T> getSubtreeCodecWithPath(InstanceIdentifier<T> path);

    /**
     * Look up the codec for specified path.
     *
     * @param <T> DataObject type
     * @param path Binding path
     * @return A {@link BindingDataObjectCodecTreeNode}
     * @throws NullPointerException if {@code path} is {@code null}
     * @throws IllegalArgumentException if the codec cannot be resolved
     */
    <T extends DataObject> @NonNull CommonDataObjectCodecTreeNode<T> getSubtreeCodec(InstanceIdentifier<T> path);

    /**
     * Look up a codec by its {@link YangInstanceIdentifier} path.
     *
     * @param path A non-empty {@link YangInstanceIdentifier}
     * @return A {@link BindingCodecTreeNode}
     * @throws NullPointerException if {@code path} is {@code null}
     * @throws IllegalArgumentException if {@code path} is empty
     */
    @Nullable BindingCodecTreeNode getSubtreeCodec(YangInstanceIdentifier path);

    // FIXME: NonNull and throwing exception
    @Nullable BindingCodecTreeNode getSubtreeCodec(Absolute path);

    /**
     * Get the {@link BindingIdentityCodec} associated with this tree.
     *
     * @return A BindingIdentityCodec instance.
     */
    @NonNull BindingIdentityCodec getIdentityCodec();

    /**
     * Get the {@link BindingInstanceIdentifierCodec} associated with this tree.
     *
     * @return A BindingInstanceIdentifierCodec instance.
     */
    @NonNull BindingInstanceIdentifierCodec getInstanceIdentifierCodec();

    /**
     * Get the {@link BindingYangDataCodecTreeNode} corresponding to a particular generated {@link YangData} type.
     *
     * @param <T> {@link YangData} type
     * @param yangDataClass Class object of {@link YangData} type
     * @return A {@link BindingYangDataCodecTreeNode}
     * @throws NullPointerException if {@code yangDataClass} is {@code null}
     * @throws IllegalArgumentException if the specified type is not known
     */
    <T extends YangData<T>> @NonNull BindingYangDataCodecTreeNode<T> getYangDataCodec(Class<T> yangDataClass);

    /**
     * Get the {@link BindingYangDataCodecTreeNode} corresponding to a particular {@link YangDataName}.
     *
     * @param yangDataName a {@link YangDataName}
     * @return A {@link BindingYangDataCodecTreeNode}
     * @throws NullPointerException if {@code yangDataName} is {@code null}
     * @throws IllegalArgumentException if the specified name is not known
     */
    @NonNull BindingYangDataCodecTreeNode<?> getYangDataCodec(YangDataName yangDataName);
}
