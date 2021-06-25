/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A type name. This class encloses Java type naming rules laid down in
 * <a href="https://docs.oracle.com/javase/specs/jls/se9/html/index.html">The Java Language Specification</a>, notably
 * sections 4 and 8. It deals with primitive, array and reference types.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class JavaTypeName implements Identifier, Immutable {
    private static final class Primitive extends JavaTypeName {
        private static final long serialVersionUID = 1L;

        Primitive(final String simpleName) {
            super(simpleName);
        }

        @Override
        public String packageName() {
            return "";
        }

        @Override
        public Optional<JavaTypeName> immediatelyEnclosingClass() {
            return Optional.empty();
        }

        @Override
        public boolean canCreateEnclosed(final String simpleName) {
            throw new UnsupportedOperationException("Primitive type " + simpleName() + " cannot enclose type "
                    + simpleName);
        }

        @Override
        public JavaTypeName createEnclosed(final String simpleName) {
            throw new UnsupportedOperationException("Primitive type " + simpleName() + " cannot enclose type "
                    + simpleName);
        }

        @Override
        public String localName() {
            return simpleName();
        }

        @Override
        public List<String> localNameComponents() {
            return ImmutableList.of(simpleName());
        }

        @Override
        public JavaTypeName createSibling(final String simpleName) {
            return new Primitive(simpleName);
        }

        @Override
        public JavaTypeName topLevelClass() {
            return this;
        }

        @Override
        public String toString() {
            return simpleName();
        }
    }

    private abstract static class Reference extends JavaTypeName {
        private static final long serialVersionUID = 1L;

        Reference(final String simpleName) {
            super(simpleName);
        }

        @Override
        public boolean canCreateEnclosed(final String simpleName) {
            return !simpleName.equals(simpleName());
        }

        @Override
        public final JavaTypeName createEnclosed(final String simpleName) {
            checkValidName(requireNonNull(simpleName));
            return new Nested(this, simpleName);
        }

        @Override
        public final String toString() {
            return appendClass(new StringBuilder()).toString();
        }

        void checkValidName(final String nestedName) {
            checkArgument(canCreateEnclosed(nestedName), "Nested class name %s conflicts with enclosing class %s",
                nestedName, this);
        }

        abstract StringBuilder appendClass(StringBuilder sb);
    }

    private static final class TopLevel extends Reference {
        private static final long serialVersionUID = 1L;

        private final String packageName;

        TopLevel(final String packageName, final String simpleName) {
            super(simpleName);
            checkArgument(!packageName.isEmpty());
            this.packageName = packageName;
        }

        @Override
        public String packageName() {
            return packageName;
        }

        @Override
        public JavaTypeName createSibling(final String simpleName) {
            return new TopLevel(packageName, simpleName);
        }

        @Override
        public Optional<JavaTypeName> immediatelyEnclosingClass() {
            return Optional.empty();
        }

        @Override
        public String localName() {
            return simpleName();
        }

        @Override
        public List<String> localNameComponents() {
            final List<String> ret = new ArrayList<>();
            ret.add(simpleName());
            return ret;
        }

        @Override
        public JavaTypeName topLevelClass() {
            return this;
        }

        @Override
        StringBuilder appendClass(final StringBuilder sb) {
            return sb.append(packageName).append('.').append(simpleName());
        }
    }

    private static final class Nested extends Reference {
        private static final long serialVersionUID = 1L;

        private final Reference immediatelyEnclosingClass;

        Nested(final Reference immediatelyEnclosingClass, final String simpleName) {
            super(simpleName);
            this.immediatelyEnclosingClass = requireNonNull(immediatelyEnclosingClass);
        }

        @Override
        public String packageName() {
            return immediatelyEnclosingClass.packageName();
        }

        @Override
        public JavaTypeName createSibling(final String simpleName) {
            return immediatelyEnclosingClass.createEnclosed(simpleName);
        }

        @Override
        public Optional<JavaTypeName> immediatelyEnclosingClass() {
            return Optional.of(immediatelyEnclosingClass);
        }

        @Override
        StringBuilder appendClass(final StringBuilder sb) {
            return immediatelyEnclosingClass.appendClass(sb).append('.').append(simpleName());
        }

        @Override
        public boolean canCreateEnclosed(final String simpleName) {
            return super.canCreateEnclosed(simpleName) && immediatelyEnclosingClass.canCreateEnclosed(simpleName);
        }

        @Override
        public String localName() {
            return immediatelyEnclosingClass.localName() + "." + simpleName();
        }

        @Override
        public List<String> localNameComponents() {
            final List<String> ret = immediatelyEnclosingClass.localNameComponents();
            ret.add(simpleName());
            return ret;
        }

        @Override
        public JavaTypeName topLevelClass() {
            return immediatelyEnclosingClass.topLevelClass();
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(JavaTypeName.class);
    private static final long serialVersionUID = 1L;

    private final String simpleName;

    JavaTypeName(final String simpleName) {
        checkArgument(!simpleName.isEmpty());
        this.simpleName = simpleName;
    }

    /**
     * Create a TypeName for an existing class.
     *
     * @param clazz Class instance
     * @return A new TypeName
     * @throws NullPointerException if clazz is null
     */
    public static JavaTypeName create(final Class<?> clazz) {
        final Class<?> enclosing = clazz.getEnclosingClass();
        if (enclosing != null) {
            return create(enclosing).createEnclosed(clazz.getSimpleName());
        }
        final Package pkg = clazz.getPackage();
        return pkg == null ? new Primitive(clazz.getSimpleName()) : new TopLevel(pkg.getName(), clazz.getSimpleName());
    }

    /**
     * Create a TypeName for a top-level class.
     *
     * @param packageName Class package name
     * @param simpleName Class simple name
     * @return A new TypeName.
     * @throws NullPointerException if any of the arguments is null
     * @throws IllegalArgumentException if any of the arguments is empty
     */
    public static JavaTypeName create(final String packageName, final String simpleName) {
        return new TopLevel(packageName, simpleName);
    }

    /**
     * Check if an enclosed type with specified name can be created.
     *
     * @param simpleName Simple name of the enclosed class
     * @return True if the proposed simple name does not conflict with any enclosing types
     * @throws IllegalArgumentException if the simpleName is empty
     * @throws UnsupportedOperationException if this type name does not support nested type
     */
    public abstract boolean canCreateEnclosed(String simpleName);

    /**
     * Create a TypeName for a class immediately enclosed by this class.
     *
     * @param simpleName Simple name of the enclosed class
     * @return A new TypeName.
     * @throws NullPointerException if simpleName is null
     * @throws IllegalArgumentException if the simpleName hides any of the enclosing types or if it is empty
     * @throws UnsupportedOperationException if this type name does not support nested type
     */
    public abstract JavaTypeName createEnclosed(String simpleName);

    /**
     * Create a TypeName for a class immediately enclosed by this class, potentially falling back to appending it with
     * a suffix if a JLS hiding conflict occurs.
     *
     * @param simpleName Simple name of the enclosed class
     * @param fallbackSuffix Suffix to append if the {@code simpleName} is cannot be created due to JLS
     * @return A new TypeName.
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if simpleName is empty or if both simpleName and fallback both hide any of the
     *                                  enclosing types
     * @throws UnsupportedOperationException if this type name does not support nested type
     */
    @SuppressWarnings("checkstyle:hiddenField")
    public final JavaTypeName createEnclosed(final String simpleName, final String fallbackSuffix) {
        checkArgument(!simpleName.isEmpty());
        try {
            return createEnclosed(simpleName);
        } catch (IllegalArgumentException e) {
            final String fallback = simpleName + fallbackSuffix;
            LOG.debug("Failed to create enclosed type '{}', falling back to '{}'", simpleName, fallback, e);
            return createEnclosed(fallback);
        }
    }

    /**
     * Create a TypeName for a class that is a sibling of this class. A sibling has the same package name, and the same
     * immediately enclosing class.
     *
     * @param simpleName Simple name of the sibling class
     * @return A new TypeName.
     * @throws NullPointerException if simpleName is null
     * @throws IllegalArgumentException if the simpleName is empty
     */
    // TODO: we could detect/allocate names in this method such that they don't conflict.
    public abstract JavaTypeName createSibling(String simpleName);

    /**
     * Return the simple name of the class.
     *
     * @return Simple name of the class.
     */
    public final String simpleName() {
        return simpleName;
    }

    /**
     * Return the package name in which this class resides. This does not account for any class nesting, i.e. for nested
     * classes this returns the package name of the top-level class in naming hierarchy.
     *
     * @return Package name of the class.
     */
    public abstract String packageName();

    /**
     * Return the enclosing class JavaTypeName, if present.
     *
     * @return Enclosing class JavaTypeName.
     */
    public abstract Optional<JavaTypeName> immediatelyEnclosingClass();

    /**
     * Return the top-level class JavaTypeName which is containing this type, or self if this type is a top-level
     * one.
     *
     * @return Top-level JavaTypeName
     */
    public abstract JavaTypeName topLevelClass();

    /**
     * Return the package-local name by which this type can be referenced by classes living in the same package.
     *
     * @return Local name.
     */
    public abstract String localName();

    /**
     * Return broken-down package-local name components.
     *
     * @return List of package-local components.
     */
    public abstract List<String> localNameComponents();

    @Override
    public final int hashCode() {
        return Objects.hash(simpleName, packageName(), immediatelyEnclosingClass());
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof JavaTypeName)) {
            return false;
        }
        final JavaTypeName other = (JavaTypeName) obj;
        return simpleName.equals(other.simpleName) && packageName().equals(other.packageName())
                && immediatelyEnclosingClass().equals(other.immediatelyEnclosingClass());
    }

    /**
     * Return the Fully-Qualified Class Name string of this TypeName.
     *
     * @return Fully-Qualified Class Name string of this TypeName.
     */
    @Override
    public abstract String toString();
}
