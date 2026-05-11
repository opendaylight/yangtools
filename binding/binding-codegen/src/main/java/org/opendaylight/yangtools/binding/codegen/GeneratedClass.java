/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.VerifyException;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.WildcardType;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * State related to generating a Java class, either top-level or nested. It takes care of tracking references to other
 * Java types and resolving them as best as possible. This class is NOT thread-safe.
 */
@NonNullByDefault
abstract sealed class GeneratedClass implements BlockBuilderFactory, Mutable
        permits GeneratedClass.Nested, GeneratedClass.TopLevel {
    /**
     * A class which is nested inside some other type. It defers import decisions to its enclosing type, eventually
     * arriving at a {@link TopLevelJavaGeneratedType}.
     */
    static final class Nested extends GeneratedClass {
        private final GeneratedClass enclosingClass;

        private Nested(final GeneratedClass enclosingClass, final GeneratedType genType) {
            super(genType);
            this.enclosingClass = requireNonNull(enclosingClass);
        }

        @Override
        boolean importCheckedType(final JavaTypeName type) {
            // Defer to enclosing type, which needs to re-run its checks
            return enclosingClass.checkAndImportType(type);
        }

        @Override
        String localTypeName(final JavaTypeName type) {
            // Check if the type is a reference to our immediately-enclosing type
            final var enclosingName = enclosingClass.name();
            return enclosingName.equals(type) ? enclosingName.simpleName() : findLocalTypeName(type);

        }

        private String findLocalTypeName(final JavaTypeName type) {
            final var descendant = findDescandantPath(type);
            return descendant == null
                // The type is not present in our hierarchy, defer to our immediately-enclosing type, which may be able
                // to find the target.
                ? enclosingClass.localTypeName(type)
                // Target type is a declared as a enclosed type of us and we have the path where it lurks.
                : printLocalTypeName(descendant);
        }

        private static String printLocalTypeName(final Iterator<String> it) {
            final var sb = new StringBuilder().append(it.next());
            while (it.hasNext()) {
                sb.append('.').append(it.next());
            }
            return sb.toString();
        }

        private @Nullable Iterator<String> findDescandantPath(final JavaTypeName type) {
            var enclosing = type.immediatelyEnclosingClass();
            if (enclosing == null) {
                throw new VerifyException("no immediately enclosing class in " + type);
            }

            final var myName = name();
            final var reversePath = new ArrayList<String>();
            reversePath.add(type.simpleName());
            do  {
                if (enclosing.equals(myName)) {
                    return reversePath.reversed().iterator();
                }

                reversePath.add(enclosing.simpleName());
                enclosing = enclosing.immediatelyEnclosingClass();
            } while (enclosing != null);

            return null;
        }
    }

    /**
     * This class tracks types generated into a single Java compilation unit (file) and manages imports of that
     * compilation unit. Since we are generating only public classes, this is synonymous to a top-level Java type.
     */
    static final class TopLevel extends GeneratedClass {
        private final HashBiMap<JavaTypeName, String> importedTypes = HashBiMap.create();

        private TopLevel(final GeneratedType genType) {
            super(genType);
        }

        // FIXME: this method should not be exposed
        @Deprecated
        Stream<JavaTypeName> imports() {
            return importedTypes.entrySet().stream()
                .filter(this::needsExplicitImport)
                .map(Entry::getKey)
                .sorted(Comparator.comparing(JavaTypeName::toString));
        }

        @Override
        String localTypeName(final JavaTypeName type) {
            // Locally-anchored type, this is simple: just strip the first local name component and concat the others
            final var it = type.localNameComponents().iterator();
            it.next();

            final var sb = new StringBuilder().append(it.next());
            while (it.hasNext()) {
                sb.append('.').append(it.next());
            }
            return sb.toString();
        }

        @Override
        boolean importCheckedType(final JavaTypeName type) {
            if (importedTypes.containsKey(type)) {
                return true;
            }
            final var simpleName = type.simpleName();
            if (importedTypes.containsValue(simpleName)) {
                return false;
            }
            importedTypes.put(type, simpleName);
            return true;
        }

        private boolean needsExplicitImport(final Entry<JavaTypeName, String> entry) {
            final var name = entry.getKey();

            if (!name().packageName().equals(name.packageName())) {
                // Different package: need to import it
                return true;
            }

            if (name.immediatelyEnclosingClass() == null) {
                // This a top-level class import, we can skip it
                return false;
            }

            // This is a nested class, we need to spell it out if the import entry points to the simple name
            return entry.getValue().equals(name.simpleName());
        }
    }

    private final HashMap<JavaTypeName, @Nullable String> nameCache = new HashMap<>();
    private final Map<String, Nested> nestedClasses;
    private final Set<String> conflictingNames;
    private final JavaTypeName name;

    GeneratedClass(final GeneratedType genType) {
        name = genType.name();

        nestedClasses = Stream.concat(genType.getEnclosedTypes().stream(), genType.getEnumerations().stream())
            .collect(Collectors.toUnmodifiableMap(GeneratedType::simpleName, type -> new Nested(this, type)));

        final var cb = new HashSet<String>();
        if (genType instanceof EnumTypeObjectArchetype enumeration) {
            cb.addAll(enumeration.valueToConstant().values());
        }
        // TODO: perhaps we can do something smarter to actually access the types
        collectAccessibleTypes(cb, genType);

        conflictingNames = Set.copyOf(cb);
    }

    private void collectAccessibleTypes(final HashSet<String> set, final GeneratedType type) {
        for (var impl : type.getImplements()) {
            if (impl instanceof GeneratedType genType) {
                for (var inner : Iterables.concat(genType.getEnclosedTypes(), genType.getEnumerations())) {
                    set.add(inner.name().simpleName());
                }
                collectAccessibleTypes(set, genType);
            }
        }
    }

    /**
     * {@return a new {@link GeneratedClass.TopLevel top-level class} for the specified {@link GeneratedType}}
     * @param genType the generated type
     */
    // FIXME: this method should:
    //        - accept a GeneratedType -> Template resolver
    //        - do the work of BaseTemplate.generate()
    //        - return a Block
    static GeneratedClass.TopLevel of(final GeneratedType genType) {
        return new TopLevel(genType);
    }

    /**
     * {@return the {@link JavaTypeName name} of this class}
     */
    final JavaTypeName name() {
        return name;
    }

    // TODO: for now just a simple BlockBuilder, but we can also do things like importedName(), so more concise
    @Override
    public final BlockBuilder newBlockBuilder() {
        return new BlockBuilder();
    }

    private String annotateReference(final String ref, final Type type, final String annotation) {
        if (type instanceof ParameterizedType parameterized) {
            return getReferenceString(annotate(ref, annotation), type, parameterized.getActualTypeArguments());
        }
        return "byte[]".equals(ref) ? "byte @" + annotation + "[]" : annotate(ref, annotation).toString();
    }

    // FIXME: rename to getCanonicalReferenceString
    final String getFullyQualifiedReference(final Type type, final String annotation) {
        return annotateReference(type.canonicalName(), type ,annotation);
    }

    // FIXME: add documentation and consider a new name
    final String getReferenceString(final Type type) {
        final String ref = getReferenceString(type.name());
        return type instanceof ParameterizedType parameterized
            ? getReferenceString(new StringBuilder(ref), type, parameterized.getActualTypeArguments())
                : ref;
    }

    final String getReferenceString(final Type type, final String annotation) {
        // Package-private method, all callers who would be passing an empty array are bound to the more special
        // case above, hence we know annotations.length >= 1
        final String ref = getReferenceString(type.name());
        return annotateReference(ref, type, annotation);
    }

    private String getReferenceString(final StringBuilder sb, final Type type, final List<Type> arguments) {
        if (arguments.isEmpty()) {
            return sb.append("<?>").toString();
        }

        sb.append('<');
        final var it = arguments.iterator();
        while (true) {
            final var arg = it.next();
            if (arg instanceof WildcardType) {
                sb.append("? extends ");
            }
            sb.append(getReferenceString(arg));
            if (!it.hasNext()) {
                return sb.append('>').toString();
            }
            sb.append(", ");
        }
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

    // FIXME: the three callers look like they could just iterate over this.nestedClasses, as they seem to derive their
    //        argument to this method from type.getEnclosedTypes()
    final Nested getNestedClass(final GeneratedType type) {
        return verifyNotNull(nestedClasses.get(type.simpleName()));
    }

    final boolean checkAndImportType(final JavaTypeName type) {
        // We can import the type only if it does not conflict with us or our immediately-enclosed types
        final var simpleName = type.simpleName();
        return !simpleName.equals(name().simpleName()) && !nestedClasses.containsKey(simpleName)
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
        final var sb = new StringBuilder();
        final int dot = ref.lastIndexOf('.');
        if (dot != -1) {
            sb.append(ref, 0, dot + 1);
        }
        return sb.append('@').append(annotation).append(' ').append(ref, dot + 1, ref.length());
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).toString();
    }
}
