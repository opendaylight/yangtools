/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;

@NonNullByDefault
record InterfaceGenerator(GeneratedType type) implements Generator {
    InterfaceGenerator(final GeneratedType type) {
        this.type = requireNonNull(type);
    }

    @Override
    public GeneratedType type() {
        return type;
    }

    @Override
    public String generate() {
        return switch (type) {
            // FIXME: split out into separate generator
            case DataRootArchetype dataRoot -> new DataRootTemplate(dataRoot).generate();
            default -> new InterfaceTemplate(type).generate();
        };
    }

    @Override
    public String getUnitName() {
        return type.name().simpleName();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type).toString();
    }
}
