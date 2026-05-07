/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.BindingPackageName;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A single node in generator tree. Each node will eventually resolve to a generated Java class. Each node also can have
 * a number of children, which are generators corresponding to the YANG subtree of this node.
 *
 * <p>Each tree is rooted in a {@link ModuleGenerator} and its organization follows roughly YANG {@code schema tree}
 * layout, but with a twist coming from the reuse of generated interfaces from a {@code grouping} in the location of
 * every {@code uses} encountered and also the corresponding backwards propagation of {@code augment} effects.
 *
 * <p>Overall the tree layout guides the allocation of Java package and top-level class namespaces.
 */
public abstract class Generator implements Iterable<Generator> {
    private final DataContainerGenerator<?, ?> parent;

    private Optional<Member> member;
    private GeneratorResult result;
    private TypeName typeName;
    private BindingPackageName javaPackage;

    Generator() {
        parent = null;
    }

    @NonNullByDefault
    Generator(final DataContainerGenerator<?, ?> parent) {
        this.parent = requireNonNull(parent);
    }

    public final @Nullable Archetype generatedType() {
        return result.generatedType();
    }

    @Override
    public Iterator<Generator> iterator() {
        return Collections.emptyIterator();
    }

    /**
     * Return the {@link DataContainerGenerator} inside which this generator is defined. It is illegal to call this
     * method on a {@link ModuleGenerator}.
     *
     * @return Parent generator
     */
    final @NonNull DataContainerGenerator<?, ?> getParent() {
        final var ret = parent;
        if (ret != null) {
            return ret;
        }
        throw new VerifyException("No parent for " + this);
    }

    boolean isEmpty() {
        return true;
    }

    /**
     * Return the namespace of this statement.
     *
     * @return Corresponding namespace
     * @throws UnsupportedOperationException if this node does not have a corresponding namespace
     */
    abstract @NonNull StatementNamespace namespace();

    @NonNull ModuleGenerator currentModule() {
        return getParent().currentModule();
    }

    /**
     * Push this statement into a {@link SchemaInferenceStack} so that the stack contains a resolvable {@code data tree}
     * hierarchy.
     *
     * @param inferenceStack Target inference stack
     */
    abstract void pushToInference(@NonNull SchemaInferenceStack inferenceStack);

    abstract @NonNull ClassPlacement classPlacement();

    final @NonNull Member getMember() {
        final var ret = ensureMember();
        if (ret != null) {
            return ret;
        }
        throw new VerifyException("No member for " + this);
    }

    final @Nullable Member ensureMember() {
        if (member == null) {
            member = switch (classPlacement()) {
                case NONE -> Optional.empty();
                case MEMBER, PHANTOM, TOP_LEVEL -> Optional.of(createMember(parentDomain()));
            };
        }
        return member.orElse(null);
    }

    @NonNull CollisionDomain parentDomain() {
        return getParent().domain();
    }

    @NonNullByDefault
    abstract Member createMember(CollisionDomain domain);

    /**
     * Create the type associated with this builder. This method idempotent.
     */
    @NonNullByDefault
    final void ensureType() {
        if (result != null) {
            return;
        }

        result = switch (classPlacement()) {
            case NONE, PHANTOM -> GeneratorResult.empty();
            case MEMBER -> GeneratorResult.member(createTypeImpl());
            case TOP_LEVEL -> GeneratorResult.toplevel(createTypeImpl());
        };

        for (var child : this) {
            child.ensureType();
        }
    }

    @NonNullByDefault
    Archetype getGeneratedType() {
        final var genType = tryGeneratedType();
        if (genType != null) {
            return genType;
        }
        throw new VerifyException("No type generated for " + this);
    }

    final @Nullable Archetype tryGeneratedType() {
        ensureType();
        return result.generatedType();
    }

    final @Nullable Archetype enclosedType() {
        ensureType();
        return result.enclosedType();
    }

    /**
     * Create the type associated with this builder, as per {@link #ensureType()} contract. This method is guaranteed to
     * be called at most once.
     */
    @NonNullByDefault
    abstract Archetype createTypeImpl();

    final @NonNull String assignedName() {
        return getMember().currentClass();
    }

    final @NonNull BindingPackageName javaPackage() {
        var local = javaPackage;
        if (local == null) {
            javaPackage = local = createJavaPackage();
        }
        return local;
    }

    @NonNull BindingPackageName createJavaPackage() {
        final var myPackage = getMember().currentPackage();
        return getPackageParent().javaPackage().subPackage(Naming.normalizePackageName(myPackage));
    }

    final @NonNull TypeName typeName() {
        TypeName local = typeName;
        if (local == null) {
            typeName = local = createTypeName();
        }
        return local;
    }

    @NonNull TypeName createTypeName() {
        return TypeName.of(getPackageParent().javaPackage(), assignedName());
    }

    @NonNull DataContainerGenerator<?, ?> getPackageParent() {
        return getParent();
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper;
    }
}
