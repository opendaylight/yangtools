/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.AttachedAnnotation;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

final class DefaultLegacyArchetype<S extends EffectiveStatement<?, ?>> implements LegacyArchetype<S> {
    private final @NonNull JavaTypeName name;
    private final @NonNull S statement;
    private final @NonNull List<AttachedAnnotation.ToType> annotations;
    private final @NonNull List<Type> implementsTypes;
    private final @NonNull List<Constant> constants;
    private final @NonNull List<MethodSignature> methodSignatures;
    private final @NonNull List<Archetype> enclosedTypes;

    DefaultLegacyArchetype(final LegacyArchetypeBuilder<S> builder) {
        name = builder.typeName();
        statement = builder.statement;
        annotations = builder.getAnnotations();
        implementsTypes = builder.getImplementsTypes();
        constants = builder.getConstants();
        methodSignatures = builder.getMethodDefinitions();
        enclosedTypes = List.copyOf(builder.getEnclosedTypes());
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
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    @NonNullByDefault
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        helper.add("name", name);

        addToStringAttribute(helper, "annotations", annotations);
        addToStringAttribute(helper, "implements", implementsTypes);
        addToStringAttribute(helper, "enclosedTypes", enclosedTypes);
        addToStringAttribute(helper, "constants", constants);
        addToStringAttribute(helper, "methods", methodSignatures);

        return helper;
    }

    @NonNullByDefault
    protected static void addToStringAttribute(final ToStringHelper helper, final String name,
            final @Nullable Collection<?> value) {
        if (value != null && !value.isEmpty()) {
            helper.add(name, value);
        }
    }
}
