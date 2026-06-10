/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.binding.generator.impl.reactor.TypeReference.ResolvedLeafref;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.meta.TypeDefinitionCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for {@link TypedefGenerator} and {@link AbstractTypeAwareGenerator}. This encompasses three
 * different statements with two different semantics:
 * <ul>
 *   <li>{@link TypedefGenerator}s always result in a generated {@link TypeObject}, even if the semantics is exactly
 *       the same as its base type. This aligns with {@code typedef} defining a new type.<li>
 *   <li>{@link LeafGenerator}s and {@link LeafListGenerator}s, on the other hand, do not generate a {@link TypeObject}
 *       unless absolutely necassary. This aligns with {@code leaf} and {@code leaf-list} being mapped onto a property
 *       of its parent type.<li>
 * </ul>
 *
 * <p>To throw a bit of confusion into the mix, there are three exceptions to those rules:
 * <ul>
 *   <li>
 *     {@code identityref} definitions never result in a type definition being emitted. The reason for this has to do
 *     with identity type mapping as well as history of our codebase.
 *
 *     <p>
 *     The problem at hand is inconsistency between the fact that identity is mapped to a {@link Class}, which is also
 *     returned from leaves which specify it like this:
 *     <pre>
 *       <code>
 *         identity iden;
 *
 *         container foo {
 *           leaf foo {
 *             type identityref {
 *               base iden;
 *             }
 *           }
 *         }
 *       </code>
 *     </pre>
 *     which results in fine-looking
 *     <pre>
 *       <code>
 *         interface Foo {
 *           Class&lt;? extends Iden&gt; getFoo();
 *         }
 *       </code>
 *     </pre>
 *
 *     <p>
 *     This gets more dicey if we decide to extend the previous snippet to also include:
 *     <pre>
 *       <code>
 *         typedef bar-ref {
 *           type identityref {
 *             base iden;
 *           }
 *         }
 *
 *         container bar {
 *           leaf bar {
 *             type bar-ref;
 *           }
 *         }
 *       </code>
 *     </pre>
 *
 *     <p>
 *     Now we have competing requirements: {@code typedef} would like us to use encapsulation to capture the defined
 *     type, while {@code getBar()} wants us to retain shape with getFoo(), as it should not matter how the
 *     {@code identityref} was formed. We need to pick between:
 *     <ol>
 *       <li>
 *         <pre>
 *           <code>
 *             public class BarRef extends ScalarTypeObject&lt;Class&lt;? extends Iden&gt;&gt; {
 *               Class&lt;? extends Iden&gt; getValue() {
 *                 // ...
 *               }
 *             }
 *
 *             interface Bar {
 *               BarRef getBar();
 *             }
 *           </code>
 *         </pre>
 *       </li>
 *       <li>
 *         <pre>
 *           <code>
 *             interface Bar {
 *               Class&lt;? extends Iden&gt; getBar();
 *             }
 *           </code>
 *         </pre>
 *       </li>
 *     </ol>
 *
 *     <p>
 *     Here the second option is more user-friendly, as the type system works along the lines of <b>reference</b>
 *     semantics, treating and {@code Bar.getBar()} and {@code Foo.getFoo()} as equivalent. The first option would
 *     force users to go through explicit encapsulation, for no added benefit as the {@code typedef} cannot possibly add
 *     anything useful to the actual type semantics.
 *   </li>
 *   <li>
 *     {@code leafref} definitions never result in a type definition being emitted. The reasons for this are similar to
 *     {@code identityref}, but have an additional twist: a {@code leafref} can target a relative path, which may only
 *     be resolved at a particular instantiation.
 *
 *     <p>Take the example of the following model:
 *     <pre>
 *       <code>
 *         grouping grp {
 *           typedef ref {
 *             type leafref {
 *               path ../xyzzy;
 *             }
 *           }
 *
 *           leaf foo {
 *             type ref;
 *           }
 *         }
 *
 *         container bar {
             uses grp;
 *
 *           leaf xyzzy {
 *             type string;
 *           }
 *         }
 *
 *         container baz {
 *           uses grp;
 *
 *           leaf xyzzy {
 *             type int32;
 *           }
 *         }
 *       </code>
 *     </pre>
 *
 *     The {@code typedef ref} points to outside of the grouping, and hence the type of {@code leaf foo} is polymorphic:
 *     the definition in {@code grouping grp} needs to use {@code Object}, whereas the instantiations in
 *     {@code container bar} and {@code container baz} need to use {@code String} and {@link Integer} respectively.
 *     Expressing the resulting interface contracts requires return type specialization and run-time checks. An
 *     intermediate class generated for the typedef would end up being a hindrance without any benefit.
 *   <li>
 *   <li>
 *     {@code enumeration} definitions never result in a derived type. This is because these are mapped to Java
 *     {@code enum}, which does not allow subclassing.
 *   <li>
 * </ul>
 *
 * <p>At the end of the day, the mechanic translation rules are giving way to correctly mapping the semantics -- which
 * in both of the exception cases boil down to tracking type indirection. Intermediate constructs involved in tracking
 * type indirection in YANG constructs is therefore explicitly excluded from the generated Java code, but the Binding
 * Specification still takes them into account when determining types as outlined above.
 */
