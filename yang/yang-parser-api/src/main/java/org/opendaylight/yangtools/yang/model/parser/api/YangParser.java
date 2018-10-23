/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.parser.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.concurrent.NotThreadSafe;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;

/**
 * Configurable single-use YANG parser. Each instance can be configured to use a different set of models after
 * which it is built. Models once added cannot be removed.
 *
 * @author Robert Varga
 */
@Beta
@NotThreadSafe
public interface YangParser {
    /**
     * Return enumeration of concrete types of {@link SchemaSourceRepresentation} parsers created from this factory
     * support. Users can use this information prepare the source they have to a representation which will be accepted
     * by this parser.
     *
     * @return Enumeration of supported schema source representations.
     */
    Collection<Class<? extends SchemaSourceRepresentation>> supportedSourceRepresentations();

    /**
     * Return the set of all YANG statements semantically supported by this parser instance.
     *
     * @return Set of all YANG statements semantically supported by this parser instance.
     */
    Set<QName> supportedStatements();

    /**
     * Add main source. All main sources are present in resulting SchemaContext.
     *
     * @param source
     *            which should be added into main sources
     * @throws YangSyntaxErrorException when one of the sources fails syntactic analysis
     * @throws IOException when an IO error occurs
     * @throws IllegalArgumentException if the representation is not supported
     */
    YangParser addSource(SchemaSourceRepresentation source) throws IOException, YangSyntaxErrorException;

    /**
     * Add main sources. All main sources are present in resulting SchemaContext.
     *
     * @param sources
     *            which should be added into main sources
     * @throws YangSyntaxErrorException when one of the sources fails syntactic analysis
     * @throws IOException when an IO error occurs
     * @throws IllegalArgumentException if the representation is not supported
     */
    default YangParser addSources(final SchemaSourceRepresentation... sources) throws IOException,
        YangSyntaxErrorException {
        for (SchemaSourceRepresentation source : sources) {
            addSource(source);
        }
        return this;
    }

    default YangParser addSources(final Collection<? extends SchemaSourceRepresentation> sources) throws IOException,
        YangSyntaxErrorException {
        for (SchemaSourceRepresentation source : sources) {
            addSource(source);
        }
        return this;
    }

    YangParser addLibSource(SchemaSourceRepresentation source) throws IOException, YangSyntaxErrorException;

    /**
     * Add library sources. Only library sources required by main sources are present in resulting SchemaContext.
     * Any other library sources are ignored and this also applies to error reporting.
     *
     * <p>
     * Note: Library sources are not supported in semantic version mode currently.
     *
     * @param sources
     *            YANG sources which should be added into library sources
     * @throws YangSyntaxErrorException when one of the sources fails syntactic analysis
     * @throws IOException when an IO error occurs
     * @throws IllegalArgumentException if the representation is not supported
     */
    default YangParser addLibSources(final SchemaSourceRepresentation... sources) throws IOException,
            YangSyntaxErrorException {
        for (SchemaSourceRepresentation source : sources) {
            addLibSource(source);
        }
        return this;
    }

    default YangParser addLibSources(final Collection<SchemaSourceRepresentation> sources) throws IOException,
            YangSyntaxErrorException {
        for (SchemaSourceRepresentation source : sources) {
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
    YangParser setSupportedFeatures(@NonNull Set<QName> supportedFeatures);

    /**
     * Set YANG modules which can be deviated by specified modules during the parsing process. Map key (QNameModule)
     * denotes a module which can be deviated by the modules in the Map value.
     *
     * @param modulesDeviatedByModules Map of YANG modules (Map key) which can be deviated by specified modules (Map
     *                                 value) in the final SchemaContext. If the map is empty, no deviations encountered
     *                                  will be supported.
     */
    YangParser setModulesWithSupportedDeviations(
            @NonNull SetMultimap<QNameModule, QNameModule> modulesDeviatedByModules);

    /**
     * Build the declared view of a combined view of declared statements.
     *
     * @return Ordered collection of declared statements from requested sources.
     * @throws YangSyntaxErrorException When a syntactic error is encountered.
     */
    List<DeclaredStatement<?>> buildDeclaredModel() throws YangParserException;

    /**
     * Build the effective view of a combined view of effective statements. Note that this representation, unlike
     * {@link #buildDeclaredModel()} does not expose submodules as top-level contracts. These are available from their
     * respective parent modules.
     *
     * @return Effective module statements indexed by their QNameModule.
     * @throws YangSyntaxErrorException When a syntactic error is encountered.
     */
    // FIXME: 3.0.0: Make this method non-default
    default Map<QNameModule, ModuleEffectiveStatement> buildEffectiveModel() throws YangParserException {
        throw new UnsupportedOperationException(getClass() + " does not implement buildEffectiveModel()");
    }

    /**
     * Build effective {@link SchemaContext}.
     *
     * @return An effective schema context comprised of configured models.
     * @throws YangSyntaxErrorException When a syntactic error is encountered.
     */
    SchemaContext buildSchemaContext() throws YangParserException;
}
