/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;

public final class InstanceIdentifierTypeBuilder extends AbstractRestrictedTypeBuilder<InstanceIdentifierTypeDefinition> {
    private boolean requireInstance;

    InstanceIdentifierTypeBuilder(final InstanceIdentifierTypeDefinition baseType, final SchemaPath path) {
        super(Preconditions.checkNotNull(baseType), path);
        this.requireInstance = baseType.requireInstance();
    }

    public void setRequireInstance(final boolean requireInstance) {
        if (this.requireInstance) {
            Preconditions.checkArgument(!requireInstance, "Cannot switch require-instance off in type %s", getPath());
        }

        this.requireInstance = requireInstance;
        touch();
    }

    @Override
    InstanceIdentifierTypeDefinition buildType() {
        if (requireInstance == getBaseType().requireInstance()) {
            return getBaseType();
        }

        return new RestrictedInstanceIdentifierType(getBaseType(), getPath(), getUnknownSchemaNodes(), requireInstance);
    }
}
