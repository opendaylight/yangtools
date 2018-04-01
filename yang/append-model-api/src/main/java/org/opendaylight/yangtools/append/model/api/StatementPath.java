/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.append.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.primitives.UnsignedInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * An unambiguous statement path composed of a non-empty list of {@link StatementIdentifier}s.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public final class StatementPath implements Identifier {
    /**
     * Statement namespace in which to look up the value.
     *
     * @author Robert Varga
     */
    public enum StatementNamespace {
        /**
         * Case within a choice based on QName.
         */
        CASE("c"),
        /**
         * Data (container, list, leaf, leaf-list, choice, rpc, action, notification, anyxml, anydata) based on QName.
         */
        DATA("d"),
        /**
         * Extension within a module based on QName.
         */
        EXTENSION("e"),
        /**
         * Feature within a module based on QName.
         */
        FEATURE("f"),
        /**
         * Grouping based on QName.
         */
        GROUPING("g"),
        /**
         * Identity within a module based on QName.
         */
        IDENTITY("i"),
        /**
         * Module based on imported prefix.
         */
        MODULE("m"),
        /**
         * Typedef based on QName.
         */
        TYPEDEF("t"),
        /**
         * Union member type based on unsigned offset.
         */
        UNION_MEMBER("u");

        @SuppressWarnings("null")
        private static final Map<String, StatementNamespace> PREFIX_TO_NAMESPACE = Maps.uniqueIndex(
            Arrays.asList(values()), StatementNamespace::getPrefix);

        private final String prefix;

        StatementNamespace(final String prefix) {
            this.prefix = requireNonNull(prefix);
        }

        public String getPrefix() {
            return prefix;
        }

        @SuppressWarnings("null")
        public static Optional<StatementNamespace> forPrefix(final String prefix) {
            return Optional.ofNullable(PREFIX_TO_NAMESPACE.get(requireNonNull(prefix)));
        }
    }

    public abstract static class StatementIdentifier<T> {
        private final T value;

        StatementIdentifier(final T value) {
            this.value = requireNonNull(value);
        }

        public abstract StatementNamespace getNamespace();

        public final T getValue() {
            return value;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(getNamespace(), value);
        }

        @Override
        public final boolean equals(final @Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof StatementIdentifier)) {
                return false;
            }
            final StatementIdentifier<?> other = (StatementIdentifier<?>) obj;
            return getNamespace() == other.getNamespace() && value.equals(other.value);
        }

        @Override
        public final String toString() {
            return MoreObjects.toStringHelper(StatementIdentifier.class)
                    .add("namespace", getNamespace())
                    .add("value", value)
                    .toString();
        }
    }

    private abstract static class AbstractQNameIdentifier extends StatementIdentifier<QName> {
        AbstractQNameIdentifier(final QName value) {
            super(value);
        }
    }

    public static final class CaseIdentifier extends AbstractQNameIdentifier {
        CaseIdentifier(final QName value) {
            super(value);
        }

        @Override
        public StatementNamespace getNamespace() {
            return StatementNamespace.CASE;
        }
    }

    public static final class DataIdentitifer extends AbstractQNameIdentifier {
        DataIdentitifer(final QName value) {
            super(value);
        }

        @Override
        public StatementNamespace getNamespace() {
            return StatementNamespace.DATA;
        }
    }

    public static final class ExtensionIdentifier extends AbstractQNameIdentifier {
        ExtensionIdentifier(final QName value) {
            super(value);
        }

        @Override
        public StatementNamespace getNamespace() {
            return StatementNamespace.EXTENSION;
        }
    }

    public static final class FeatureIdentifier extends AbstractQNameIdentifier {
        FeatureIdentifier(final QName value) {
            super(value);
        }

        @Override
        public StatementNamespace getNamespace() {
            return StatementNamespace.FEATURE;
        }
    }

    public static final class GroupingIdentifier extends AbstractQNameIdentifier {
        GroupingIdentifier(final QName value) {
            super(value);
        }

        @Override
        public StatementNamespace getNamespace() {
            return StatementNamespace.GROUPING;
        }
    }

    public static final class IdentityIdentifier extends AbstractQNameIdentifier {
        IdentityIdentifier(final QName value) {
            super(value);
        }

        @Override
        public StatementNamespace getNamespace() {
            return StatementNamespace.IDENTITY;
        }
    }

    public static final class ModuleIdentifier extends StatementIdentifier<String> {
        ModuleIdentifier(final String prefix) {
            super(prefix);
        }

        @Override
        public StatementNamespace getNamespace() {
            return StatementNamespace.MODULE;
        }
    }

    public static final class TypedefIdentifier extends AbstractQNameIdentifier {
        TypedefIdentifier(final QName value) {
            super(value);
        }

        @Override
        public StatementNamespace getNamespace() {
            return StatementNamespace.TYPEDEF;
        }
    }

    public static final class UnionMemberIdentifier extends StatementIdentifier<UnsignedInteger> {
        UnionMemberIdentifier(final UnsignedInteger value) {
            super(value);
        }

        @Override
        public StatementNamespace getNamespace() {
            return StatementNamespace.UNION_MEMBER;
        }
    }

    private static final long serialVersionUID = 1L;

    private final List<StatementIdentifier<?>> elements;

    private StatementPath(final List<StatementIdentifier<?>> elements) {
        this.elements = requireNonNull(elements);
    }

    public static StatementPath of(final StatementIdentifier<?> element) {
        return new StatementPath(ImmutableList.of(element));
    }

    public static StatementPath of(final StatementIdentifier<?>... elements) {
        return new StatementPath(ImmutableList.copyOf(elements));
    }

    public static StatementPath of(final Collection<? extends StatementIdentifier<?>> elements) {
        return new StatementPath(ImmutableList.copyOf(elements));
    }

    public static CaseIdentifier caseIdentifier(final QName qname) {
        return new CaseIdentifier(qname);
    }

    public static DataIdentitifer dataIdentifier(final QName qname) {
        return new DataIdentitifer(qname);
    }

    public static ExtensionIdentifier extensionIdentifier(final QName qname) {
        return new ExtensionIdentifier(qname);
    }

    public static FeatureIdentifier featureIdentifier(final QName qname) {
        return new FeatureIdentifier(qname);
    }

    public static GroupingIdentifier groupingIdentifier(final QName qname) {
        return new GroupingIdentifier(qname);
    }

    public static IdentityIdentifier identityIdentifier(final QName qname) {
        return new IdentityIdentifier(qname);
    }

    public static ModuleIdentifier moduleIdentifier(final String prefix) {
        return new ModuleIdentifier(prefix);
    }

    public static TypedefIdentifier typedefIdentifier(final QName qname) {
        return new TypedefIdentifier(qname);
    }

    public static UnionMemberIdentifier unionMemberIdentifier(final UnsignedInteger offset) {
        return new UnionMemberIdentifier(offset);
    }

    public List<StatementIdentifier<?>> getElements() {
        return elements;
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StatementPath)) {
            return false;
        }
        return elements.equals(((StatementPath)obj).elements);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("elements", elements).toString();
    }
}
