/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadata;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;

@Beta
public abstract class AbstractAnydataNodeDataWithSchema<T> extends SimpleNodeDataWithSchema<AnyDataSchemaNode> {
    private NormalizedMetadata metadata;

    protected AbstractAnydataNodeDataWithSchema(final AnyDataSchemaNode dataSchemaNode) {
        super(dataSchemaNode);
    }

    @Override
    public T getValue() {
        return objectModelClass().cast(super.getValue());
    }

    @Override
    public void setValue(final Object value) {
        final Class<T> clazz = objectModelClass();
        checkArgument(clazz.isInstance(value), "Value %s is not compatible with %s", clazz);
        super.setValue(value);
    }

    public final NormalizedMetadata getMetadata() {
        return metadata;
    }

    public final void setMetadata(final NormalizedMetadata value) {
        checkState(metadata == null, "Metadata already set to %s", metadata);
        metadata = value;
    }

    protected abstract Class<T> objectModelClass();
}
