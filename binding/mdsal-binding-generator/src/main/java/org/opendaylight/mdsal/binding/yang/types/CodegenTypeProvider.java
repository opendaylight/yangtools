/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import static org.opendaylight.mdsal.binding.generator.BindingGeneratorUtil.encodeAngleBrackets;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.AbstractEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.binding.RegexPatterns;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractTypeProvider} which generates full metadata, suitable for codegen purposes. For runtime purposes,
 * considering using {@link RuntimeTypeProvider}.
 */
// FIXME: remove this class
@Deprecated(forRemoval = true)
final class CodegenTypeProvider extends AbstractTypeProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CodegenTypeProvider.class);

    @VisibleForTesting
    CodegenTypeProvider(final EffectiveModelContext schemaContext) {
        super(schemaContext);
    }

    @Override
    public void addEnumDescription(final EnumBuilder enumBuilder, final EnumTypeDefinition enumTypeDef) {
        final Optional<String> optDesc = enumTypeDef.getDescription();
        if (optDesc.isPresent()) {
            enumBuilder.setDescription(encodeAngleBrackets(optDesc.get()));
        }
    }

    @Override
    void addCodegenInformation(final GeneratedTypeBuilderBase<?> genTOBuilder, final TypeDefinition<?> typeDef) {
        final Optional<String> optDesc = typeDef.getDescription();
        if (optDesc.isPresent()) {
            genTOBuilder.setDescription(encodeAngleBrackets(optDesc.get()));
        }
        typeDef.getReference().ifPresent(genTOBuilder::setReference);
    }

    /**
     * Converts the pattern constraints to the list of
     * the strings which represents these constraints.
     *
     * @param patternConstraints
     *            list of pattern constraints
     * @return list of strings which represents the constraint patterns
     */
    @Override
    public Map<String, String> resolveRegExpressions(final List<PatternConstraint> patternConstraints) {
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

    private static String applyModifier(final ModifierKind modifier, final String pattern) {
        switch (modifier) {
            case INVERT_MATCH:
                return RegexPatterns.negatePatternString(pattern);
            default:
                LOG.warn("Ignoring unhandled modifier {}", modifier);
                return pattern;
        }
    }

    @Override
    public GeneratedTOBuilder newGeneratedTOBuilder(final JavaTypeName identifier) {
        return new CodegenGeneratedTOBuilder(identifier);
    }

    @Override
    public GeneratedTypeBuilder newGeneratedTypeBuilder(final JavaTypeName identifier) {
        return new CodegenGeneratedTypeBuilder(identifier);
    }

    @Override
    public AbstractEnumerationBuilder newEnumerationBuilder(final JavaTypeName identifier) {
        return new CodegenEnumerationBuilder(identifier);
    }
}
