/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;

public class CrossSourceStatementReactor {

    private final Map<ModelProcessingPhase, StatementSupportBundle> supportedTerminology;
    private final Map<ValidationBundleType, Collection<?>> supportedValidation;

    CrossSourceStatementReactor(final Map<ModelProcessingPhase, StatementSupportBundle> supportedTerminology) {
        this.supportedTerminology = ImmutableMap.copyOf(supportedTerminology);
        this.supportedValidation = ImmutableMap.of();
    }

    CrossSourceStatementReactor(final Map<ModelProcessingPhase, StatementSupportBundle> supportedTerminology,
            final Map<ValidationBundleType, Collection<?>> supportedValidation) {
        this.supportedTerminology = ImmutableMap.copyOf(supportedTerminology);
        this.supportedValidation = ImmutableMap.copyOf(supportedValidation);
    }

    public static Builder builder() {
        return new Builder();
    }

    public final BuildAction newBuild() {
        return newBuild(StatementParserMode.DEFAULT_MODE, t -> true);
    }

    public final BuildAction newBuild(final Predicate<QName> isFeatureSupported) {
        return new BuildAction(StatementParserMode.DEFAULT_MODE, isFeatureSupported);
    }

    public final BuildAction newBuild(final StatementParserMode statementParserMode) {
        return new BuildAction(statementParserMode, t -> true);
    }

    public final BuildAction newBuild(final StatementParserMode statementParserMode,
            final Predicate<QName> isFeatureSupported) {
        return new BuildAction(statementParserMode, isFeatureSupported);
    }

    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<CrossSourceStatementReactor> {

        final Map<ModelProcessingPhase, StatementSupportBundle> bundles = new EnumMap<>(ModelProcessingPhase.class);
        final Map<ValidationBundleType, Collection<?>> validationBundles = new EnumMap<>(ValidationBundleType.class);

        public Builder setBundle(final ModelProcessingPhase phase, final StatementSupportBundle bundle) {
            bundles.put(phase, bundle);
            return this;
        }

        public Builder setValidationBundle(final ValidationBundleType type, final Collection<?> validationBundle) {
            validationBundles.put(type, validationBundle);
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
            this(StatementParserMode.DEFAULT_MODE, t -> true);
        }

        public BuildAction(StatementParserMode statementParserMode) {
            this(statementParserMode, t -> true);
        }

        public BuildAction(Predicate<QName> isFeatureSupported) {
            this(StatementParserMode.DEFAULT_MODE, isFeatureSupported);
        }

        public BuildAction(StatementParserMode statementParserMode, Predicate<QName> isFeatureSupported) {
            this.context = new BuildGlobalContext(supportedTerminology, supportedValidation, statementParserMode,
                    isFeatureSupported);
        }

        public void addSource(final StatementStreamSource source) {
            context.addSource(source);
        }

        public void addSources(final StatementStreamSource... sources) {
            for (StatementStreamSource source : sources) {
                context.addSource(source);
            }
        }

        /**
         * @throws org.opendaylight.yangtools.yang.parser.spi.source.SourceException
         * @throws ReactorException
         */
        public EffectiveModelContext build() throws ReactorException {
            return context.build();
        }

        public EffectiveSchemaContext buildEffective() throws ReactorException {
            return context.buildEffective();
        }

        public SchemaContext buildEffective(final Collection<ByteSource> yangByteSources) throws ReactorException,
                IOException {
            for (ByteSource yangByteSource : yangByteSources) {
                addSource(new YangStatementSourceImpl(yangByteSource.openStream()));
            }

            return buildEffective();
        }

        public SchemaContext buildEffective(final List<InputStream> yangInputStreams) throws ReactorException {
            for (InputStream yangInputStream : yangInputStreams) {
                addSource(new YangStatementSourceImpl(yangInputStream));
            }

            return buildEffective();
        }

        /**
         * @deprecated This method was never used and relies on deprecated
         *             module methods.
         */
        @Deprecated
        public Map<File, Module> buildEffectiveMappedToSource(final List<File> yangFiles) throws ReactorException,
                FileNotFoundException {
            if (yangFiles == null || yangFiles.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<String, File> pathToFile = new HashMap<>();
            Map<File, Module> sourceFileToModule = new HashMap<>();

            for (File yangFile : yangFiles) {
                addSource(new YangStatementSourceImpl(new NamedFileInputStream(yangFile, yangFile.getPath())));
                pathToFile.put(yangFile.getPath(), yangFile);
            }

            EffectiveSchemaContext schema = buildEffective();
            Set<Module> modules = schema.getModules();
            for (Module module : modules) {
                sourceFileToModule.put(pathToFile.get(module.getModuleSourcePath()), module);
            }

            return sourceFileToModule;
        }
    }
}