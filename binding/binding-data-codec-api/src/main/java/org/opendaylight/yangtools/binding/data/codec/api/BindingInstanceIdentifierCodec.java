/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.PropertyIdentifier;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;

@Beta
public interface BindingInstanceIdentifierCodec extends Immutable {
    /**
     * Translates supplied {@link YangInstanceIdentifier} into a {@link DataObjectReference}, if possible.
     *
     * @param domPath YANG Instance Identifier
     * @return a DataObjectReference, or {@code null} if the instance identifier is not representable.
     * @throws NullPointerException if domPath is {@code null}
     * @throws IllegalArgumentException if domPath is not valid.
     */
    // FIXME: Document MissingSchemaException being thrown?
    // FIXME: Document MissingSchemaForClassException being thrown?
    <T extends DataObject> @Nullable DataObjectReference<T> toBinding(@NonNull YangInstanceIdentifier domPath);

    /**
     * Translates supplied {@link YangInstanceIdentifier} into a {@link BindingInstanceIdentifier}, if possible.
     * Identifiers which refer to {@link ChoiceNode}, {@link MapNode}, {@link UnkeyedListNode} and {@link LeafSetNode}
     * are impossible to translate into a {@link BindingInstanceIdentifier} because they are an addressing artifact
     * of {@link NormalizedNode} representation.
     *
     * @param domPath YANG Instance Identifier
     * @return BindingInstanceIdentifier, or {@code null} if the {@link YangInstanceIdentifier} is not representable.
     * @throws NullPointerException if domPath is {@code null}
     * @throws IllegalArgumentException if domPath is not valid.
     */
    // FIXME: Document MissingSchemaException being thrown?
    // FIXME: Document MissingSchemaForClassException being thrown?
    @Nullable BindingInstanceIdentifier toBindingInstanceIdentifier(@NonNull YangInstanceIdentifier domPath);

    /**
     * Translates supplied {@link DataObjectReference} into a {@link YangInstanceIdentifier}.
     *
     * @param bindingPath a data object reference
     * @return DOM Instance Identifier
     * @throws NullPointerException if bindingPath is null
     * @throws IllegalArgumentException if bindingPath is not valid.
     */
    // FIXME: Document MissingSchemaException being thrown
    // FIXME: Document MissingSchemaForClassException being thrown
    @NonNull YangInstanceIdentifier fromBinding(@NonNull DataObjectReference<?> bindingPath);

    /**
     * Translates supplied {@link DataObjectIdentifier} into a {@link YangInstanceIdentifier}.
     *
     * @param bindingPath a data object reference
     * @return DOM Instance Identifier
     * @throws NullPointerException if bindingPath is null
     * @throws IllegalArgumentException if bindingPath is not valid.
     */
    // FIXME: Document MissingSchemaException being thrown
    // FIXME: Document MissingSchemaForClassException being thrown
    default @NonNull YangInstanceIdentifier fromBinding(final @NonNull DataObjectIdentifier<?> bindingPath) {
        return fromBinding((DataObjectReference<?>) bindingPath);
    }

    /**
     * Translates supplied {@link PropertyIdentifier} into a {@link YangInstanceIdentifier}.
     *
     * @param bindingPath a property identifier
     * @return DOM Instance Identifier
     * @throws NullPointerException if bindingPath is null
     * @throws IllegalArgumentException if bindingPath is not valid.
     */
    // FIXME: Document MissingSchemaException being thrown
    // FIXME: Document MissingSchemaForClassException being thrown
    @NonNull YangInstanceIdentifier fromBinding(@NonNull PropertyIdentifier<?, ?> bindingPath);

    /**
     * Translates supplied {@link BindingInstanceIdentifier} into a {@link YangInstanceIdentifier}.
     *
     * @param bindingPath Binding Instance Identifier
     * @return DOM Instance Identifier
     * @throws NullPointerException if bindingPath is null
     * @throws IllegalArgumentException if bindingPath is not valid.
     */
    // FIXME: Document MissingSchemaException being thrown
    // FIXME: Document MissingSchemaForClassException being thrown
    default @NonNull YangInstanceIdentifier fromBinding(final @NonNull BindingInstanceIdentifier bindingPath) {
        return switch (bindingPath) {
            case DataObjectIdentifier<?> doi -> fromBinding(doi);
            case PropertyIdentifier<?, ?> pi -> fromBinding(pi);
        };
    }
}
