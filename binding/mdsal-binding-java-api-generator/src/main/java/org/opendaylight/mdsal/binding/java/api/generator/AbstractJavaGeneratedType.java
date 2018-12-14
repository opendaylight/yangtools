/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.concurrent.NotThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.Enumeration.Pair;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.WildcardType;

/**
 * Abstract class representing a generated type, either top-level or nested. It takes care of tracking references
 * to other Java types and resolving them as best as possible.
 *
 * @author Robert Varga
 */
@NonNullByDefault
@NotThreadSafe
abstract class AbstractJavaGeneratedType {
    private final Map<JavaTypeName, @Nullable String> nameCache = new HashMap<>();
    private final ImmutableMap<String, NestedJavaGeneratedType> enclosedTypes;
    private final ImmutableSet<String> conflictingNames;

    private final JavaTypeName name;

    AbstractJavaGeneratedType(final GeneratedType genType) {
        name = genType.getIdentifier();
        final Builder<String, NestedJavaGeneratedType> b = ImmutableMap.builder();
        for (GeneratedType type : Iterables.concat(genType.getEnclosedTypes(), genType.getEnumerations())) {
            b.put(type.getIdentifier().simpleName(), new NestedJavaGeneratedType(this, type));
        }
        enclosedTypes = b.build();

        final Set<String> cb = new HashSet<>();
        if (genType instanceof Enumeration) {
            ((Enumeration) genType).getValues().stream().map(Pair::getMappedName).forEach(cb::add);
        }
        // TODO: perhaps we can do something smarter to actually access the types
        collectAccessibleTypes(cb, genType);

        conflictingNames = ImmutableSet.copyOf(cb);
    }

    private void collectAccessibleTypes(final Set<String> set, final GeneratedType type) {
        for (Type impl : type.getImplements()) {
            if (impl instanceof GeneratedType) {
                final GeneratedType genType = (GeneratedType) impl;
                for (GeneratedType inner : Iterables.concat(genType.getEnclosedTypes(), genType.getEnumerations())) {
                    set.add(inner.getIdentifier().simpleName());
                }
                collectAccessibleTypes(set, genType);
            }
        }
    }

    final JavaTypeName getName() {
        return name;
    }

    final String getSimpleName() {
        return name.simpleName();
    }

    final String getReferenceString(final Type type, final String... annotations) {
        final String ref = getReferenceString(type.getIdentifier());
        if (!(type instanceof ParameterizedType)) {
            return annotations.length == 0 ? ref : annotate(ref, annotations).toString();
        }

        final StringBuilder sb = annotate(ref, annotations).append('<');
        final Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        if (types.length == 0) {
            return sb.append("?>").toString();
        }

        for (int i = 0; i < types.length; i++) {
            final Type t = types[i];
            if (t instanceof WildcardType) {
                sb.append("? extends ");
            }
            sb.append(getReferenceString(t));
            if (i != types.length - 1) {
                sb.append(", ");
            }
        }

        return sb.append('>').toString();
    }

    final String getReferenceString(final JavaTypeName type) {
        if (type.packageName().isEmpty()) {
            // This is a packageless primitive type, refer to it directly
            return type.simpleName();
        }

        // Self-reference, return simple name
        if (name.equals(type)) {
            return name.simpleName();
        }

        // Fast path: we have already resolved how to refer to this type
        final String existing = nameCache.get(type);
        if (existing != null) {
            return existing;
        }

        // Fork based on whether the class is in this compilation unit, package or neither
        final String result;
        if (name.topLevelClass().equals(type.topLevelClass())) {
            result = localTypeName(type);
        } else if (name.packageName().equals(type.packageName())) {
            result = packageTypeName(type);
        } else {
            result = foreignTypeName(type);
        }

        nameCache.put(type, result);
        return result;
    }

    final NestedJavaGeneratedType getEnclosedType(final JavaTypeName type) {
        return requireNonNull(enclosedTypes.get(type.simpleName()));
    }

    final boolean checkAndImportType(final JavaTypeName type) {
        // We can import the type only if it does not conflict with us or our immediately-enclosed types
        final String simpleName = type.simpleName();
        return !simpleName.equals(getSimpleName()) && !enclosedTypes.containsKey(simpleName)
                && !conflictingNames.contains(simpleName) && importCheckedType(type);
    }

    abstract boolean importCheckedType(JavaTypeName type);

    abstract String localTypeName(JavaTypeName type);

    private String foreignTypeName(final JavaTypeName type) {
        return checkAndImportType(type) ? type.simpleName() : type.toString();
    }

    private String packageTypeName(final JavaTypeName type) {
        // Try to anchor the top-level type and use a local reference
        return checkAndImportType(type.topLevelClass()) ? type.localName() : type.toString();
    }

    private static StringBuilder annotate(final String ref, final String... annotations) {
        final StringBuilder sb = new StringBuilder();
        if (annotations.length == 0) {
            return sb.append(ref);
        }

        final int dot = ref.lastIndexOf('.');
        if (dot != -1) {
            sb.append(ref, 0, dot + 1);
        }
        for (String annotation : annotations) {
            sb.append('@').append(annotation).append(' ');
        }

        return sb.append(ref, dot + 1, ref.length());
    }
}
