/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.yangtools.binding.generator.impl.tree.StatementRepresentation;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.GetterAnnotation;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.ReturnType;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An explicit {@link Generator}, associated with a particular {@link EffectiveStatement}.
 */
// FIXME: unify this with Generator
public abstract class AbstractExplicitGenerator<S extends EffectiveStatement<?, ?>, R extends RuntimeType>
        extends Generator implements CopyableNode, StatementRepresentation<S> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractExplicitGenerator.class);

    private final @NonNull S statement;

    /**
     * Field tracking previous incarnation (along reverse of 'uses' and 'augment' axis) of this statement. This field
     * can either be one of:
     * <ul>
     *   <li>{@code null} when not resolved, i.e. access is not legal, or</li>
     *   <li>{@code this} object if this is the original definition, or</li>
     *   <li>a generator which is one step closer to the original definition</li>
     * </ul>
     */
    private AbstractExplicitGenerator<S, R> prev;
    /**
     * Field holding the original incarnation, i.e. the terminal node along {@link #prev} links.
     */
    private AbstractExplicitGenerator<S, R> orig;
    /**
     * Field containing and indicator holding the runtime type, if applicable.
     */
    private @Nullable R runtimeType;
    private boolean runtimeTypeInitialized;

    @NonNullByDefault
    AbstractExplicitGenerator(final S statement) {
        this.statement = requireNonNull(statement);
    }

    @NonNullByDefault
    AbstractExplicitGenerator(final S statement, final CompositeGenerator<?, ?> parent) {
        super(parent);
        this.statement = requireNonNull(statement);
    }

    @Override
    public final @NonNull S statement() {
        return statement;
    }

    /**
     * Return the {@link RuntimeType} associated with this objec, if applicablet. This represents
     * the externally-accessible view of this object when considered outside the schema tree or binding tree hierarchy.
     *
     * @return Associated run-time type, or {@code null}
     */
    public final @Nullable R generatedRuntimeType() {
        final var type = generatedType();
        return type == null ? null : runtimeType();
    }

    private @Nullable R runtimeType() {
        if (!runtimeTypeInitialized) {
            final var type = runtimeJavaType();
            if (type != null) {
                runtimeType = createExternalRuntimeType(type);
            }
            runtimeTypeInitialized = true;
        }
        return runtimeType;
    }

    /**
     * Return the {@link RuntimeType} associated with this object. This represents the externally-accessible view of
     * this object when considered outside the schema tree or binding tree hierarchy.
     *
     * @return Associated run-time type
     */
    public final @NonNull R getRuntimeType() {
        final var local = runtimeType();
        if (local == null) {
            throw new VerifyException(this + " does not have a run-time type");
        }
        return local;
    }

    /**
     * Return the {@link Type} associated with this object at run-time, if applicable. This method often synonymous
     * with {@code generatedType().orElseNull()}, but not always. For example
     * <pre>
     *   <code>
     *     leaf foo {
     *       type string;
     *     }
     *   </code>
     * </pre>
     * Results in an empty {@link #generatedType()}, but still produces a {@code java.lang.String}-based
     * {@link RuntimeType}.
     *
     * @return Associated {@link Type}
     */
    // FIXME: this should be a generic class argument
    // FIXME: this needs a better name, but 'runtimeType' is already taken.
    @Nullable Type runtimeJavaType() {
        return generatedType();
    }

    /**
     * Create the externally-accessible {@link RuntimeType} view of this object. The difference between
     * this method and {@link #createInternalRuntimeType(AugmentResolver, EffectiveStatement)} is that this method
     * represents the view attached to {@link #statement()} and contains a separate global view of all available
     * augmentations attached to the Archetype.
     *
     * @param type {@link Type} associated with this object, as returned by {@link #runtimeJavaType()}
     * @return Externally-accessible RuntimeType
     */
    abstract @NonNull R createExternalRuntimeType(@NonNull Type type);

    /**
     * Create the internally-accessible {@link RuntimeType} view of this object, if applicable. The difference between
     * this method and {@link #createExternalRuntimeType(Type)} is that this represents the view attache
     * to the specified {@code stmt}, which is supplied by the parent statement. The returned {@link RuntimeType} always
     * reports the global view of attached augmentations as empty.
     *
     * @param lookup context to use when looking up child statements
     * @param stmt Statement for which to create the view
     * @return Internally-accessible RuntimeType, or {@code null} if not applicable
     */
    final @Nullable R createInternalRuntimeType(final @NonNull AugmentResolver resolver, final @NonNull S stmt) {
        // FIXME: cache requests: if we visited this statement, we obviously know what it entails. Note that we walk
        //        towards the original definition. As such, the cache may have to live in the generator we look up,
        //        but should operate on this statement to reflect lookups. This needs a bit of figuring out.
        var gen = this;
        do {
            final var type = gen.runtimeJavaType();
            if (type != null) {
                return createInternalRuntimeType(resolver, stmt, type);
            }

            gen = gen.previous();
        } while (gen != null);

        return null;
    }

    abstract @NonNull R createInternalRuntimeType(@NonNull AugmentResolver resolver, @NonNull S statement,
        @NonNull Type type);

    @Override
    public final boolean isAddedByUses() {
        return statement instanceof AddedByUsesAware aware && aware.isAddedByUses();
    }

    @Override
    public final boolean isAugmenting() {
        return statement instanceof CopyableNode copyable && copyable.isAugmenting();
    }

    /**
     * Attempt to link the generator corresponding to the original definition for this generator.
     *
     * @return {@code true} if this generator is linked
     */
    final boolean linkOriginalGenerator() {
        if (orig != null) {
            // Original already linked
            return true;
        }

        if (prev == null) {
            LOG.trace("Linking {}", this);

            if (!isAddedByUses() && !isAugmenting()) {
                orig = prev = this;
                LOG.trace("Linked {} to self", this);
                return true;
            }

            final var link = getParent().<S, R>originalChild(getQName());
            if (link == null) {
                LOG.trace("Cannot link {} yet", this);
                return false;
            }

            prev = link.previous();
            orig = link.original();
            if (orig != null) {
                LOG.trace("Linked {} to {} original {}", this, prev, orig);
                return true;
            }

            LOG.trace("Linked {} to intermediate {}", this, prev);
            return false;
        }

        orig = prev.originalLink().original();
        if (orig != null) {
            LOG.trace("Linked {} to original {}", this, orig);
            return true;
        }
        return false;
    }

    /**
     * {@return the previous incarnation of this generator, or {@code null} if this is the original generator}
     */
    final @Nullable AbstractExplicitGenerator<S, R> previous() {
        final var local = verifyNotNull(prev, "Generator %s does not have linkage to previous instance resolved", this);
        return local == this ? null : local;
    }

    /**
     * {@return the previous incarnation of this generator}
     */
    final @NonNull AbstractExplicitGenerator<S, R> getPrevious() {
        final var previous = previous();
        if (previous == null) {
            throw new VerifyException("Missing previous link in " + this);
        }
        return previous;
    }

    /**
     * Return the original incarnation of this generator, or self if this is the original generator.
     *
     * @return Original incarnation of this generator
     */
    @NonNull AbstractExplicitGenerator<S, R> getOriginal() {
        final var ret = orig;
        if (ret != null) {
            return ret;
        }
        throw new VerifyException("Generator " + this + " does not have linkage to original instance resolved");
    }

    @Nullable AbstractExplicitGenerator<S, R> tryOriginal() {
        return orig;
    }

    /**
     * Return the link towards the original generator.
     *
     * @return Link towards the original generator.
     */
    final @NonNull OriginalLink<S, R> originalLink() {
        final var local = prev;
        if (local == null) {
            return OriginalLink.partial(this);
        }
        return local == this ? OriginalLink.complete(this) : OriginalLink.partial(local);
    }

    @Nullable AbstractExplicitGenerator<?, ?> findSchemaTreeGenerator(final QName qname) {
        return findLocalSchemaTreeGenerator(qname);
    }

    final @Nullable AbstractExplicitGenerator<?, ?> findLocalSchemaTreeGenerator(final QName qname) {
        for (var child : this) {
            if (child instanceof AbstractExplicitGenerator<?, ?> gen
                && gen.statement() instanceof SchemaTreeEffectiveStatement<?> stmt && qname.equals(stmt.argument())) {
                return gen;
            }
        }
        return null;
    }

    final @NonNull QName getQName() {
        final var arg = statement.argument();
        if (arg instanceof QName qname) {
            return qname;
        }
        throw new VerifyException("Unexpected argument " + arg);
    }

    @NonNull AbstractQName localName() {
        // FIXME: this should be done in a nicer way
        final var arg = statement.argument();
        if (arg instanceof AbstractQName aqn) {
            return aqn;
        }
        throw new VerifyException("Illegal argument " + arg);
    }

    @Override
    ClassPlacement classPlacement() {
        // We process nodes introduced through augment or uses separately
        // FIXME: this is not quite right!
        return isAddedByUses() || isAugmenting() ? ClassPlacement.NONE : ClassPlacement.TOP_LEVEL;
    }

    @Override
    Member createMember(final CollisionDomain domain) {
        return domain.addPrimary(this, new CamelCaseNamingStrategy(namespace(), localName()));
    }

    // FIXME: separate this method into a separate mixin interface
    @Nullable GetterMethod asGetterMethod() {
        if (isAugmenting()) {
            // Do not process augmented nodes: they will be taken care of in their home augmentation
            return null;
        }
        if (isAddedByUses()) {
            // If this generator has been added by a uses node, it is already taken care of by the corresponding
            // grouping. There is one exception to this rule: 'type leafref' can use a relative path to point
            // outside of its home grouping. In this case we need to examine the instantiation until we succeed in
            // resolving the reference.
            //
            // This dispatch is not exactly nice but it beats defining a no-op method here only to have it overridden
            // by a final method in AbstractTypeObjectGenerator: this dispatches directly where it needs to
            return this instanceof AbstractTypeObjectGenerator<?, ?> typeObjectGenerator
                ? typeObjectGenerator.constructGetterMethodOverride() : null;
        }

        return constructGetter(methodReturnType(), Collections.emptyIterator());
    }

    @NonNullByDefault
    GetterMethod constructGetter(final ReturnType returnType, final Iterator<GetterAnnotation> annotations) {
        throw new VerifyException("Attempted to construct getter for " + this);
    }

    @NonNullByDefault
    static final GetterMethod constructGetter(final SchemaTreeEffectiveStatement<?> statement,
            final ReturnType returnType, final Iterator<GetterAnnotation> annotations) {
        // FIXME: This method assumes a injective mapping from YANG identifier to method suffix. That is not the case,
        //        as we have dealt with a similar problem for class names, where we have the whol NamingStrategy thing
        //        and fallbacks.
        //
        //        And example of this problem is
        //
        //          container foo {
        //            leaf bar { type string; }
        //            leaf Bar { type uint64; }
        //          }
        //
        //       Our ability to address such problems is limited by groupings, as they essentially freeze their view on
        //       naming and may be sitting in a different compilation unit, so providing backpressure from
        //       instantiations is a challenge.
        //
        //       Anyway we should be doing our level best to make things work even in face of such challenging models.
        //
        //       In the mean time MethodSignature derives the suffix via Naming.getGetterSuffix(QName), but retains
        //       it as an API detail. If/when we have a solution, it is a simple matter of providing a separate builder
        //       and supply the suffix into it.
        if (!annotations.hasNext()) {
            return GetterMethod.of(statement, returnType);
        }
        final var first = annotations.next();
        if (!annotations.hasNext()) {
            return GetterMethod.of(statement, returnType, first);
        }
        final var list = new ArrayList<GetterAnnotation>();
        list.add(first);
        annotations.forEachRemaining(list::add);
        return GetterMethod.of(statement, returnType, list);
    }

    @NonNullByDefault
    ReturnType methodReturnType() {
        throw new VerifyException("Attempted create method from " + this);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        helper.add("argument", statement.argument());

        if (isAddedByUses()) {
            helper.addValue("addedByUses");
        }
        if (isAugmenting()) {
            helper.addValue("augmenting");
        }
        return helper;
    }

    @NonNullByDefault
    static final Archetype verifyGeneratedType(final Type type) {
        if (type instanceof Archetype ret) {
            return ret;
        }
        throw new VerifyException("Unexpected type " + type);
    }
}
