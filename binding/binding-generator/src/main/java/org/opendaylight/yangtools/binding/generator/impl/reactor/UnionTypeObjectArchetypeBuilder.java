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
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.generator.impl.reactor.TypeObjectSupport.Union.Dependencies;
import org.opendaylight.yangtools.binding.model.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ReturnType;
import org.opendaylight.yangtools.binding.model.ScalarTypes;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.BuiltInType;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

/**
 * Utility class for creating {@link UnionTypeObjectArchetype}s.
 */
@NonNullByDefault
final class UnionTypeObjectArchetypeBuilder {
    // FIXME: remove this map
    private static final ImmutableMap<QName, @Nullable ConcreteType> SIMPLE_TYPES =
        ImmutableMap.<QName, @Nullable ConcreteType>builder()
            .put(BuiltInType.BINARY.typeName(), ScalarTypes.BINARY)
            .put(BuiltInType.BOOLEAN.typeName(), ScalarTypes.BOOLEAN)
            .put(BuiltInType.EMPTY.typeName(), ScalarTypes.EMPTY)
            .put(BuiltInType.INSTANCE_IDENTIFIER.typeName(), ScalarTypes.INSTANCE_IDENTIFIER)
            .put(BuiltInType.INT8.typeName(), ScalarTypes.INT8)
            .put(BuiltInType.INT16.typeName(), ScalarTypes.INT16)
            .put(BuiltInType.INT32.typeName(), ScalarTypes.INT32)
            .put(BuiltInType.INT64.typeName(), ScalarTypes.INT64)
            .put(BuiltInType.STRING.typeName(), ScalarTypes.STRING)
            .put(BuiltInType.UINT8.typeName(), ScalarTypes.UINT8)
            .put(BuiltInType.UINT16.typeName(), ScalarTypes.UINT16)
            .put(BuiltInType.UINT32.typeName(), ScalarTypes.UINT32)
            .put(BuiltInType.UINT64.typeName(), ScalarTypes.UINT64)
            .build();

    private final TypeEffectiveStatement.MandatoryIn<?, ?> definingStatement;

    private UnionTypeObjectArchetypeBuilder(final TypeEffectiveStatement.MandatoryIn<?, ?> definingStatement) {
        this.definingStatement = requireNonNull(definingStatement);
    }

    static UnionTypeObjectArchetype buildArchetype(final TypeName typeName,
            final TypeEffectiveStatement.MandatoryIn<?, ?> statement, final UnionTypeDefinition typeDefinition,
            final TypeEffectiveStatement type, final Dependencies dependencies) {
        return new UnionTypeObjectArchetypeBuilder(statement).createUnion(dependencies, typeName, type, typeDefinition);
    }

