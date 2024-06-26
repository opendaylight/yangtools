/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.Enumeration;
import org.opendaylight.yangtools.binding.model.api.Enumeration.Pair;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.WildcardType;

/**
 * Abstract class representing a generated type, either top-level or nested. It takes care of tracking references
 * to other Java types and resolving them as best as possible. This class is NOT thread-safe.
 *
 * @author Robert Varga
 */
@NonNullByDefault
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
        if (genType instanceof Enumeration enumeration) {
            enumeration.getValues().stream().map(Pair::getMappedName).forEach(cb::add);
        }
        // TODO: perhaps we can do something smarter to actually access the types
        collectAccessibleTypes(cb, genType);

        conflictingNames = ImmutableSet.copyOf(cb);
    }

    private void collectAccessibleTypes(final Set<String> set, final GeneratedType type) {
        for (Type impl : type.getImplements()) {
            if (impl instanceof GeneratedType genType) {
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

    private String annotateReference(final String ref, final Type type, final String annotation) {
        if (type instanceof ParameterizedType parameterized) {
            return getReferenceString(annotate(ref, annotation), type, parameterized.getActualTypeArguments());
        }
        return "byte[]".equals(ref) ? "byte @" + annotation + "[]" : annotate(ref, annotation).toString();
    }

    final String getFullyQualifiedReference(final Type type, final String annotation) {
        return annotateReference(type.getFullyQualifiedName(), type ,annotation);
    }

    final String getReferenceString(final Type type) {
        final String ref = getReferenceString(type.getIdentifier());
        return type instanceof ParameterizedType parameterized
            ? getReferenceString(new StringBuilder(ref), type, parameterized.getActualTypeArguments())
                : ref;
    }

    final String getReferenceString(final Type type, final String annotation) {
        // Package-private method, all callers who would be passing an empty array are bound to the more special
        // case above, hence we know annotations.length >= 1
        final String ref = getReferenceString(type.getIdentifier());
        return annotateReference(ref, type, annotation);
    }

    private String getReferenceString(final StringBuilder sb, final Type type, final @NonNull Type[] arguments) {
        if (arguments.length == 0) {
            return sb.append("<?>").toString();
        }

        sb.append('<');
        for (int i = 0; i < arguments.length; i++) {
            final Type arg = arguments[i];
            if (arg instanceof WildcardType) {
                sb.append("? extends ");
            }
            sb.append(getReferenceString(arg));
            if (i != arguments.length - 1) {
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

    private static StringBuilder annotate(final String ref, final String annotation) {
        final StringBuilder sb = new StringBuilder();
        final int dot = ref.lastIndexOf('.');
        if (dot != -1) {
            sb.append(ref, 0, dot + 1);
        }
        return sb.append('@').append(annotation).append(' ').append(ref, dot + 1, ref.length());
    }
}
