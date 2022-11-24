/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

final class CollisionDomain {
    abstract sealed class Member {
        private final Generator gen;

        private List<Secondary> secondaries = List.of();
        private String currentPackage;
        private String currentClass;

        Member(final Generator gen) {
            this.gen = requireNonNull(gen);
        }

        final void addSecondary(final Secondary secondary) {
            if (secondaries.isEmpty()) {
                secondaries = new ArrayList<>();
            }
            secondaries.add(requireNonNull(secondary));
        }

        final @NonNull String currentClass() {
            if (currentClass == null) {
                currentClass = computeCurrentClass();
            }
            return currentClass;
        }

        final @NonNull String currentPackage() {
            if (currentPackage == null) {
                currentPackage = computeCurrentPackage();
            }
            return currentPackage;
        }

        abstract boolean equalRoot(@NonNull Member other);

        abstract String computeCurrentClass();

        abstract String computeCurrentPackage();

        boolean signalConflict() {
            solved = false;
            currentClass = null;
            currentPackage = null;

            for (Secondary secondary : secondaries) {
                secondary.primaryConflict();
            }

            return true;
        }

        @Override
        public final String toString() {
            return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
        }

        ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("gen", gen).add("class", currentClass).add("package", currentPackage);
        }
    }

    private sealed class Primary extends Member {
        private ClassNamingStrategy strategy;

        Primary(final Generator gen, final ClassNamingStrategy strategy) {
            super(gen);
            this.strategy = requireNonNull(strategy);
        }

        @Override
        final String computeCurrentClass() {
            return strategy.simpleClassName();
        }

        @Override
        final String computeCurrentPackage() {
            return packageString(strategy.nodeIdentifier());
        }

        @Override
        final boolean signalConflict() {
            final ClassNamingStrategy newStrategy = strategy.fallback();
            if (newStrategy == null) {
                return false;
            }

            strategy = newStrategy;
            return super.signalConflict();
        }

        @Override
        final ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper.add("strategy", strategy));
        }

        @Override
        boolean equalRoot(final Member other) {
            return other instanceof Primary primary
                && strategy.nodeIdentifier().getLocalName().equals(primary.strategy.nodeIdentifier().getLocalName());
        }
    }

    private final class Prefix extends Primary {
        Prefix(final Generator gen, final ClassNamingStrategy strategy) {
            super(gen, strategy);
        }
    }

    private abstract sealed class Secondary extends Member {
        private final String classSuffix;
        final @NonNull Member classPrimary;

        Secondary(final Generator gen, final Member primary, final String classSuffix) {
            super(gen);
            classPrimary = requireNonNull(primary);
            this.classSuffix = requireNonNull(classSuffix);
            primary.addSecondary(this);
        }

        @Override
        final String computeCurrentClass() {
            return classPrimary.currentClass() + classSuffix;
        }

        @Override
        final boolean signalConflict() {
            return classPrimary.signalConflict();
        }

        final void primaryConflict() {
            super.signalConflict();
        }

        @Override
        final boolean equalRoot(final Member other) {
            return other instanceof Secondary sec
                && classPrimary.equalRoot(sec.classPrimary) && classSuffix.equals(sec.classSuffix);
        }
    }

    private final class LeafSecondary extends Secondary {
        LeafSecondary(final Generator gen, final Member classPrimary, final String classSuffix) {
            super(gen, classPrimary, classSuffix);
        }

        @Override
        String computeCurrentPackage() {
            // This should never happen
            throw new UnsupportedOperationException();
        }
    }

    private final class SuffixSecondary extends Secondary {
        private final AbstractQName packageSuffix;

        SuffixSecondary(final Generator gen, final Member primaryClass, final String classSuffix,
                final AbstractQName packageSuffix) {
            super(gen, primaryClass, classSuffix);
            this.packageSuffix = requireNonNull(packageSuffix);
        }

        @Override
        String computeCurrentPackage() {
            return classPrimary.currentPackage() + '.' + packageString(packageSuffix);
        }
    }

    private final class AugmentSecondary extends Secondary {
        private final SchemaNodeIdentifier packageSuffix;

        AugmentSecondary(final AbstractAugmentGenerator gen, final Member primary, final String classSuffix,
                final SchemaNodeIdentifier packageSuffix) {
            super(gen, primary, classSuffix);
            this.packageSuffix = requireNonNull(packageSuffix);
        }

        @Override
        String computeCurrentPackage() {
            final Iterator<QName> it = packageSuffix.getNodeIdentifiers().iterator();

            final StringBuilder sb = new StringBuilder();
            sb.append(packageString(it.next()));
            while (it.hasNext()) {
                sb.append('.').append(packageString(it.next()));
            }
            return sb.toString();
        }
    }

    private final AbstractCompositeGenerator<?, ?> gen;

    private List<Member> members = List.of();
    private boolean solved;

    CollisionDomain(final AbstractCompositeGenerator<?, ?> gen) {
        this.gen = requireNonNull(gen);
    }

    @NonNull Member addPrefix(final Generator memberGen, final ClassNamingStrategy strategy) {
        // Note that contrary to the method name, we are not adding the result to members
        return new Prefix(memberGen, strategy);
    }

    @NonNull Member addPrimary(final Generator memberGen, final ClassNamingStrategy strategy) {
        return addMember(new Primary(memberGen, strategy));
    }

    @NonNull Member addSecondary(final Generator memberGen, final Member primary, final String classSuffix) {
        return addMember(new LeafSecondary(memberGen, primary, classSuffix));
    }

    @NonNull Member addSecondary(final InputGenerator memberGen, final Member primary) {
        return addMember(new SuffixSecondary(memberGen, primary, BindingMapping.RPC_INPUT_SUFFIX,
            memberGen.statement().argument()));
    }

    @NonNull Member addSecondary(final OutputGenerator memberGen, final Member primary) {
        return addMember(new SuffixSecondary(memberGen, primary, BindingMapping.RPC_OUTPUT_SUFFIX,
            memberGen.statement().argument()));
    }

    @NonNull Member addSecondary(final AbstractAugmentGenerator memberGen, final Member classPrimary,
            final String classSuffix, final SchemaNodeIdentifier packageSuffix) {
        return addMember(new AugmentSecondary(memberGen, classPrimary, classSuffix, packageSuffix));
    }

    /*
     * Naming child nodes is tricky.
     *
     * We map multiple YANG namespaces (see YangStatementNamespace) onto a single Java namespace
     * (package/class names), hence we can have legal conflicts on same localName.
     *
     * Furthermore not all localNames are valid Java class/package identifiers, hence even non-equal localNames can
     * conflict on their mapping.
     *
     * Final complication is that we allow user to control preferred name, or we generate one, and we try to come up
     * with nice names like 'foo-bar' becoming FooBar and similar.
     *
     * In all cases we want to end up with cutest possible names while also never creating duplicates. For that we
     * start with each child telling us their preferred name and we collect name->child mapping.
     */
    boolean findSolution() {
        if (solved) {
            // Already solved, nothing to do
            return false;
        }
        if (members.size() < 2) {
            // Zero or one member: no conflict possible
            solved = true;
            return false;
        }

        boolean result = false;
        do {
            // Construct mapping to discover any naming overlaps.
            final Multimap<String, Member> toAssign = ArrayListMultimap.create();
            for (Member member : members) {
                toAssign.put(member.currentClass(), member);
            }

            // Deal with names which do not create a conflict. This is very simple and also very effective, we rarely
            // run into conflicts.
            final var it = toAssign.asMap().entrySet().iterator();
            while (it.hasNext()) {
                final Entry<String, Collection<Member>> entry = it.next();
                final Collection<Member> assignees = entry.getValue();
                if (assignees.size() == 1) {
                    it.remove();
                }
            }

            // This looks counter-intuitive, but the idea is simple: the act of assigning a different strategy may end
            // up creating conflicts where there were none -- including in this domain. Marking this bit allows us to
            // react to such invalidation chains and retry the process.
            solved = true;
            if (!toAssign.isEmpty()) {
                result = true;
                // We still have some assignments we need to resolve -- which means we need to change their strategy.
                for (Collection<Member> conflicting : toAssign.asMap().values()) {
                    int remaining = 0;
                    for (Member member : conflicting) {
                        if (!member.signalConflict()) {
                            remaining++;
                        }
                    }
                    checkState(remaining < 2, "Failed to solve %s due to naming conflict among %s", this, conflicting);
                }
            }
        } while (!solved);

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("gen", gen).toString();
    }

    private @NonNull Member addMember(final @NonNull Member member) {
        if (members.isEmpty()) {
            members = new ArrayList<>();
        }
        members.add(member);
        return member;
    }

    private static @NonNull String packageString(final AbstractQName component) {
        // Replace dashes with dots, as dashes are not allowed in package names
        return component.getLocalName().replace('-', '.');
    }
}
