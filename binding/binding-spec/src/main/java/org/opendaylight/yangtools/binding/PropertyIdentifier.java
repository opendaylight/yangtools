/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.impl.AppendIterable;

/**
 * A {@link BindingInstanceIdentifier} pointing to a {@link DataContainer} property via a {@link DataObjectIdentifier}.
 */
public record PropertyIdentifier<C extends DataObject, V>(
        @NonNull DataObjectIdentifier<C> container,
        @NonNull ExactPropertyStep<C, V> property) implements BindingInstanceIdentifier {
    public PropertyIdentifier {
        if (!property.containerType().equals(container.lastStep().type())) {
            throw new IllegalArgumentException("Mismatched " + container + " and " + property);
        }
    }

    @Override
    public Iterable<? extends @NonNull Step> steps() {
        return new AppendIterable<>(container.steps(), property);
    }

    @Override
    public <E extends EntryObject<E, K>, K extends Key<E>> K firstKeyOf(final Class<@NonNull E> listItem) {
        return container.firstKeyOf(listItem);
    }
}
