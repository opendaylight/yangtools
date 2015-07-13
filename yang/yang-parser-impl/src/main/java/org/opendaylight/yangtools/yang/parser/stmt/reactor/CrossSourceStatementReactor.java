/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import java.util.Collection;

import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import com.google.common.collect.ImmutableMap;
import java.util.EnumMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

public class CrossSourceStatementReactor {

    private final Map<ModelProcessingPhase,StatementSupportBundle> supportedTerminology;
    private final Map<ValidationBundleType,Collection<?>> supportedValidation;

    CrossSourceStatementReactor(Map<ModelProcessingPhase, StatementSupportBundle> supportedTerminology) {
        this.supportedTerminology = ImmutableMap.copyOf(supportedTerminology);
        this.supportedValidation = ImmutableMap.of();
    }

    CrossSourceStatementReactor(Map<ModelProcessingPhase, StatementSupportBundle> supportedTerminology, Map<ValidationBundleType,Collection<?>> supportedValidation) {
        this.supportedTerminology = ImmutableMap.copyOf(supportedTerminology);
        this.supportedValidation = ImmutableMap.copyOf(supportedValidation);
    }

    public static final Builder builder() {
        return new Builder();
    }

    public final BuildAction newBuild() {
        return new BuildAction();
    }

    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<CrossSourceStatementReactor>{

        final Map<ModelProcessingPhase,StatementSupportBundle> bundles = new EnumMap<>(ModelProcessingPhase.class);
        final Map<ValidationBundleType,Collection<?>> validationBundles = new EnumMap<>(ValidationBundleType.class);

        public Builder setBundle(ModelProcessingPhase phase,StatementSupportBundle bundle) {
            bundles.put(phase, bundle);
            return this;
        }


        public Builder setValidationBundle(
                ValidationBundleType type,
                Collection<?> validationBundle) {
            validationBundles.put(type,validationBundle);
            return this;
        }

        @Override
        public CrossSourceStatementReactor build() {
            return new CrossSourceStatementReactor(bundles, validationBundles);
        }

    }

    public class BuildAction {

        private final BuildGlobalContext context;

        public BuildAction() {
            this.context = new BuildGlobalContext(supportedTerminology, supportedValidation);
        }

        public void addSource(StatementStreamSource source) {
            context.addSource(source);
        }

        public void addSources(StatementStreamSource... sources) {
            for (StatementStreamSource source : sources) {
                context.addSource(source);
            }
        }

        public EffectiveModelContext build() throws SourceException, ReactorException {
            return context.build();
        }

        public EffectiveSchemaContext buildEffective() throws SourceException, ReactorException {
            return context.buildEffective();
        }

    }


}
