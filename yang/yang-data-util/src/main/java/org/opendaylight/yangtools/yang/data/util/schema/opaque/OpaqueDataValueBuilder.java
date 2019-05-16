/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.schema.opaque;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataValue;

@Beta
public final class OpaqueDataValueBuilder extends AbstractOpaqueDataNodeBuilder<OpaqueDataValue> {
    private Object value;

    @Override
    public OpaqueDataValueBuilder withIdentifier(final NodeIdentifier identifier) {
        super.withIdentifier(identifier);
        return this;
    }

    @Override
    public OpaqueDataValueBuilder withValue(final Object newValue) {
        checkState(value == null, "Value has already been set to %s", value);
        value = requireNonNull(newValue);
        return this;
    }

    @Override
    public OpaqueDataValue build(final NodeIdentifier identifier) {
        checkState(value != null, "Value has not been set");
        return new ImmutableOpaqueDataValue(identifier, value);
    }
}
