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
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@Beta
public interface BindingInstanceIdentifierCodec extends Immutable {
    /**
     * Translates supplied {@link YangInstanceIdentifier} into an {@link BindingInstanceIdentifier}.
     *
     * @param domPath YANG Instance Identifier
     * @return a {@link BindingInstanceIdentifier}
     * @throws NullPointerException if domPath is null
     * @throws IllegalArgumentException if domPath is not valid.
     */
    // FIXME: Document MissingSchemaException being thrown?
    // FIXME: Document MissingSchemaForClassException being thrown?
    @NonNull BindingInstanceIdentifier dataToBinding(@NonNull YangInstanceIdentifier domPath);

    /**
     * Translates supplied {@link YangInstanceIdentifier} into an {@link DataObjectReference}, if possible.
     *
     * @param domPath YANG Instance Identifier
     * @return Binding Instance Identifier, or null if the instance identifier is not representable.
     * @throws NullPointerException if domPath is null
     * @throws IllegalArgumentException if domPath is not valid.
     */
    // FIXME: Document MissingSchemaException being thrown?
    // FIXME: Document MissingSchemaForClassException being thrown?
    default @Nullable DataObjectReference<?> toBinding(final @NonNull YangInstanceIdentifier domPath) {
        return switch (dataToBinding(domPath)) {
            case DataObjectReference<?> ref -> ref;
            default -> null;
        };
    }

    /**
     * Translates supplied {@link BindingInstanceIdentifier} into {@link YangInstanceIdentifier}.
     *
     * @param bindingPath Binding Instance Identifier
     * @return DOM Instance Identifier
     * @throws NullPointerException if bindingPath is null
     * @throws IllegalArgumentException if bindingPath is not valid.
     */
    // FIXME: Document MissingSchemaException being thrown
    // FIXME: Document MissingSchemaForClassException being thrown
    @NonNull YangInstanceIdentifier fromBinding(@NonNull BindingInstanceIdentifier bindingPath);
}
