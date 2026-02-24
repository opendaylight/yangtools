/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;

@NonNullByDefault
record EnumTypeObjectGenerator(EnumTypeObjectArchetype type) implements Generator {
    EnumTypeObjectGenerator {
        requireNonNull(type);
    }

    @Override
    public String generate() {
        return new EnumTypeObjectTemplate(type).generate();
    }

    @Override
    public String getUnitName() {
        return type.simpleName();
    }
}
