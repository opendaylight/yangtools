/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;

/**
 * Statement stream source, which is used for inference of effective model.
 *
 * <p>
 * Statement stream source is required to emit its statements using supplied {@link StatementWriter}.
 *
 * <p>
 * Since YANG allows language extensions defined in sources (which defines how source is serialized), instances of
 * extensions present anywhere and forward references, each source needs to be processed in three steps, where each step
 * uses different set of supported statements.
 *
 * <p>
 * Steps (in order of invocation) are:
 * <ol>
 * <li>{@link #writePreLinkage(StatementWriter, QNameToStatementDefinition)} -
 * Source MUST emit only statements related in pre-linkage, which are present in
 * supplied statement definition map. This step is used as preparatory cross-source
 * relationship resolution phase which collects available module names and namespaces.
 * It is necessary in order to correct resolution of unknown statements used by linkage
 * phase (e.g. semantic version of yang modules).
 * </li>
 * <li>{@link #writeLinkage(StatementWriter, QNameToStatementDefinition, PrefixResolver, YangVersion)} -
 * Source MUST emit only statements related in linkage, which are present in
 * supplied statement definition map. This step is used to build cross-source
 * linkage and visibility relationship, and to determine XMl namespaces and
 * prefixes.</li>
 * <li>
 * {@link #writeLinkageAndStatementDefinitions(StatementWriter, QNameToStatementDefinition, PrefixResolver,
 * YangVersion)}
 * - Source MUST emit only statements related to linkage and language extensions
 * definitions, which are present in supplied statement definition map. This
 * step is used to build statement definitions in order to fully processed
 * source.</li>
 * <li>
 * {@link #writeFull(StatementWriter, QNameToStatementDefinition, PrefixResolver, YangVersion)}
 * - Source MUST emit all statements present in source. This step is used to
 * build full declared statement model of source.</li>
 * </ol>
 */
// FIXME: 7.0.0: this is a push parser, essentially traversing the same tree multiple times. Perhaps we should create
//               a visitor/filter or perform some explicit argument binding?
public interface StatementStreamSource extends Identifiable<SourceIdentifier> {
    /**
     * Emits only pre-linkage-related statements to supplied {@code writer}.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param stmtDef
     *            Map of available statement definitions. Only these statements
     *            may be written to statement writer, source MUST ignore and MUST NOT
     *            emit any other statements.
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writePreLinkage(StatementWriter writer, QNameToStatementDefinition stmtDef);

    /**
     * Emits only linkage-related statements to supplied {@code writer} based on specified YANG version.
     * Default implementation does not make any differences between versions.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param stmtDef
     *            Map of available statement definitions. Only these statements
     *            may be written to statement writer, source MUST ignore and
     *            MUST NOT emit any other statements.
     * @param preLinkagePrefixes
     *            Pre-linkage map of source-specific prefixes to namespaces
     * @param yangVersion
     *            yang version.
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writeLinkage(StatementWriter writer, QNameToStatementDefinition stmtDef,
        PrefixResolver preLinkagePrefixes, YangVersion yangVersion);

    /**
     * Emits only linkage and language extension statements to supplied
     * {@code writer} based on specified YANG version. Default implementation
     * does not make any differences between versions.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param stmtDef
     *            Map of available statement definitions. Only these statements
     *            may be written to statement writer, source MUST ignore and
     *            MUST NOT emit any other statements.
     * @param prefixes
     *            Map of source-specific prefixes to namespaces
     * @param yangVersion
     *            YANG version.
     *
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writeLinkageAndStatementDefinitions(StatementWriter writer, QNameToStatementDefinition stmtDef,
        PrefixResolver prefixes, YangVersion yangVersion);

    /**
     * Emits every statements present in this statement source to supplied
     * {@code writer} based on specified yang version. Default implementation
     * does not make any differences between versions.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param stmtDef
     *            Map of available statement definitions.
     * @param prefixes
     *            Map of source-specific prefixes to namespaces
     * @param yangVersion
     *            yang version.
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writeFull(StatementWriter writer, QNameToStatementDefinition stmtDef, PrefixResolver prefixes,
        YangVersion yangVersion);
}
