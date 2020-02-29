/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@Beta
public interface BindingInstanceIdentifierCodec extends Immutable {
    /**
     * Translates supplied {@link YangInstanceIdentifier} into an {@link InstanceIdentifier}, if possible.
     *
     * @param domPath YANG Instance Identifier
     * @return Binding Instance Identifier, or null if the instance identifier is not representable.
     * @throws NullPointerException if domPath is null
     */
    <T extends DataObject> @Nullable InstanceIdentifier<T> toBinding(@NonNull YangInstanceIdentifier domPath);

    /**
     * Translates supplied {@link InstanceIdentifier} into {@link YangInstanceIdentifier}.
     *
     * @param bindingPath Binding Instance Identifier
     * @return DOM Instance Identifier
     * @throws NullPointerException if bindingPath is null
     * @throws IllegalArgumentException if bindingPath is not valid.
     */
    @NonNull YangInstanceIdentifier fromBinding(@NonNull InstanceIdentifier<?> bindingPath);
}
