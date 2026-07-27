/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@NonNullByDefault
final class DefaultLegacyArchetype<S extends EffectiveStatement<?, ?>> implements LegacyArchetype<S> {
    private final JavaTypeName name;
    private final S statement;
    private final List<AttachedAnnotation.ToType> annotations;
    private final List<Type> implementsTypes;
    private final List<Constant> constants;
    private final List<MethodSignature> methodSignatures;
    private final List<Archetype> enclosedTypes;

    DefaultLegacyArchetype(final JavaTypeName name, final S statement,
            final List<AttachedAnnotation.ToType> annotations, final List<Type> implementsTypes,
            final List<Constant> constants, final List<MethodSignature> methodSignatures,
            final List<Archetype> enclosedTypes) {
        this.name = requireNonNull(name);
        this.statement = requireNonNull(statement);
        this.annotations = requireNonNull(annotations);
        this.implementsTypes = requireNonNull(implementsTypes);
        this.constants = requireNonNull(constants);
        this.methodSignatures = requireNonNull(methodSignatures);
        this.enclosedTypes = requireNonNull(enclosedTypes);
    }

    @Override
    public S statement() {
        return statement;
    }

    @Override
    public JavaTypeName name() {
        return name;
    }

    @Override
    public List<AttachedAnnotation.ToType> annotations() {
        return annotations;
    }

    @Override
    public List<Type> getImplements() {
        return implementsTypes;
    }

    @Override
    public List<Archetype> enclosedTypes() {
        return enclosedTypes;
    }

    @Override
    public List<Constant> getConstantDefinitions() {
        return constants;
    }

    @Override
    public List<MethodSignature> getMethodDefinitions() {
        return methodSignatures;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Type other && name.equals(other.name());
    }

    @Override
    public String toString() {
        final var helper = MoreObjects.toStringHelper(this).add("name", name);
        addNonEmpty(helper, "annotations", annotations);
        addNonEmpty(helper, "implements", implementsTypes);
        addNonEmpty(helper, "enclosedTypes", enclosedTypes);
        addNonEmpty(helper, "constants", constants);
        addNonEmpty(helper, "methods", methodSignatures);
        return helper.toString();
    }

    private static void addNonEmpty(final ToStringHelper helper, final String name, final List<?> list) {
        if (!list.isEmpty()) {
            helper.add(name, list);
        }
    }
}
