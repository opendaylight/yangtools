/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.type.RequireInstanceRestrictedTypeDefinition;

@Beta
public abstract class RequireInstanceRestrictedTypeBuilder<T extends RequireInstanceRestrictedTypeDefinition<T>>
        extends AbstractRestrictedTypeBuilder<T> {
    private boolean requireInstance;

    RequireInstanceRestrictedTypeBuilder(final T baseType, final QName qname) {
        super(baseType, qname);
        requireInstance = baseType == null || baseType.requireInstance();
    }

    public final void setRequireInstance(final boolean requireInstance) {
        this.requireInstance = requireInstance;
        touch();
    }

    final boolean getRequireInstance() {
        return requireInstance;
    }
}