    private UnionTypeObjectArchetype createUnion(
            final Dependencies dependencies, final TypeName typeName, final TypeEffectiveStatement type,
            final TypeDefinition<?> typedef) {
        final var enclosedTypes = new ArrayList<TypeObjectArchetype<?>>();
        // A linear list of properties generated from subtypes. We need this information for runtime types, as it allows
        // direct mapping of type to corresponding property -- without having to resort to re-resolving the leafrefs
        // again.
        final var typeProperties = new ArrayList<String>();
        // type-to-property mapping: this really boils down to canonical name, ensuring we grouping things by upstream
        // types -- we map each of them to a property name.
        final var properties = new LinkedHashMap<Type, String>();

        // FIXME: this is wrong: each expression set should be bound to a particular union property case
        // Pattern string is the key, XSD regex is the value. The reason for this choice is that the pattern carries
        // also negation information and hence guarantees uniqueness.
        // final var expressions = new HashMap<String, String>();

        for (var stmt : type.effectiveSubstatements()) {
            if (stmt instanceof TypeEffectiveStatement subType) {
                final QName subName = subType.argument();
                final String localName = subName.getLocalName();

                String propSource = localName;
                final Type generatedType;
                if (BuiltInType.UNION.typeName().equals(subName)) {
                    final var subUnionName = typeName.createEnclosed(
                        provideAvailableNameForGenTOBuilder(typeName.simpleName()));
                    final var subUnion = createUnion(dependencies, subUnionName, subType, subType.typeDefinition());
                    enclosedTypes.add(subUnion);
                    propSource = subUnionName.simpleName();
                    generatedType = subUnion;
                } else if (BuiltInType.ENUMERATION.typeName().equals(subName)) {
                    final var subEnumeration = EnumTypeObjectArchetype.of(
                        typeName.createEnclosed(Naming.getClassName(localName), "$"), definingStatement,
                        (EnumTypeDefinition) subType.typeDefinition());
                    enclosedTypes.add(subEnumeration);
                    generatedType = subEnumeration;
                } else if (BuiltInType.BITS.typeName().equals(subName)) {
                    final var subBits = BitsTypeObjectArchetype.of(
                        typeName.createEnclosed(Naming.getClassName(localName), "$"), definingStatement,
                        (BitsTypeDefinition) subType.typeDefinition());
                    enclosedTypes.add(subBits);
                    generatedType = subBits;
                } else if (BuiltInType.IDENTITYREF.typeName().equals(subName)) {
                    propSource = stmt.findFirstEffectiveSubstatement(BaseEffectiveStatement.class)
                        .orElseThrow(() -> new VerifyException(
                            "Invalid identityref definition %s in %s, missing BASE statement".formatted(
                                stmt, definingStatement)))
                        .argument().getLocalName();
                    generatedType = verifyNotNull(dependencies.identityrefOf(stmt),
                        "Cannot resolve identityref %s in %s", stmt, definingStatement).methodReturnType();
                } else if (BuiltInType.LEAFREF.typeName().equals(subName)) {
                    generatedType = verifyNotNull(dependencies.leafrefOf(stmt),
                        "Cannot resolve leafref %s in %s", stmt, definingStatement).methodReturnType();
                } else {
                    final var subDef = subType.typeDefinition();

                    ReturnType baseType = SIMPLE_TYPES.get(subName);
                    if (baseType == null) {
                        if (!BuiltInType.DECIMAL64.typeName().equals(subName)) {
                            // This has to be a reference to a typedef, let's lookup it up and pick up its type
                            final var baseGen = verifyNotNull(dependencies.basetypeOf(subName),
                                "Cannot resolve base type %s in %s", subName, definingStatement);
                            baseType = baseGen.methodReturnType();

                            // FIXME: This is legacy behaviour for leafrefs:
                            if (baseGen.isLeafRef()) {
                                final var matching = properties.get(baseType);
                                if (matching != null) {
                                    typeProperties.add(matching);
                                    continue;
                                }

                                // ... otherwise generate this weird property name
                                propSource = getUnionLeafrefMemberName(typeName.simpleName(), baseType.simpleName());
                            }
                        } else {
                            baseType = Decimal64Type.ofFractionDigits(
                                ((DecimalTypeDefinition) subDef).getFractionDigits());
                        }
                    }

//                    expressions.putAll(AbstractTypeObjectGenerator.resolveRegExpressions(subDef));

                    generatedType = AbstractTypeObjectGenerator.restrictType(baseType,
                        Restrictions.of(type.typeDefinition()));
                }

                final var propName = Naming.getPropertyName(propSource);
                typeProperties.add(propName);

                if (properties.containsValue(propName)) {
                    // FIXME:
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

                final var prev = properties.put(generatedType, propName);
                if (prev != null) {
                    throw new VerifyException("Unexpected previous property " + propName + " type " + prev);
                }
            }
        }

        return UnionTypeObjectArchetype.of(typeName, definingStatement, typeProperties,
            List.copyOf(properties.keySet()), List.copyOf(enclosedTypes));
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
