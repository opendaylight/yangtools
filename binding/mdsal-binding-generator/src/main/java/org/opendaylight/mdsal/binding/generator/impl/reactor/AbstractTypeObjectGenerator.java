/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.generator.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.generator.impl.reactor.TypeReference.ResolvedLeafref;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.ri.BaseYangTypes;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.model.ri.TypeConstants;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.AbstractEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.RegexPatterns;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternExpression;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
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
 * <p>
 * To throw a bit of confusion into the mix, there are three exceptions to those rules:
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
 *     {@code identityref}, but have an additional twist: a {@leafref} can target a relative path, which may only be
 *     resolved at a particular instantiation.
 *
 *     Take the example of the following model:
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
 * <p>
 * At the end of the day, the mechanic translation rules are giving way to correctly mapping the semantics -- which in
 * both of the exception cases boil down to tracking type indirection. Intermediate constructs involved in tracking
 * type indirection in YANG constructs is therefore explicitly excluded from the generated Java code, but the Binding
 * Specification still takes them into account when determining types as outlined above.
 */
abstract class AbstractTypeObjectGenerator<S extends EffectiveStatement<?, ?>, R extends RuntimeType>
        extends AbstractDependentGenerator<S, R> {
    private static final class UnionDependencies implements Immutable {
        private final Map<EffectiveStatement<?, ?>, TypeReference> identityTypes = new HashMap<>();
        private final Map<EffectiveStatement<?, ?>, TypeReference> leafTypes = new HashMap<>();
        private final Map<QName, TypedefGenerator> baseTypes = new HashMap<>();

        UnionDependencies(final TypeEffectiveStatement<?> type, final GeneratorContext context) {
            resolveUnionDependencies(context, type);
        }

        private void resolveUnionDependencies(final GeneratorContext context, final TypeEffectiveStatement<?> union) {
            for (EffectiveStatement<?, ?> stmt : union.effectiveSubstatements()) {
                if (stmt instanceof TypeEffectiveStatement<?> type) {
                    final QName typeName = type.argument();
                    if (TypeDefinitions.IDENTITYREF.equals(typeName)) {
                        if (!identityTypes.containsKey(stmt)) {
                            identityTypes.put(stmt, TypeReference.identityRef(
                                type.streamEffectiveSubstatements(BaseEffectiveStatement.class)
                                    .map(BaseEffectiveStatement::argument)
                                    .map(context::resolveIdentity)
                                    .collect(Collectors.toUnmodifiableList())));
                        }
                    } else if (TypeDefinitions.LEAFREF.equals(typeName)) {
                        if (!leafTypes.containsKey(stmt)) {
                            leafTypes.put(stmt, TypeReference.leafRef(context.resolveLeafref(
                                type.findFirstEffectiveSubstatementArgument(PathEffectiveStatement.class)
                                .orElseThrow())));
                        }
                    } else if (TypeDefinitions.UNION.equals(typeName)) {
                        resolveUnionDependencies(context, type);
                    } else if (!isBuiltinName(typeName) && !baseTypes.containsKey(typeName)) {
                        baseTypes.put(typeName, context.resolveTypedef(typeName));
                    }
                }
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTypeObjectGenerator.class);
    static final ImmutableMap<QName, Type> SIMPLE_TYPES = ImmutableMap.<QName, Type>builder()
        .put(TypeDefinitions.BINARY, BaseYangTypes.BINARY_TYPE)
        .put(TypeDefinitions.BOOLEAN, BaseYangTypes.BOOLEAN_TYPE)
        .put(TypeDefinitions.DECIMAL64, BaseYangTypes.DECIMAL64_TYPE)
        .put(TypeDefinitions.EMPTY, BaseYangTypes.EMPTY_TYPE)
        .put(TypeDefinitions.INSTANCE_IDENTIFIER, BaseYangTypes.INSTANCE_IDENTIFIER)
        .put(TypeDefinitions.INT8, BaseYangTypes.INT8_TYPE)
        .put(TypeDefinitions.INT16, BaseYangTypes.INT16_TYPE)
        .put(TypeDefinitions.INT32, BaseYangTypes.INT32_TYPE)
        .put(TypeDefinitions.INT64, BaseYangTypes.INT64_TYPE)
        .put(TypeDefinitions.STRING, BaseYangTypes.STRING_TYPE)
        .put(TypeDefinitions.UINT8, BaseYangTypes.UINT8_TYPE)
        .put(TypeDefinitions.UINT16, BaseYangTypes.UINT16_TYPE)
        .put(TypeDefinitions.UINT32, BaseYangTypes.UINT32_TYPE)
        .put(TypeDefinitions.UINT64, BaseYangTypes.UINT64_TYPE)
        .build();

    private final TypeEffectiveStatement<?> type;

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
    private List<GeneratedType> auxiliaryGeneratedTypes = List.of();
    private UnionDependencies unionDependencies;
    private List<AbstractTypeObjectGenerator<?, ?>> inferred = List.of();

    /**
     * The type of single-element return type of the getter method associated with this generator. This is retained for
     * run-time type purposes. It may be uninitialized, in which case this object must have a generated type.
     */
    private Type methodReturnTypeElement;

    AbstractTypeObjectGenerator(final S statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
        type = statement().findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
    }

    @Override
    public final List<GeneratedType> auxiliaryGeneratedTypes() {
        return auxiliaryGeneratedTypes;
    }

    @Override
    final void linkDependencies(final GeneratorContext context) {
        verify(inferred != null, "Duplicate linking of %s", this);

        final QName typeName = type.argument();
        if (isBuiltinName(typeName)) {
            verify(inferred.isEmpty(), "Unexpected non-empty downstreams in %s", this);
            inferred = null;
            return;
        }

        final AbstractExplicitGenerator<S, R> prev = previous();
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
        final List<AbstractTypeObjectGenerator<?, ?>> downstreams = verifyNotNull(inferred,
            "Duplicated linking of %s", this);
        baseGen = verifyNotNull(upstreamBaseGen);
        baseGen.addDerivedGenerator(this);
        inferred = null;

        for (AbstractTypeObjectGenerator<?, ?> downstream : downstreams) {
            downstream.linkBaseGen(upstreamBaseGen);
        }
    }

    void bindTypeDefinition(final GeneratorContext context) {
        if (baseGen != null) {
            // We have registered with baseGen, it will push the type to us
            return;
        }

        final QName arg = type.argument();
        if (TypeDefinitions.IDENTITYREF.equals(arg)) {
            refType = TypeReference.identityRef(type.streamEffectiveSubstatements(BaseEffectiveStatement.class)
                .map(BaseEffectiveStatement::argument)
                .map(context::resolveIdentity)
                .collect(Collectors.toUnmodifiableList()));
        } else if (TypeDefinitions.LEAFREF.equals(arg)) {
            final AbstractTypeObjectGenerator<?, ?> targetGenerator = context.resolveLeafref(
                type.findFirstEffectiveSubstatementArgument(PathEffectiveStatement.class).orElseThrow());
            checkArgument(targetGenerator != this, "Effective model contains self-referencing leaf %s",
                statement().argument());
            refType = TypeReference.leafRef(targetGenerator);
        } else if (TypeDefinitions.UNION.equals(arg)) {
            unionDependencies = new UnionDependencies(type, context);
            LOG.trace("Resolved union {} to dependencies {}", type, unionDependencies);
        }

        LOG.trace("Resolved base {} to generator {}", type, refType);
        bindDerivedGenerators(refType);
    }

    final void bindTypeDefinition(final @Nullable TypeReference reference) {
        refType = reference;
        LOG.trace("Resolved derived {} to generator {}", type, refType);
    }

    private static boolean isBuiltinName(final QName typeName) {
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
        return baseGen != null || SIMPLE_TYPES.containsKey(type.argument()) || isAddedByUses() || isAugmenting()
            ? ClassPlacement.NONE : ClassPlacement.MEMBER;
    }

    @Override
    final GeneratedType getGeneratedType(final TypeBuilderFactory builderFactory) {
        // For derived enumerations defer to base type
        return isDerivedEnumeration() ? baseGen.getGeneratedType(builderFactory)
            : super.getGeneratedType(builderFactory);
    }

    final boolean isEnumeration() {
        return baseGen != null ? baseGen.isEnumeration() : TypeDefinitions.ENUMERATION.equals(type.argument());
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
        if (genType.isPresent()) {
            return genType.orElseThrow();
        }
        final var prev = verifyNotNull(previous(), "No previous generator for %s", this);
        return prev.runtimeJavaType();
    }

    final @NonNull Type methodReturnElementType(final @NonNull TypeBuilderFactory builderFactory) {
        var local = methodReturnTypeElement;
        if (local == null) {
            methodReturnTypeElement = local = createMethodReturnElementType(builderFactory);
        }
        return local;
    }

    private @NonNull Type createMethodReturnElementType(final @NonNull TypeBuilderFactory builderFactory) {
        final GeneratedType generatedType = tryGeneratedType(builderFactory);
        if (generatedType != null) {
            // We have generated a type here, so return it. This covers 'bits', 'enumeration' and 'union'.
            return generatedType;
        }

        if (refType != null) {
            // This is a reference type of some kind. Defer to its judgement as to what the return type is.
            return refType.methodReturnType(builderFactory);
        }

        final AbstractExplicitGenerator<?, ?> prev = previous();
        if (prev != null) {
            // We have been added through augment/uses, defer to the original definition
            return prev.methodReturnType(builderFactory);
        }

        final Type baseType;
        if (baseGen == null) {
            final QName qname = type.argument();
            baseType = verifyNotNull(SIMPLE_TYPES.get(qname), "Cannot resolve type %s in %s", qname, this);
        } else {
            // We are derived from a base generator. Defer to its type for return.
            baseType = baseGen.getGeneratedType(builderFactory);
        }

        return restrictType(baseType, computeRestrictions(), builderFactory);
    }

    private static @NonNull Type restrictType(final @NonNull Type baseType, final Restrictions restrictions,
            final TypeBuilderFactory builderFactory) {
        if (restrictions == null || restrictions.isEmpty()) {
            // No additional restrictions, return base type
            return baseType;
        }

        if (!(baseType instanceof GeneratedTransferObject gto)) {
            // This is a simple Java type, just wrap it with new restrictions
            return Types.restrictedType(baseType, restrictions);
        }

        // Base type is a GTO, we need to re-adjust it with new restrictions
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(gto.getIdentifier());
        final GeneratedTransferObject parent = gto.getSuperType();
        if (parent != null) {
            builder.setExtendsType(parent);
        }
        builder.setRestrictions(restrictions);
        for (GeneratedProperty gp : gto.getProperties()) {
            builder.addProperty(gp.getName())
                .setValue(gp.getValue())
                .setReadOnly(gp.isReadOnly())
                .setAccessModifier(gp.getAccessModifier())
                .setReturnType(gp.getReturnType())
                .setFinal(gp.isFinal())
                .setStatic(gp.isStatic());
        }
        return builder.build();
    }

    @Override
    final void addAsGetterMethodOverride(final GeneratedTypeBuilderBase<?> builder,
            final TypeBuilderFactory builderFactory) {
        if (!(refType instanceof ResolvedLeafref)) {
            // We are not dealing with a leafref or have nothing to add
            return;
        }

        final AbstractTypeObjectGenerator<?, ?> prev =
            (AbstractTypeObjectGenerator<?, ?>) verifyNotNull(previous(), "Missing previous link in %s", this);
        if (prev.refType instanceof ResolvedLeafref) {
            // We should be already inheriting the correct type
            return;
        }

        // Note: this may we wrapped for leaf-list, hence we need to deal with that
        final Type myType = methodReturnType(builderFactory);
        LOG.trace("Override of {} to {}", this, myType);
        final MethodSignatureBuilder getter = constructGetter(builder, myType);
        getter.addAnnotation(OVERRIDE_ANNOTATION);
        annotateDeprecatedIfNecessary(getter);
    }

    final @Nullable Restrictions computeRestrictions() {
        final List<ValueRange> length = type.findFirstEffectiveSubstatementArgument(LengthEffectiveStatement.class)
            .orElse(List.of());
        final List<ValueRange> range = type.findFirstEffectiveSubstatementArgument(RangeEffectiveStatement.class)
            .orElse(List.of());
        final List<PatternExpression> patterns = type.streamEffectiveSubstatements(PatternEffectiveStatement.class)
            .map(PatternEffectiveStatement::argument)
            .collect(Collectors.toUnmodifiableList());

        if (length.isEmpty() && range.isEmpty() && patterns.isEmpty()) {
            return null;
        }

        return BindingGeneratorUtil.getRestrictions(extractTypeDefinition());
    }

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        if (baseGen != null) {
            final GeneratedType baseType = baseGen.getGeneratedType(builderFactory);
            verify(baseType instanceof GeneratedTransferObject, "Unexpected base type %s", baseType);
            return createDerivedType(builderFactory, (GeneratedTransferObject) baseType);
        }

        // FIXME: why do we need this boolean?
        final boolean isTypedef = this instanceof TypedefGenerator;
        final QName arg = type.argument();
        if (TypeDefinitions.BITS.equals(arg)) {
            return createBits(builderFactory, statement(), typeName(), currentModule(),
                (BitsTypeDefinition) extractTypeDefinition(), isTypedef);
        } else if (TypeDefinitions.ENUMERATION.equals(arg)) {
            return createEnumeration(builderFactory, statement(), typeName(), currentModule(),
                (EnumTypeDefinition) extractTypeDefinition());
        } else if (TypeDefinitions.UNION.equals(arg)) {
            final List<GeneratedType> tmp = new ArrayList<>(1);
            final GeneratedTransferObject ret = createUnion(tmp, builderFactory, statement(), unionDependencies,
                typeName(), currentModule(), type, isTypedef, extractTypeDefinition());
            auxiliaryGeneratedTypes = List.copyOf(tmp);
            return ret;
        } else {
            return createSimple(builderFactory, statement(), typeName(), currentModule(),
                verifyNotNull(SIMPLE_TYPES.get(arg), "Unhandled type %s", arg), extractTypeDefinition());
        }
    }

    private static @NonNull GeneratedTransferObject createBits(final TypeBuilderFactory builderFactory,
            final EffectiveStatement<?, ?> definingStatement, final JavaTypeName typeName, final ModuleGenerator module,
            final BitsTypeDefinition typedef, final boolean isTypedef) {
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName);
        builder.setTypedef(isTypedef);
        builder.addImplementsType(BindingTypes.BITS_TYPE_OBJECT);
        builder.setBaseType(typedef);
        YangSourceDefinition.of(module.statement(), definingStatement).ifPresent(builder::setYangSourceDefinition);

        for (Bit bit : typedef.getBits()) {
            final String name = bit.getName();
            GeneratedPropertyBuilder genPropertyBuilder = builder.addProperty(Naming.getPropertyName(name));
            genPropertyBuilder.setReadOnly(true);
            genPropertyBuilder.setReturnType(Types.primitiveBooleanType());

            builder.addEqualsIdentity(genPropertyBuilder);
            builder.addHashIdentity(genPropertyBuilder);
            builder.addToStringProperty(genPropertyBuilder);
        }
        builder.addConstant(Types.immutableSetTypeFor(Types.STRING), TypeConstants.VALID_NAMES_NAME, typedef);

        // builder.setSchemaPath(typedef.getPath());
        builder.setModuleName(module.statement().argument().getLocalName());
        builderFactory.addCodegenInformation(typedef, builder);
        annotateDeprecatedIfNecessary(typedef, builder);
        makeSerializable(builder);
        return builder.build();
    }

    private static @NonNull Enumeration createEnumeration(final TypeBuilderFactory builderFactory,
            final EffectiveStatement<?, ?> definingStatement, final JavaTypeName typeName,
            final ModuleGenerator module, final EnumTypeDefinition typedef) {
        // TODO units for typedef enum
        final AbstractEnumerationBuilder builder = builderFactory.newEnumerationBuilder(typeName);
        YangSourceDefinition.of(module.statement(), definingStatement).ifPresent(builder::setYangSourceDefinition);

        typedef.getDescription().map(BindingGeneratorUtil::encodeAngleBrackets)
            .ifPresent(builder::setDescription);
        typedef.getReference().ifPresent(builder::setReference);

        builder.setModuleName(module.statement().argument().getLocalName());
        builder.updateEnumPairsFromEnumTypeDef(typedef);
        return builder.toInstance();
    }

    private static @NonNull GeneratedType createSimple(final TypeBuilderFactory builderFactory,
            final EffectiveStatement<?, ?> definingStatement, final JavaTypeName typeName, final ModuleGenerator module,
            final Type javaType, final TypeDefinition<?> typedef) {
        final String moduleName = module.statement().argument().getLocalName();
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName);
        builder.setTypedef(true);
        builder.addImplementsType(BindingTypes.scalarTypeObject(javaType));
        YangSourceDefinition.of(module.statement(), definingStatement).ifPresent(builder::setYangSourceDefinition);

        final GeneratedPropertyBuilder genPropBuilder = builder.addProperty(TypeConstants.VALUE_PROP);
        genPropBuilder.setReturnType(javaType);
        builder.addEqualsIdentity(genPropBuilder);
        builder.addHashIdentity(genPropBuilder);
        builder.addToStringProperty(genPropBuilder);

        builder.setRestrictions(BindingGeneratorUtil.getRestrictions(typedef));

//        builder.setSchemaPath(typedef.getPath());
        builder.setModuleName(moduleName);
        builderFactory.addCodegenInformation(typedef, builder);

        annotateDeprecatedIfNecessary(typedef, builder);

        if (javaType instanceof ConcreteType
            // FIXME: This looks very suspicious: we should by checking for Types.STRING
            && "String".equals(javaType.getName()) && typedef.getBaseType() != null) {
            addStringRegExAsConstant(builder, resolveRegExpressions(typedef));
        }
        addUnits(builder, typedef);

        makeSerializable(builder);
        return builder.build();
    }

    private static @NonNull GeneratedTransferObject createUnion(final List<GeneratedType> auxiliaryGeneratedTypes,
            final TypeBuilderFactory builderFactory, final EffectiveStatement<?, ?> definingStatement,
            final UnionDependencies dependencies, final JavaTypeName typeName, final ModuleGenerator module,
            final TypeEffectiveStatement<?> type, final boolean isTypedef, final TypeDefinition<?> typedef) {
        final GeneratedUnionBuilder builder = builderFactory.newGeneratedUnionBuilder(typeName);
        YangSourceDefinition.of(module.statement(), definingStatement).ifPresent(builder::setYangSourceDefinition);
        builder.addImplementsType(BindingTypes.UNION_TYPE_OBJECT);
        builder.setIsUnion(true);

//        builder.setSchemaPath(typedef.getPath());
        builder.setModuleName(module.statement().argument().getLocalName());
        builderFactory.addCodegenInformation(definingStatement, builder);

        annotateDeprecatedIfNecessary(definingStatement, builder);

        // Pattern string is the key, XSD regex is the value. The reason for this choice is that the pattern carries
        // also negation information and hence guarantees uniqueness.
        final Map<String, String> expressions = new HashMap<>();

        // Linear list of properties generated from subtypes. We need this information for runtime types, as it allows
        // direct mapping of type to corresponding property -- without having to resort to re-resolving the leafrefs
        // again.
        final List<String> typeProperties = new ArrayList<>();

        for (EffectiveStatement<?, ?> stmt : type.effectiveSubstatements()) {
            if (stmt instanceof TypeEffectiveStatement<?> subType) {
                final QName subName = subType.argument();
                final String localName = subName.getLocalName();

                String propSource = localName;
                final Type generatedType;
                if (TypeDefinitions.UNION.equals(subName)) {
                    final JavaTypeName subUnionName = typeName.createEnclosed(
                        provideAvailableNameForGenTOBuilder(typeName.simpleName()));
                    final GeneratedTransferObject subUnion = createUnion(auxiliaryGeneratedTypes, builderFactory,
                        definingStatement, dependencies, subUnionName, module, subType, isTypedef,
                        subType.getTypeDefinition());
                    builder.addEnclosingTransferObject(subUnion);
                    propSource = subUnionName.simpleName();
                    generatedType = subUnion;
                } else if (TypeDefinitions.ENUMERATION.equals(subName)) {
                    final Enumeration subEnumeration = createEnumeration(builderFactory, definingStatement,
                        typeName.createEnclosed(Naming.getClassName(localName), "$"), module,
                        (EnumTypeDefinition) subType.getTypeDefinition());
                    builder.addEnumeration(subEnumeration);
                    generatedType = subEnumeration;
                } else if (TypeDefinitions.BITS.equals(subName)) {
                    final GeneratedTransferObject subBits = createBits(builderFactory, definingStatement,
                        typeName.createEnclosed(Naming.getClassName(localName), "$"), module,
                        (BitsTypeDefinition) subType.getTypeDefinition(), isTypedef);
                    builder.addEnclosingTransferObject(subBits);
                    generatedType = subBits;
                } else if (TypeDefinitions.IDENTITYREF.equals(subName)) {
                    propSource = stmt.findFirstEffectiveSubstatement(BaseEffectiveStatement.class)
                        .orElseThrow(() -> new VerifyException(String.format("Invalid identityref "
                            + "definition %s in %s, missing BASE statement", stmt, definingStatement)))
                        .argument().getLocalName();
                    generatedType = verifyNotNull(dependencies.identityTypes.get(stmt),
                        "Cannot resolve identityref %s in %s", stmt, definingStatement)
                        .methodReturnType(builderFactory);
                } else if (TypeDefinitions.LEAFREF.equals(subName)) {
                    generatedType = verifyNotNull(dependencies.leafTypes.get(stmt),
                        "Cannot resolve leafref %s in %s", stmt, definingStatement)
                        .methodReturnType(builderFactory);
                } else {
                    Type baseType = SIMPLE_TYPES.get(subName);
                    if (baseType == null) {
                        // This has to be a reference to a typedef, let's lookup it up and pick up its type
                        final AbstractTypeObjectGenerator<?, ?> baseGen = verifyNotNull(
                            dependencies.baseTypes.get(subName), "Cannot resolve base type %s in %s", subName,
                            definingStatement);
                        baseType = baseGen.methodReturnType(builderFactory);

                        // FIXME: This is legacy behaviour for leafrefs:
                        if (baseGen.refType instanceof TypeReference.Leafref) {
                            // if there already is a compatible property, do not generate a new one
                            final Type search = baseType;

                            final String matching = builder.getProperties().stream()
                                .filter(prop -> search == ((GeneratedPropertyBuilderImpl) prop).getReturnType())
                                .findFirst()
                                .map(GeneratedPropertyBuilder::getName)
                                .orElse(null);
                            if (matching != null) {
                                typeProperties.add(matching);
                                continue;
                            }

                            // ... otherwise generate this weird property name
                            propSource = Naming.getUnionLeafrefMemberName(builder.getName(), baseType.getName());
                        }
                    }

                    expressions.putAll(resolveRegExpressions(subType.getTypeDefinition()));

                    generatedType = restrictType(baseType,
                        BindingGeneratorUtil.getRestrictions(type.getTypeDefinition()), builderFactory);
                }

                final String propName = Naming.getPropertyName(propSource);
                typeProperties.add(propName);

                if (builder.containsProperty(propName)) {
                    /*
                     *  FIXME: this is not okay, as we are ignoring multiple base types. For example in the case of:
                     *
                     *    type union {
                     *      type string {
                     *        length 1..5;
                     *      }
                     *      type string {
                     *        length 8..10;
                     *      }
                     *    }
                     *
                     *  We are ending up losing the information about 8..10 being an alternative. This is also the case
                     *  for leafrefs -- we are performing property compression as well (see above). While it is alluring
                     *  to merge these into 'length 1..5|8..10', that may not be generally feasible.
                     *
                     *  We should resort to a counter of conflicting names, i.e. the second string would be mapped to
                     *  'string1' or similar.
                     */
                    continue;
                }

                final GeneratedPropertyBuilder propBuilder = builder
                    .addProperty(propName)
                    .setReturnType(generatedType);

                builder.addEqualsIdentity(propBuilder);
                builder.addHashIdentity(propBuilder);
                builder.addToStringProperty(propBuilder);
            }
        }

        // Record property names if needed
        builder.setTypePropertyNames(typeProperties);

        addStringRegExAsConstant(builder, expressions);
        addUnits(builder, typedef);

        makeSerializable(builder);
        return builder.build();
    }

    // FIXME: we should not rely on TypeDefinition
    abstract @NonNull TypeDefinition<?> extractTypeDefinition();

    abstract @NonNull GeneratedTransferObject createDerivedType(@NonNull TypeBuilderFactory builderFactory,
        @NonNull GeneratedTransferObject baseType);

    /**
     * Adds to the {@code genTOBuilder} the constant which contains regular expressions from the {@code expressions}.
     *
     * @param genTOBuilder generated TO builder to which are {@code regular expressions} added
     * @param expressions list of string which represent regular expressions
     */
    static void addStringRegExAsConstant(final GeneratedTOBuilder genTOBuilder, final Map<String, String> expressions) {
        if (!expressions.isEmpty()) {
            genTOBuilder.addConstant(Types.listTypeFor(BaseYangTypes.STRING_TYPE), TypeConstants.PATTERN_CONSTANT_NAME,
                ImmutableMap.copyOf(expressions));
        }
    }

    /**
     * Converts the pattern constraints from {@code typedef} to the list of the strings which represents these
     * constraints.
     *
     * @param typedef extended type in which are the pattern constraints sought
     * @return list of strings which represents the constraint patterns
     * @throws IllegalArgumentException if <code>typedef</code> equals null
     */
    static Map<String, String> resolveRegExpressions(final TypeDefinition<?> typedef) {
        return typedef instanceof StringTypeDefinition stringTypedef
            // TODO: run diff against base ?
            ? resolveRegExpressions(stringTypedef.getPatternConstraints())
                : ImmutableMap.of();
    }

    /**
     * Converts the pattern constraints to the list of the strings which represents these constraints.
     *
     * @param patternConstraints list of pattern constraints
     * @return list of strings which represents the constraint patterns
     */
    private static Map<String, String> resolveRegExpressions(final List<PatternConstraint> patternConstraints) {
        if (patternConstraints.isEmpty()) {
            return ImmutableMap.of();
        }

        final Map<String, String> regExps = Maps.newHashMapWithExpectedSize(patternConstraints.size());
        for (PatternConstraint patternConstraint : patternConstraints) {
            String regEx = patternConstraint.getJavaPatternString();

            // The pattern can be inverted
            final Optional<ModifierKind> optModifier = patternConstraint.getModifier();
            if (optModifier.isPresent()) {
                regEx = applyModifier(optModifier.get(), regEx);
            }

            regExps.put(regEx, patternConstraint.getRegularExpressionString());
        }

        return regExps;
    }

    /**
     * Returns string which contains the same value as <code>name</code> but integer suffix is incremented by one. If
     * <code>name</code> contains no number suffix, a new suffix initialized at 1 is added. A suffix is actually
     * composed of a '$' marker, which is safe, as no YANG identifier can contain '$', and a unsigned decimal integer.
     *
     * @param name string with name of augmented node
     * @return string with the number suffix incremented by one (or 1 is added)
     */
    private static String provideAvailableNameForGenTOBuilder(final String name) {
        final int dollar = name.indexOf('$');
        if (dollar == -1) {
            return name + "$1";
        }

        final int newSuffix = Integer.parseUnsignedInt(name.substring(dollar + 1)) + 1;
        verify(newSuffix > 0, "Suffix counter overflow");
        return name.substring(0, dollar + 1) + newSuffix;
    }

    private static String applyModifier(final ModifierKind modifier, final String pattern) {
        return switch (modifier) {
            case INVERT_MATCH -> RegexPatterns.negatePatternString(pattern);
        };
    }
}
