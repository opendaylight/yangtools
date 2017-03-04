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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CrossSourceStatementReactor {
    private static final Logger LOG = LoggerFactory.getLogger(CrossSourceStatementReactor.class);

    private final Map<ModelProcessingPhase, StatementSupportBundle> supportedTerminology;
    private final Map<ValidationBundleType, Collection<?>> supportedValidation;

    CrossSourceStatementReactor(final Map<ModelProcessingPhase, StatementSupportBundle> supportedTerminology,
            final Map<ValidationBundleType, Collection<?>> supportedValidation) {
        this.supportedTerminology = ImmutableMap.copyOf(supportedTerminology);
        this.supportedValidation = ImmutableMap.copyOf(supportedValidation);
    }

    /**
     * Create a new {@link Builder}.
     *
     * @return A new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Start a new reactor build using default statement parser mode and enabling all features.
     *
     * @return A new {@link BuildAction}.
     */
    public final BuildAction newBuild() {
        return newBuild(StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Start a new reactor build using default statement parser mode and only specified features.
     *
     * @param supportedFeatures The set of supported features in the final SchemaContext
     * @return A new {@link BuildAction}.
     */
    public final BuildAction newBuild(final Set<QName> supportedFeatures) {
        return new BuildAction(warnOnNull(supportedFeatures), StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Start a new reactor build using specified statement parser mode and enabling all features.
     *
     * @param statementParserMode Parser mode to use
     * @return A new {@link BuildAction}.
     * @throws NullPointerException if statementParserMode is null
     */
    public final BuildAction newBuild(final StatementParserMode statementParserMode) {
        return new BuildAction(null, statementParserMode);
    }

    /**
     * Start a new reactor build using default statement parser mode and only specified features.
     *
     * @param supportedFeatures The set of supported features in the final SchemaContext
     * @return A new {@link BuildAction}.
     * @throws NullPointerException if statementParserMode is null
     */
    public final BuildAction newBuild(final StatementParserMode statementParserMode,
            final Set<QName> supportedFeatures) {
        return new BuildAction(warnOnNull(supportedFeatures), statementParserMode);
    }

    private static <T> T warnOnNull(final T obj) {
        if (obj == null) {
            LOG.info("Set of supported features has not been provided, so all features are supported by default.");
        }
        return obj;
    }

    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<CrossSourceStatementReactor> {
        private final Map<ValidationBundleType, Collection<?>> validationBundles =
                new EnumMap<>(ValidationBundleType.class);
        private final Map<ModelProcessingPhase, StatementSupportBundle> bundles =
                new EnumMap<>(ModelProcessingPhase.class);

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

        BuildAction(final Set<QName> supportedFeatures, final StatementParserMode statementParserMode) {
            this.context = new BuildGlobalContext(supportedTerminology, supportedValidation, statementParserMode,
                supportedFeatures);
        }

        /**
         * Add main source. All main sources are present in resulting
         * SchemaContext.
         *
         * @param source
         *            which should be added into main sources
         */
        public void addSource(final StatementStreamSource source) {
            context.addSource(source);
        }

        /**
         * Add main sources. All main sources are present in resulting
         * SchemaContext.
         *
         * @param sources
         *            which should be added into main sources
         */
        public void addSources(final StatementStreamSource... sources) {
            addSources(Arrays.asList(sources));
        }

        public void addSources(final Collection<? extends StatementStreamSource> sources) {
            for (final StatementStreamSource source : sources) {
                context.addSource(source);
            }
        }

        /**
         * Add library sources. Only library sources required by main sources
         * are present in resulting SchemaContext. Any other library sources are
         * ignored and this also applies to error reporting.
         *
         * Library sources are not supported in semantic version mode currently.
         *
         * @param libSources
         *            yang sources which should be added into library sources
         */
        public void addLibSources(final StatementStreamSource... libSources) {
            for (final StatementStreamSource libSource : libSources) {
                context.addLibSource(libSource);
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

        /**
         * @deprecated Use {@link #addSources(Collection)} and {@link #buildEffective()} instead.
         */
        @Deprecated
        public SchemaContext buildEffective(final Collection<ByteSource> yangByteSources) throws ReactorException,
                IOException {
            for (final ByteSource source : yangByteSources) {
                if (source instanceof YangTextSchemaSource) {
                    try {
                        addSource(YangStatementStreamSource.create((YangTextSchemaSource) source));
                    } catch (YangSyntaxErrorException e) {
                        throw new IOException("Source " + source + " failed to parse", e);
                    }
                } else {
                    addSource(new YangStatementSourceImpl(source.openStream()));
                }
            }

            return buildEffective();
        }

        /**
         * @deprecated Use {@link #addSources(Collection)} and {@link #buildEffective()} instead.
         */
        @Deprecated
        public SchemaContext buildEffective(final List<InputStream> yangInputStreams) throws ReactorException {
            for (final InputStream yangInputStream : yangInputStreams) {
                addSource(new YangStatementSourceImpl(yangInputStream));
            }

            return buildEffective();
        }
    }
}