abstract class AbstractTypeObjectGenerator<
        S extends TypeEffectiveStatement.MandatoryIn<QName, ?> & TypeDefinitionCompat.WithQNameArgument<?>,
        R extends RuntimeType> extends AbstractDependentGenerator<S, R> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTypeObjectGenerator.class);

    private final @NonNull TypeObjectSupport support;

    // FIXME: these fields should be better-controlled with explicit sequencing guards. It it currently stands, we are
    //        expending two (or more) additional fields to express downstream linking. If we had the concept of
    //        resolution step (an enum), we could just get by with a simple queue of Step/Callback pairs, which would
    //        trigger as needed. See how we manage baseGen/inferred fields.

    /**
     * The generator corresponding to our YANG base type. It produces the superclass of our encapsulated type. If it is
     * {@code null}, this generator is the root of the hierarchy.
     */
    private TypedefGenerator baseGen;
    private TypeReference refType;
    // The generator corresponding to the furthest resolved leaf in leafref chain, at point when this was resolved
    // serves to help in detection of circular leafref chains
    private AbstractTypeObjectGenerator<?, ?> furthestInRefChain;
    private TypeObjectSupport.Union.Dependencies unionDependencies;
    private List<AbstractTypeObjectGenerator<?, ?>> inferred = List.of();

    /**
     * The type of single-element return type of the getter method associated with this generator. This is retained for
     * run-time type purposes. It may be uninitialized, in which case this object must have a generated type.
     */
    private Type methodReturnTypeElement;

    @NonNullByDefault
    AbstractTypeObjectGenerator(final S statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
        support = TypeObjectSupport.of(statement().typeStatement());
    }

    @Override
    final void linkDependencies(final GeneratorContext context) {
        verify(inferred != null, "Duplicate linking of %s", this);

        final QName typeName = support.type.argument();
        if (isBuiltinName(typeName)) {
            verify(inferred.isEmpty(), "Unexpected non-empty downstreams in %s", this);
            inferred = null;
            return;
        }

        final var prev = previous();
        if (prev != null) {
            verify(prev instanceof AbstractTypeObjectGenerator, "Unexpected previous %s", prev);
            ((AbstractTypeObjectGenerator<S, R>) prev).linkInferred(this);
        } else {
            linkBaseGen(context.resolveTypedef(typeName));
        }
    }

    private void linkInferred(final AbstractTypeObjectGenerator<?, ?> downstream) {
        if (inferred == null) {
            downstream.linkBaseGen(verifyNotNull(baseGen, "Mismatch on linking between %s and %s", this, downstream));
            return;
        }

        if (inferred.isEmpty()) {
            inferred = new ArrayList<>(2);
        }
        inferred.add(downstream);
    }

    private void linkBaseGen(final TypedefGenerator upstreamBaseGen) {
        verify(baseGen == null, "Attempted to replace base %s with %s in %s", baseGen, upstreamBaseGen, this);
        final var downstreams = verifyNotNull(inferred, "Duplicated linking of %s", this);
        baseGen = verifyNotNull(upstreamBaseGen);
        baseGen.addDerivedGenerator(this);
        inferred = null;

        for (AbstractTypeObjectGenerator<?, ?> downstream : downstreams) {
            downstream.linkBaseGen(upstreamBaseGen);
        }
    }

    @NonNullByDefault
    void bindTypeDefinition(final GeneratorContext context) {
        if (baseGen != null) {
            // We have registered with baseGen, it will push the type to us
            return;
        }

        switch (support) {
            case TypeObjectSupport.Identityref identity ->
                refType = TypeReference.identityRef(identity.baseIdentities()
                    .map(context::resolveIdentity)
                    .collect(Collectors.toUnmodifiableList()));
            case TypeObjectSupport.Leafref leafref ->
                refType = resolveLeafref(context, leafref.path());
            case TypeObjectSupport.Union union -> {
                unionDependencies = new TypeObjectSupport.Union.Dependencies(union.type, context);
                LOG.trace("Resolved union {} to dependencies {}", union.type, unionDependencies);
            }
            default -> {
                // no-op
            }
        }

        LOG.trace("Resolved base {} to generator {}", support.type, refType);
        bindDerivedGenerators(refType);
    }

    final void bindTypeDefinition(final @Nullable TypeReference reference) {
        refType = reference;
        LOG.trace("Resolved derived {} to generator {}", support.type, refType);
    }

    final boolean isLeafRef() {
        return refType instanceof TypeReference.Leafref;
    }

    @NonNullByDefault
    private TypeReference resolveLeafref(final GeneratorContext context, final PathExpression path) {
        final AbstractTypeObjectGenerator<?, ?> targetGenerator;
        try {
            targetGenerator = context.resolveLeafref(path);
        } catch (IllegalArgumentException e) {
            return TypeReference.leafRef(e);
        }

        checkCircularRefChain(targetGenerator);

        return TypeReference.leafRef(targetGenerator);
    }

    private void checkCircularRefChain(final AbstractTypeObjectGenerator<?, ?> targetGenerator) {
        if (targetGenerator == null) {
            // UnresolvedLeafref
            return;
        }
        var current = targetGenerator;
        // We can't rely on furthestInRefChain of our targetGenerator as it could be resolved in the meantime
        while (current.furthestInRefChain != null) {
            current = current.furthestInRefChain;
        }

        // At this point we either arrived at some not yet resolved leafref or at non leafref generator
        // we check for circular leafref chain as the not yet resolved leafref could be this
        checkArgument(current != this, "Circular leafref chain detected at leaf %s", statement().argument());
        furthestInRefChain = current;
    }

    static final boolean isBuiltinName(final QName typeName) {
        return YangConstants.RFC6020_YANG_MODULE.equals(typeName.getModule());
    }

    abstract void bindDerivedGenerators(@Nullable TypeReference reference);

    @Override
    final ClassPlacement classPlacement() {
        if (refType != null) {
            // Reference types never create a new type
            return ClassPlacement.NONE;
        }
        if (isDerivedEnumeration()) {
            // Types derived from an enumeration never create a new type, as that would have to be a subclass of an enum
            // and since enums are final, that can never happen.
            return ClassPlacement.NONE;
        }
        return classPlacementImpl();
    }

    @NonNull ClassPlacement classPlacementImpl() {
        // TODO: make this a lot more accurate by comparing the effective delta between the base type and the effective
        //       restricted type. We should not be generating a type for constructs like:
        //
        //         leaf foo {
        //           type uint8 { range 0..255; }
        //         }
        //
        //       or
        //
        //         typedef foo {
        //           type uint8 { range 0..100; }
        //         }
        //
        //         leaf foo {
        //           type foo { range 0..100; }
        //         }
        //
        //       Which is relatively easy to do for integral types, but is way more problematic for 'pattern'
        //       restrictions. Nevertheless we can define the mapping in a way which can be implemented with relative
        //       ease.
        if (baseGen != null) {
            return ClassPlacement.NONE;
        }
        return support instanceof TypeObjectSupport.Scalar || isAddedByUses() || isAugmenting() ? ClassPlacement.NONE
            : ClassPlacement.MEMBER;
    }

    @Override
    final Archetype getGeneratedType(final TypeBuilderFactory builderFactory) {
        // For derived enumerations defer to base type
        return isDerivedEnumeration() ? baseGen.getGeneratedType(builderFactory)
            : super.getGeneratedType(builderFactory);
    }

    final boolean isEnumeration() {
        return baseGen != null ? baseGen.isEnumeration() : support instanceof TypeObjectSupport.Enumeration;
    }

    final boolean isDerivedEnumeration() {
        return baseGen != null && baseGen.isEnumeration();
    }

    @Override
    Type methodReturnType(final TypeBuilderFactory builderFactory) {
        return methodReturnElementType(builderFactory);
    }

    @Override
    final Type runtimeJavaType() {
        if (methodReturnTypeElement != null) {
            return methodReturnTypeElement;
        }
        final var genType = generatedType();
        return genType != null ? genType : getPrevious().runtimeJavaType();
    }

    @NonNullByDefault
    final Type methodReturnElementType(final TypeBuilderFactory builderFactory) {
        var local = methodReturnTypeElement;
        if (local == null) {
            methodReturnTypeElement = local = createMethodReturnElementType(builderFactory);
        }
        return local;
    }

    @NonNullByDefault
    private Type createMethodReturnElementType(final TypeBuilderFactory builderFactory) {
        final var generatedType = tryGeneratedType(builderFactory);
        if (generatedType != null) {
            // We have generated a type here, so return it. This covers 'bits', 'enumeration' and 'union'.
            return generatedType;
        }

        if (refType != null) {
            // This is a reference type of some kind. Defer to its judgement as to what the return type is.
            return refType.methodReturnType(builderFactory);
        }

        final var prev = previous();
        if (prev != null) {
            // We have been added through augment/uses, defer to the original definition
            return prev.methodReturnType(builderFactory);
        }

        final Type baseType;
        if (baseGen == null) {
            if (!(support instanceof TypeObjectSupport.Scalar scalar)) {
                throw new VerifyException("Cannot resolve type " + support.type.argument() + " in " + this);
            }
            baseType = scalar.javaType;
        } else {
            // We are derived from a base generator. Defer to its type for return.
            baseType = baseGen.getGeneratedType(builderFactory);
        }

        return restrictType(baseType, Restrictions.compute(statement(), support.type), builderFactory);
    }

    static final @NonNull Type restrictType(final @NonNull Type baseType, final @Nullable Restrictions restrictions,
            final TypeBuilderFactory builderFactory) {
        if (restrictions == null || restrictions.isEmpty()) {
            // No additional restrictions, return base type
            return baseType;
        }

        return switch (baseType) {
            // This is a simple Java type, just wrap it with new restrictions
            case ConcreteType concrete -> concrete.withRestrictions(restrictions);
            // Base type is a GTO, we need to re-adjust it with new restrictions
            case ScalarTypeObjectArchetype scalar -> {
                // FIXME: this is definitely not quite right: statement/typeDefinition should be different
                yield new ScalarTypeObjectArchetype(scalar.name(), scalar.statement(), scalar.typeDefinition(),
                    scalar.valueType(), restrictions, scalar.getSuperType());
            }
            case UnionTypeObjectArchetype union -> {
                // FIXME: this is definitely not quite right: statement/typeDefinition should be different
                yield new UnionTypeObjectArchetype(union.name(), union.statement(), union.typePropertyNames(),
                    union.typePropertyTypes(), List.of(), union.getSuperType());
            }
            default -> throw new VerifyException("Unhandled base type " + baseType);
        };
    }

    @Override
    final void addAsGetterMethodOverride(final GeneratedTypeBuilderBase<?> builder,
            final TypeBuilderFactory builderFactory) {
        if (!(refType instanceof ResolvedLeafref)) {
            // We are not dealing with a leafref or have nothing to add
            return;
        }

        final var prev = (AbstractTypeObjectGenerator<?, ?>) getPrevious();
        if (prev.refType instanceof ResolvedLeafref) {
            // We should be already inheriting the correct type
            return;
        }

        // Note: this may we wrapped for leaf-list, hence we need to deal with that
        final var myType = methodReturnType(builderFactory);
        LOG.trace("Override of {} to {}", this, myType);
        final var getter = constructGetter(builder, myType);
        getter.addAnnotation(OVERRIDE_ANNOTATION);
        annotateDeprecatedIfNecessary(getter);
    }

    @Override
    final TypeObjectArchetype<?> createTypeImpl(final TypeBuilderFactory builderFactory) {
        if (baseGen != null) {
            final var baseType = baseGen.getGeneratedType(builderFactory);
            if (!(baseType instanceof GeneratedTransferObject<?> gto)) {
                throw new VerifyException("Unexpected base type " + baseType);
            }
            return createDerivedType(builderFactory, gto);
        }

        return switch (support) {
            case TypeObjectSupport.Bits bits -> {
                final var stmt = statement();
                yield new BitsTypeObjectArchetype(typeName(), stmt, (BitsTypeDefinition) stmt.typeDefinition());
            }
            case TypeObjectSupport.Enumeration enumeration -> {
                final var stmt = statement();
                yield new EnumTypeObjectArchetype(typeName(), stmt, (EnumTypeDefinition) stmt.typeDefinition());
            }
            case TypeObjectSupport.Union union -> union.toArchetype(this, unionDependencies, builderFactory);
            case TypeObjectSupport.Scalar scalar -> scalar.toArchetype(this, builderFactory);
            default -> throw new VerifyException("Unhandled type " + support.type.argument());
        };
    }

    abstract @NonNull GeneratedTransferObject<?> createDerivedType(@NonNull TypeBuilderFactory builderFactory,
        @NonNull GeneratedTransferObject<?> baseType);
}
