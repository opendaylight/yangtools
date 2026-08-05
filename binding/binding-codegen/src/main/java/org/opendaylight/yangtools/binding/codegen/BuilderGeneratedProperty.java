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
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.api.Type;

@NonNullByDefault
final class BuilderGeneratedProperty implements GeneratedProperty {
    final MethodSignature getter;
    private final String name;

    BuilderGeneratedProperty(final String name, final MethodSignature getter) {
        this.name = requireNonNull(name);
        this.getter = requireNonNull(getter);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getReturnType() {
        return getter.returnType();
    }

    ValueMechanics getMechanics() {
        return getter.mechanics();
    }

    String getGetterName() {
        return getter.name();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof BuilderGeneratedProperty other
            && name.equals(other.name) && getter.equals(other.getter);
    }

    @Override
    public boolean isReadOnly() {
        throw uoe();
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException("Method not supported");
    }
}
