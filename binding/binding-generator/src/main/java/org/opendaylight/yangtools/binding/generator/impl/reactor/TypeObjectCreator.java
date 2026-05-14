/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.generator.impl.reactor.TypeObjectSupport.Union.Dependencies;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.BuiltInType;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

/**
 * Utility class for creating {@link TypeObjectArchetype}s.
 */
@NonNullByDefault
final class TypeObjectCreator {
    // FIXME: remove this map
    private static final ImmutableMap<QName, ConcreteType> SIMPLE_TYPES =
        ImmutableMap.<QName, ConcreteType>builder()
            .put(BuiltInType.BINARY.typeName(), BaseYangTypes.BINARY_TYPE)
            .put(BuiltInType.BOOLEAN.typeName(), BaseYangTypes.BOOLEAN_TYPE)
            .put(BuiltInType.EMPTY.typeName(), BaseYangTypes.EMPTY_TYPE)
            .put(BuiltInType.INSTANCE_IDENTIFIER.typeName(), BaseYangTypes.INSTANCE_IDENTIFIER)
            .put(BuiltInType.INT8.typeName(), BaseYangTypes.INT8_TYPE)
            .put(BuiltInType.INT16.typeName(), BaseYangTypes.INT16_TYPE)
            .put(BuiltInType.INT32.typeName(), BaseYangTypes.INT32_TYPE)
            .put(BuiltInType.INT64.typeName(), BaseYangTypes.INT64_TYPE)
            .put(BuiltInType.STRING.typeName(), BaseYangTypes.STRING_TYPE)
            .put(BuiltInType.UINT8.typeName(), BaseYangTypes.UINT8_TYPE)
            .put(BuiltInType.UINT16.typeName(), BaseYangTypes.UINT16_TYPE)
            .put(BuiltInType.UINT32.typeName(), BaseYangTypes.UINT32_TYPE)
            .put(BuiltInType.UINT64.typeName(), BaseYangTypes.UINT64_TYPE)
            .build();

    private final TypeEffectiveStatement.MandatoryIn<?, ?> definingStatement;
    private final TypeBuilderFactory builderFactory;
    private final ModuleEffectiveStatement module;

    private TypeObjectCreator(final TypeEffectiveStatement.MandatoryIn<?, ?> definingStatement,
            final TypeBuilderFactory builderFactory, final ModuleEffectiveStatement module) {
        this.definingStatement = requireNonNull(definingStatement);
        this.builderFactory = requireNonNull(builderFactory);
        this.module = requireNonNull(module);
    }

    static Map.Entry<UnionTypeObjectArchetype, List<GeneratedType>> createUnionTypeObjectArchetype(
            final JavaTypeName typeName, final TypeEffectiveStatement.MandatoryIn<?, ?> statement,
            final UnionTypeDefinition typeDefinition, final TypeEffectiveStatement type,
            final Dependencies dependencies, final TypeBuilderFactory builderFactory,
            final ModuleEffectiveStatement module) {
        final var tmp = new ArrayList<GeneratedType>(1);
        final var archetype = new TypeObjectCreator(statement, builderFactory, module)
            .createUnion(tmp, dependencies, typeName, type, typeDefinition);
        return Map.entry(archetype, tmp);
    }

