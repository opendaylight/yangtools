/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;

@NonNullByDefault
final class BuilderGeneratedProperty implements GeneratedProperty {
    private final MethodSignature getter;
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
        return getter.getReturnType();
    }

    ValueMechanics getMechanics() {
        return getter.getMechanics();
    }

    String getGetterName() {
        return getter.getName();
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
    public @Nullable TypeMemberComment getComment() {
        throw uoe();
    }

    @Override
    public List<AnnotationType> getAnnotations() {
        throw uoe();
    }

    @Override
    public AccessModifier getAccessModifier() {
        throw uoe();
    }

    @Override
    public boolean isStatic() {
        throw uoe();
    }

    @Override
    public boolean isFinal() {
        throw uoe();
    }

    @Override
    public String getValue() {
        throw uoe();
    }

    @Override
    public boolean isReadOnly() {
        throw uoe();
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException("Method not supported");
    }
}
