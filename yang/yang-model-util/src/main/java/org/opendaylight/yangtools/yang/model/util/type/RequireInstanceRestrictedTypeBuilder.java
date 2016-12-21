/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

@Beta
public abstract class RequireInstanceRestrictedTypeBuilder<T extends TypeDefinition<T>>
        extends AbstractRestrictedTypeBuilder<T> {

    private boolean requireInstance;

    RequireInstanceRestrictedTypeBuilder(final T baseType, final SchemaPath path) {
        super(baseType, path);
    }

    public final void setRequireInstance(final boolean requireInstance) {
        if (this.requireInstance) {
            Preconditions.checkArgument(requireInstance, "Cannot switch off require-instance in type %s", getPath());
        }

        this.requireInstance = requireInstance;
        touch();
    }

    final boolean getRequireInstance() {
        return requireInstance;
    }
}
