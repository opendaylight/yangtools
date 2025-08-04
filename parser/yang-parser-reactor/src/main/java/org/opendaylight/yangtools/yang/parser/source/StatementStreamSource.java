/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixResolver;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Statement stream source, which is used for inference of effective model.
 *
 * <p>Statement stream source is required to emit its statements using supplied {@link StatementWriter}.
 *
 * <p>Since YANG allows language extensions defined in sources (which defines how source is serialized), instances of
 * extensions present anywhere and forward references, each source needs to be processed in three steps, where each step
 * uses different set of supported statements.
 *
 * <p>Steps (in order of invocation) are:
 * <ol>
 * <li>
 * {@link #writeRoot(StatementWriter writer, StatementDefinitionResolver resolver)} -
 * Source MUST emit only root statement - MODULE, or SUBMODULE.
 * This step is used to prepare root statements for the Linkage Resolution process done
 * by {@code SourceLinkageResolver} and before the next step.</li>
 * <li>
 * {@link #writeLinkageAndStatementDefinitions(StatementWriter, StatementDefinitionResolver, PrefixResolver)}
 * - Source MUST emit only statements related to linkage and language extensions
 * definitions, which are present in supplied statement definition map. This
 * step is used to build statement definitions in order to fully processed
 * source.</li>
 * <li>
 * {@link #writeFull(StatementWriter, StatementDefinitionResolver, PrefixResolver)}
 * - Source MUST emit all statements present in source. This step is used to
 * build full declared statement model of source.</li>
 * </ol>
 */
// FIXME: 7.0.0: this is a push parser, essentially traversing the same tree multiple times. Perhaps we should create
//               a visitor/filter or perform some explicit argument binding?
public sealed interface StatementStreamSource permits YangIRStatementStreamSource, YinDOMStatementStreamSource {
    /**
     * A factory for {@link StatementStreamSource}s.
     *
     * @param <S> the type of {@link SourceRepresentation}
     */
    @NonNullByDefault
    @FunctionalInterface
    interface Factory<S extends SourceRepresentation & SourceInfo.Extractor> {
        /**
         * {@return a new {@link StatementStreamSource} backed by specified source and version}
         * @param source the source
         * @param yangVersion the version
         */
        StatementStreamSource newStreamSource(S source, YangVersion yangVersion);
    }

    /**
     * The {@link Factory} for {@link YangIRSource}.
     */
    @NonNullByDefault
    static Factory<YangIRSource> forYangIR() {
        return YangIRStatementStreamSource.FACTORY;
    }

    /**
     * The {@link Factory} for {@link YinDOMSource}.
     */
    @NonNullByDefault
    static Factory<YinDOMSource> forYInDOM() {
        return YinDOMStatementStreamSource.FACTORY;
    }

    /**
     * Creates only the Root statement via the supplied {@link StatementWriter}.
     *
     * @param writer
     *              {@link StatementWriter} which should be used to create the Root statement.
     * @param resolver
     *              Map of available statement definitions. The only necessary definitions here are MODULE and
     *              SUBMODULE.
     */
    void writeRoot(StatementWriter writer, StatementDefinitionResolver resolver);

    /**
     * Emits only linkage and language extension statements to supplied
     * {@code writer} based on specified YANG version. Default implementation
     * does not make any differences between versions.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param resolver
     *            Map of available statement definitions. Only these statements
     *            may be written to statement writer, source MUST ignore and
     *            MUST NOT emit any other statements.
     * @param prefixes
     *            Map of source-specific prefixes to namespaces
     *
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writeLinkageAndStatementDefinitions(StatementWriter writer, StatementDefinitionResolver resolver,
        PrefixResolver prefixes);

    /**
     * Emits every statements present in this statement source to supplied
     * {@code writer} based on specified yang version. Default implementation
     * does not make any differences between versions.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param resolver
     *            Map of available statement definitions.
     * @param prefixes
     *            Map of source-specific prefixes to namespaces
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writeFull(StatementWriter writer, StatementDefinitionResolver resolver, PrefixResolver prefixes);
}
