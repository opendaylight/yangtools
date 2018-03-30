/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RequireInstanceRestrictedTypeDefinition;

@Beta
//FIXME: 3.0.0: this should require T to be a RequireInstanceRestrictedTypeDefinition
public abstract class RequireInstanceRestrictedTypeBuilder<T extends TypeDefinition<T>>
        extends AbstractRestrictedTypeBuilder<T> {

    private boolean requireInstance;

    RequireInstanceRestrictedTypeBuilder(final T baseType, final SchemaPath path) {
        super(baseType, path);
        requireInstance = baseType instanceof RequireInstanceRestrictedTypeDefinition
                ? ((RequireInstanceRestrictedTypeDefinition<?>)baseType).requireInstance() : true;
    }

    public final void setRequireInstance(final boolean requireInstance) {
        this.requireInstance = requireInstance;
        touch();
    }

    final boolean getRequireInstance() {
        return requireInstance;
    }
}
