package org.opendaylight.yangtools.yang.parser.spi.source;


public interface StatementStreamSource {

    /**
     *
     *
     * @param writer
     * @param stmtDef
     */
    void writeLinkage(StatementWriter writer, QNameToStatementDefinition stmtDef) throws SourceException;

    /**
     *
     * @param writer
     * @param prefixes
     * @param stmtDef
     */
    void writeLinkageAndStatementDefinitions(StatementWriter writer, QNameToStatementDefinition stmtDef, PrefixToModule prefixes) throws SourceException;

    /**
     *
     * @param writer
     * @param prefixes
     * @param stmtDef
     */
    void writeFull(StatementWriter writer,QNameToStatementDefinition stmtDef, PrefixToModule prefixes) throws SourceException;
}
