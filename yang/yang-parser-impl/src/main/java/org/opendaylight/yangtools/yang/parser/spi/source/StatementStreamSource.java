/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

/**
 *
 * Statement stream source, which is used for inference of effective model.
 *
 * <p>
 * Statement stream source is required to emit its statements using supplied
 * {@link StatementWriter}.
 * </p>
 * <p>
 * Since YANG allows language extensions defined in sources (which defines how
 * source is serialized), instances of extensions present anywhere and forward
 * references, each source needs to be processed in three steps, where each step
 * uses different set of supported statements.
 * <p>
 * Steps (in order of invocation) are:
 *
 * <ol>
 * <li>{@link #writeLinkage(StatementWriter, QNameToStatementDefinition)} -
 * Source MUST emit only statements related in linkage, which are present in
 * supplied statement definition map. This step is used to build cross-source
 * linkage and visibility relationship, and to determine XMl namespaces and
 * prefixes.</li>
 * <li>
 * {@link #writeLinkageAndStatementDefinitions(StatementWriter, QNameToStatementDefinition, PrefixToModule)}
 * - Source MUST emit only statements related to linkage and language extensions
 * definitions, which are present in supplied statement definition map. This
 * step is used to build statement definitions in order to fully processed
 * source.</li>
 * <li>
 * {@link #writeFull(StatementWriter, QNameToStatementDefinition, PrefixToModule)}
 * - Source MUST emit all statements present in source. This step is used to
 * build full declared statement model of source.</li>
 * </ol>
 *
 */
public interface StatementStreamSource {

    /**
     *
     * Emits only linkage-related statements to supplied {@code writer}.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param stmtDef
     *            Map of available statement definitions. Only these statements
     *            may be written to statement writer, source MUST ignore and MUST NOT
     *            emit any other statements.
     *
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writeLinkage(StatementWriter writer, QNameToStatementDefinition stmtDef) throws SourceException;

    /**
     *
     * Emits only linkage and language extension statements to supplied
     * {@code writer}.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param stmtDef
     *            Map of available statement definitions. Only these statements
     *            may be written to statement writer, source MUST ignore and MUST NOT
     *            emit any other statements.
     * @param prefixes
     *            Map of source-specific prefixes to namespaces
     *
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writeLinkageAndStatementDefinitions(StatementWriter writer, QNameToStatementDefinition stmtDef, PrefixToModule prefixes) throws SourceException;

    /**
     *
     * Emits every statements present in this statement source to supplied
     * {@code writer}.
     *
     * @param writer
     *            {@link StatementWriter} which should be used to emit
     *            statements.
     * @param stmtDef
     *            Map of available statement definitions.
     * @param prefixes
     *            Map of source-specific prefixes to namespaces
     * @throws SourceException
     *             If source was is not valid, or provided statement writer
     *             failed to write statements.
     */
    void writeFull(StatementWriter writer,QNameToStatementDefinition stmtDef, PrefixToModule prefixes) throws SourceException;
}
