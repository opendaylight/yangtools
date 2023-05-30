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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.yangtools.binding.generator.impl.tree.StatementRepresentation;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotableTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
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

    AbstractExplicitGenerator(final S statement) {
        this.statement = requireNonNull(statement);
    }

    AbstractExplicitGenerator(final S statement, final AbstractCompositeGenerator<?, ?> parent) {
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
     * this method and {@link #createInternalRuntimeType(EffectiveStatement)} is that this method represents the view
     * attached to {@link #statement()} and contains a separate global view of all available augmentations attached to
     * the GeneratedType.
     *
     * @param type {@link Type} associated with this object, as returned by {@link #runtimeJavaType()}
     * @return Externally-accessible RuntimeType
     */
    abstract @NonNull R createExternalRuntimeType(@NonNull Type type);

    /**
     * Create the internally-accessible {@link RuntimeType} view of this object, if applicable. The difference between
     * this method and {@link #createExternalRuntimeType()} is that this represents the view attached to the specified
     * {@code stmt}, which is supplied by the parent statement. The returned {@link RuntimeType} always reports the
     * global view of attached augmentations as empty.
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
        return introducedByAugmentation(statement);
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
        return verifyNotNull(orig, "Generator %s does not have linkage to original instance resolved", this);
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

    @NonNullByDefault
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        if (isAugmenting()) {
            // Do not process augmented nodes: they will be taken care of in their home augmentation
            return;
        }
        if (isAddedByUses()) {
            // If this generator has been added by a uses node, it is already taken care of by the corresponding
            // grouping. There is one exception to this rule: 'type leafref' can use a relative path to point
            // outside of its home grouping. In this case we need to examine the instantiation until we succeed in
            // resolving the reference.
            addAsGetterMethodOverride(builder, builderFactory);
            return;
        }

        final var returnType = methodReturnType(builderFactory);
        constructGetter(builder, returnType);
        constructRequire(builder, returnType);
    }

    MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        return constructGetter(builder, returnType, Naming.getGetterMethodName(localName().getLocalName()));
    }

    final MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder,
            final Type returnType, final String methodName) {
        final var getMethod = builder.addMethod(methodName).setReturnType(returnType);

        annotateDeprecatedIfNecessary(getMethod);

        statement.findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class)
            .map(TypeMemberComment::referenceOf).ifPresent(getMethod::setComment);

        return getMethod;
    }

    void constructRequire(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        // No-op in most cases
    }

    final void constructRequireImpl(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        constructGetter(builder, returnType, Naming.getRequireMethodName(localName().getLocalName()))
            .setDefault(true)
            .setMechanics(ValueMechanics.NONNULL);
    }

    void addAsGetterMethodOverride(final @NonNull GeneratedTypeBuilderBase<?> builder,
            final @NonNull TypeBuilderFactory builderFactory) {
        // No-op for most cases
    }

    @NonNull Type methodReturnType(final @NonNull TypeBuilderFactory builderFactory) {
        return getGeneratedType(builderFactory);
    }

    final void annotateDeprecatedIfNecessary(final AnnotableTypeBuilder builder) {
        annotateDeprecatedIfNecessary(statement, builder);
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

    static final @NonNull GeneratedType verifyGeneratedType(final Type type) {
        if (type instanceof GeneratedType ret) {
            return ret;
        }
        throw new VerifyException("Unexpected type " + type);
    }

    /**
     * Determines, whether the given {@link EffectiveStatement} was introduced by augmentation.
     * The given {@link EffectiveStatement} is assumed to be a descendant of {@code this}
     * {@link AbstractExplicitGenerator}
     *
     * @param stmt {@link EffectiveStatement} which is assumed to be a descendant
     *             of {@code this} {@link AbstractExplicitGenerator}
     * @return {@code true} if the given {@link EffectiveStatement} was introduced into the schema tree by augmentation
     *         or {@code false} otherwise
     */
    protected final boolean introducedByAugmentation(final EffectiveStatement<?, ?> stmt) {
        if (this instanceof ModuleGenerator) {
            return false;
        }
        Optional<GeneratorWithTarget> maybeGenWithTarget = getClosestGeneratorWithTarget(this);

        while (maybeGenWithTarget.isPresent()) {
            final GeneratorWithTarget candidate = maybeGenWithTarget.orElseThrow();
            for (final AugmentationSchemaNode augmentation : candidate.target().getAvailableAugmentations()) {
                if (isAugmentingVia(stmt, augmentation)) {
                    return true;
                }
            }
            maybeGenWithTarget = getClosestGeneratorWithTarget(candidate.generator().getParent());
        }
        return false;
    }

    /**
     * Determines whether the specified {@link EffectiveStatement} is a descendant
     * of the given {@link AugmentationSchemaNode}.
     *
     * @param stmt Descendant candidate
     * @param augmentation Root of the recursive search for the descendant candidate
     * @return true if the {@code stmt} is the descendant of the given {@code augmentation} or false if not
     */
    private boolean isAugmentingVia(final EffectiveStatement<?, ?> stmt, final AugmentationSchemaNode augmentation) {
        if (stmt.argument() instanceof QName qName
                && augmentation instanceof SchemaTreeAwareEffectiveStatement<?, ?> schemaTree) {
            return isInSchemaTree(qName, schemaTree);
        }
        return false;
    }

    /**
     * Searches for a {@link AbstractExplicitGenerator} in a 'reverse' fashion, traversing the tree by parent
     * for the closest parent whose associated {@link AbstractExplicitGenerator#statement}
     * is of {@link AugmentationTarget} instance.
     *
     * <p>
     * The search is performed until such a generator is found or if the generator is of {@link ModuleGenerator}
     * instance, which does not have any statement associated with it.
     *
     * @param parent the {@link AbstractExplicitGenerator} whose closest parent with {@link AugmentationTarget}
     *               we are looking for
     * @return the closest (in up direction - traverse by parent => {@link AbstractCompositeGenerator#getParent()})
     *          generator, whose associated statement is instance of {@link AugmentationTarget} wrapped
     *          in a DTO {@link GeneratorWithTarget} returned as {@link Optional}
     *          or {@link Optional#empty} if none found
     */
    private Optional<GeneratorWithTarget> getClosestGeneratorWithTarget(
            final AbstractExplicitGenerator<?, ?> parent) {
        AbstractExplicitGenerator<?, ?> possibleTarget = parent;

        while (!(possibleTarget instanceof ModuleGenerator)) {
            if (possibleTarget.statement() instanceof AugmentationTarget augTarget) {
                return Optional.of(new GeneratorWithTarget(possibleTarget, augTarget));
            }
            possibleTarget = possibleTarget.getParent();
        }
        return Optional.empty();
    }

    /**
     * Recursively searches the schema tree until it finds the specified {@code child}.
     *
     * @param child Descendant identifier
     * @param stmt The parent, whose descendant we are searching for
     * @return true if the given {@code child} is a descendant of the given {@code stmt} otherwise return false
     */
    private boolean isInSchemaTree(final QName child, final SchemaTreeAwareEffectiveStatement<?, ?> stmt) {
        if (stmt.findSchemaTreeNode(child).isPresent()) {
            return true;
        }

        for (final EffectiveStatement<?, ?> s : stmt.effectiveSubstatements()) {
            if (s instanceof SchemaTreeAwareEffectiveStatement<?, ?> schemaAware) {
                if (isInSchemaTree(child, schemaAware)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Serves as a DTO (or a tuple) as a mean of returning a generator and its statement
     * that is cast to {@link AugmentationTarget}.
     * @param generator {@link AbstractExplicitGenerator} whose statement is instance of {@link AugmentationTarget}
     * @param target statement, associated with the given {@code generator}
     */
    private record GeneratorWithTarget(AbstractExplicitGenerator<?, ?> generator, AugmentationTarget target) {
    }
}