    private UnionTypeObjectArchetype createUnion(final List<GeneratedType> auxiliaryGeneratedTypes,
            final Dependencies dependencies, final JavaTypeName typeName, final TypeEffectiveStatement type,
            final TypeDefinition<?> typedef) {
        final var builder = builderFactory.newUnionTypeObjectBuilder(typeName);
        YangSourceDefinition.of(module, definingStatement).ifPresent(builder::setYangSourceDefinition);
        builder.addImplementsType(BindingTypes.UNION_TYPE_OBJECT);
        builder.setModuleName(module.argument().getLocalName());
        builderFactory.addCodegenInformation(definingStatement, builder);

        AbstractTypeObjectGenerator.annotateDeprecatedIfNecessary(definingStatement, builder);

        // Pattern string is the key, XSD regex is the value. The reason for this choice is that the pattern carries
        // also negation information and hence guarantees uniqueness.
        final var expressions = new HashMap<String, String>();

        // Linear list of properties generated from subtypes. We need this information for runtime types, as it
        // allows direct mapping of type to corresponding property -- without having to resort to re-resolving
        // the leafrefs again.
        final var typeProperties = new ArrayList<String>();

        for (var stmt : type.effectiveSubstatements()) {
            if (stmt instanceof TypeEffectiveStatement subType) {
                final QName subName = subType.argument();
                final String localName = subName.getLocalName();

                String propSource = localName;
                final Type generatedType;
                if (BuiltInType.UNION.typeName().equals(subName)) {
                    final var subUnionName = typeName.createEnclosed(
                        provideAvailableNameForGenTOBuilder(typeName.simpleName()));
                    final var subUnion = createUnion(auxiliaryGeneratedTypes, dependencies, subUnionName, subType,
                        subType.typeDefinition());
                    builder.addEnclosingTransferObject(subUnion);
                    propSource = subUnionName.simpleName();
                    generatedType = subUnion;
                } else if (BuiltInType.ENUMERATION.typeName().equals(subName)) {
                    final var subEnumeration = new EnumTypeObjectArchetype(
                        typeName.createEnclosed(Naming.getClassName(localName), "$"), definingStatement,
                        (EnumTypeDefinition) subType.typeDefinition());
                    builder.addEnumeration(subEnumeration);
                    generatedType = subEnumeration;
                } else if (BuiltInType.BITS.typeName().equals(subName)) {
                    final var subBits = new BitsTypeObjectArchetype(
                        typeName.createEnclosed(Naming.getClassName(localName), "$"), definingStatement,
                        (BitsTypeDefinition) subType.typeDefinition());
                    builder.addEnclosingTransferObject(subBits);
                    generatedType = subBits;
                } else if (BuiltInType.IDENTITYREF.typeName().equals(subName)) {
                    propSource = stmt.findFirstEffectiveSubstatement(BaseEffectiveStatement.class)
                        .orElseThrow(() -> new VerifyException(String.format("Invalid identityref "
                            + "definition %s in %s, missing BASE statement", stmt, definingStatement)))
                        .argument().getLocalName();
                    generatedType = verifyNotNull(dependencies.identityrefOf(stmt),
                        "Cannot resolve identityref %s in %s", stmt, definingStatement)
                        .methodReturnType(builderFactory);
                } else if (BuiltInType.LEAFREF.typeName().equals(subName)) {
                    generatedType = verifyNotNull(dependencies.leafrefOf(stmt),
                        "Cannot resolve leafref %s in %s", stmt, definingStatement)
                        .methodReturnType(builderFactory);
                } else {
                    final var subDef = subType.typeDefinition();

                    Type baseType = SIMPLE_TYPES.get(subName);
                    if (baseType == null) {
                        if (!BuiltInType.DECIMAL64.typeName().equals(subName)) {
                            // This has to be a reference to a typedef, let's lookup it up and pick up its type
                            final var baseGen = verifyNotNull(dependencies.basetypeOf(subName),
                                "Cannot resolve base type %s in %s", subName, definingStatement);
                            baseType = baseGen.methodReturnType(builderFactory);

                            // FIXME: This is legacy behaviour for leafrefs:
                            if (baseGen.isLeafRef()) {
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
                                propSource = getUnionLeafrefMemberName(builder.typeName().simpleName(),
                                    baseType.simpleName());
                            }
                        } else {
                            baseType = Decimal64Type.ofFractionDigits(
                                ((DecimalTypeDefinition) subDef).getFractionDigits());
                        }
                    }

                    expressions.putAll(AbstractTypeObjectGenerator.resolveRegExpressions(subDef));

                    generatedType = AbstractTypeObjectGenerator.restrictType(baseType,
                        AbstractTypeObjectGenerator.getRestrictions(type.typeDefinition()), builderFactory);
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
                     *  We are ending up losing the information about 8..10 being an alternative. This is also
                     *  the case for leafrefs -- we are performing property compression as well (see above). While
                     *  it is alluring to merge these into 'length 1..5|8..10', that may not be generally feasible.
                     *
                     *  We should resort to a counter of conflicting names, i.e. the second string would be mapped
                     *  to 'string1' or similar.
                     */
                    continue;
                }

                builder.addProperty(propName).setReturnType(generatedType);
            }
        }

        // Record property names if needed
        builder.setTypePropertyNames(typeProperties);

        AbstractTypeObjectGenerator.addStringRegExAsConstant(builder, expressions);
        AbstractTypeObjectGenerator.addUnits(builder, typedef);

        AbstractTypeObjectGenerator.makeSerializable(builder);
        return builder.build();
    }

    // FIXME: this is legacy union/leafref property handling. The resulting value is *not* normalized for use as a
    //        property.
    private static String getUnionLeafrefMemberName(final String unionClassSimpleName,
            final String referencedClassSimpleName) {
        return requireNonNull(referencedClassSimpleName) + requireNonNull(unionClassSimpleName) + "Value";
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
        if (newSuffix <= 0) {
            throw new VerifyException("Suffix counter overflow");
        }
        return name.substring(0, dollar + 1) + newSuffix;
    }
}
