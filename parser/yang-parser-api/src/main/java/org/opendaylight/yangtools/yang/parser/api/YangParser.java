/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;

/**
 * Configurable single-use YANG parser. Each instance can be configured to use a different set of models after
 * which it is built. Models once added cannot be removed. Implementations are expected to be NOT thread-safe.
 */
@Beta
public interface YangParser {
    /**
     * Return enumeration of concrete types of {@link SourceRepresentation} parsers created from this factory
     * support. Users can use this information prepare the source they have to a representation which will be accepted
     * by this parser.
     *
     * @return Enumeration of supported schema source representations.
     */
    @NonNull Collection<Class<? extends SourceRepresentation>> supportedSourceRepresentations();

    /**
     * Add main source. All main sources are present in resulting SchemaContext.
     *
     * @param source which should be added into main sources
     * @throws YangSyntaxErrorException when one of the sources fails syntactic analysis
     * @throws IOException when an IO error occurs
     * @throws IllegalArgumentException if the representation is not supported
     */
    @NonNull YangParser addSource(SourceRepresentation source) throws IOException, YangSyntaxErrorException;

    /**
     * Add main sources. All main sources are present in resulting SchemaContext.
     *
     * @param sources which should be added into main sources
     * @throws YangSyntaxErrorException when one of the sources fails syntactic analysis
     * @throws IOException when an IO error occurs
     * @throws IllegalArgumentException if the representation is not supported
     */
    default @NonNull YangParser addSources(final SourceRepresentation... sources)
            throws IOException, YangSyntaxErrorException {
        for (var source : sources) {
            addSource(source);
        }
        return this;
    }

    default @NonNull YangParser addSources(final Collection<? extends SourceRepresentation> sources)
            throws IOException, YangSyntaxErrorException {
        for (var source : sources) {
            addSource(source);
        }
        return this;
    }

    YangParser addLibSource(SourceRepresentation source) throws IOException, YangSyntaxErrorException;

    /**
     * Add library sources. Only library sources required by main sources are present in resulting SchemaContext.
     * Any other library sources are ignored and this also applies to error reporting.
     *
     * <p>
     * Note: Library sources are not supported in semantic version mode currently.
     *
     * @param sources YANG sources which should be added into library sources
     * @throws YangSyntaxErrorException when one of the sources fails syntactic analysis
     * @throws IOException when an IO error occurs
     * @throws IllegalArgumentException if the representation is not supported
     */
    default @NonNull YangParser addLibSources(final SourceRepresentation... sources)
            throws IOException, YangSyntaxErrorException {
        for (var source : sources) {
            addLibSource(source);
        }
        return this;
    }

    default @NonNull YangParser addLibSources(final Collection<SourceRepresentation> sources)
            throws IOException, YangSyntaxErrorException {
        for (var source : sources) {
            addLibSource(source);
        }
        return this;
    }

    /**
     * Set supported features based on which all if-feature statements in the parsed YANG modules will be resolved. If
     * this method is not invoked, all features will be supported.
     *
     * @param supportedFeatures Set of supported features in the final SchemaContext. If the set is empty, no features
     *                          encountered will be supported.
     */
    @NonNull YangParser setSupportedFeatures(@NonNull FeatureSet supportedFeatures);

    /**
     * Set YANG modules which can be deviated by specified modules during the parsing process. Map key (QNameModule)
     * denotes a module which can be deviated by the modules in the Map value.
     *
     * @param modulesDeviatedByModules Map of YANG modules (Map key) which can be deviated by specified modules (Map
     *                                 value) in the final SchemaContext. If the map is empty, no deviations encountered
     *                                 will be supported.
     */
    @NonNull YangParser setModulesWithSupportedDeviations(
            @NonNull SetMultimap<QNameModule, QNameModule> modulesDeviatedByModules);

    /**
     * Build the declared view of a combined view of declared statements.
     *
     * @return Ordered collection of declared statements from requested sources.
     * @throws YangSyntaxErrorException When a syntactic error is encountered.
     */
    @NonNull List<DeclaredStatement<?>> buildDeclaredModel() throws YangParserException;

    /**
     * Build the effective view of a combined view of effective statements. Note that this representation, unlike
     * {@link #buildDeclaredModel()} does not expose submodules as top-level contracts. These are available from their
     * respective parent modules.
     *
     * @return Effective module statements indexed by their QNameModule.
     * @throws YangSyntaxErrorException When a syntactic error is encountered.
     */
    @NonNull EffectiveModelContext buildEffectiveModel() throws YangParserException;
}
