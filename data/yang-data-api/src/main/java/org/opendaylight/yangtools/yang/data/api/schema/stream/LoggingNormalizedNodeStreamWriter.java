/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link NormalizedNodeStreamWriter} which logs the events into a {@link Logger}.
 */
@Beta
public final class LoggingNormalizedNodeStreamWriter extends AbstractIndentingNormalizedNodeStreamWriter {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingNormalizedNodeStreamWriter.class);

    public LoggingNormalizedNodeStreamWriter() {
        // Default constructor
    }

    public LoggingNormalizedNodeStreamWriter(final int indentSize) {
        super(indentSize);
    }

    @Override
    public void flush() {
        LOG.trace("<<FLUSH>>");
    }

    @Override
    public void close() {
        LOG.debug("<<END-OF-STREAM>>");
    }

    @Override
    void enterUnkeyedListItem(final NodeIdentifier name, final String indent) {
        LOG.debug("{}{}[](no key)", indent, name);
    }

    @Override
    void enterUnkeyedList(final NodeIdentifier name, final String indent) {
        LOG.debug("{}{}(no key)", indent, name);
    }

    @Override
    void enterMapNode(final NodeIdentifier name, final String indent) {
        LOG.debug("{}{}(key)", indent, name);
    }

    @Override
    void enterMapEntryNode(final NodeIdentifierWithPredicates identifier, final String indent) {
        LOG.debug("{}{}[](key)", indent, identifier);
    }

    @Override
    void enterLeafSet(final NodeIdentifier name, final String indent) {
        LOG.debug("{}{}(leaf-list)", indent, name);
    }

    @Override
    void enterContainerNode(final NodeIdentifier name, final String indent) {
        LOG.debug("{}{}(container)", indent, name);
    }

    @Override
    void enterChoiceNode(final NodeIdentifier name, final String indent) {
        LOG.debug("{}{}(choice)", indent, name);
    }

    @Override
    void enterLeafSetEntryNode(final NodeWithValue<?> name, final String indent) {
        LOG.debug("{}{}(entry}", indent, name.getNodeType());
    }

    @Override
    void enterLeafNode(final NodeIdentifier name, final String indent) {
        LOG.debug("{}{}(leaf)", indent, name);
    }

    @Override
    void enterAnyxmlNode(final NodeIdentifier name, final String indent) {
        LOG.debug("{}{}(anyxml)", indent, name);
    }

    @Override
    void enterAnydataNode(final NodeIdentifier name, final String indent) {
        LOG.debug("{}{}(anydata)", indent, name);
    }

    @Override
    void exitNode(final String indent) {
        LOG.debug("{}(end)", indent);
    }

    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    void scalarValue(final Object value, final String indent) {
        LOG.debug("{}({})={}", indent, value.getClass().getSimpleName(), value);
    }
}
