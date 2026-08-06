/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.GetterMethod;
import org.opendaylight.yangtools.binding.model.api.Type;

@NonNullByDefault
record BuilderProperty(String name, GetterMethod getter) {
    BuilderProperty {
        requireNonNull(name);
        requireNonNull(getter);
    }

    Type type() {
        return getter.returnType();
    }

    String fieldName() {
        return BaseTemplate.fieldName(name);
    }

    String getterName() {
        return getter.name();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof BuilderProperty other
            && name.equals(other.name) && getter.equals(other.getter);
    }
}
