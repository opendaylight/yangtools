/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import java.util.List;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

public final class XtendHelper {
    private XtendHelper() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    public static Iterable<TypeDefinition> getTypes(final UnionTypeDefinition definition) {
        return (List) definition.getTypes();
    }
}
