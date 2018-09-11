/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayDeque;
import java.util.Deque;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link NormalizedNodeStreamWriter} which logs the events into a {@link Logger}.
 */
@Beta
public final class LoggingNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingNormalizedNodeStreamWriter.class);
    private static final int DEFAULT_INDENT_SIZE = 2;

    private final Deque<String> indent = new ArrayDeque<>();
    private final String indentStr;

    public LoggingNormalizedNodeStreamWriter() {
        this(DEFAULT_INDENT_SIZE);
    }

    public LoggingNormalizedNodeStreamWriter(final int indentSize) {
        this.indentStr = Strings.repeat(" ", indentSize);
        indent.push("");
    }

    private String ind() {
        return indent.peek();
    }

    private void decIndent() {
        indent.pop();
    }

    private void incIndent() {
        indent.push(ind() + indentStr);
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}[](no key)", ind(), name);
        incIndent();
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(no key)", ind(), name);
        incIndent();
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) {

    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(key)", ind(), name);
        incIndent();
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint) {
        LOG.debug("{}{}[](key)", ind(), identifier);
        incIndent();
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(leaf-list)", ind(), name);
        incIndent();
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(leaf-list)", ind(), name);
        incIndent();
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(container)", ind(), name);
        incIndent();
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(choice)", ind(), name);
        incIndent();
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) {
        LOG.debug("{}{}(augmentation)", ind(), identifier);
        incIndent();
    }

    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void leafSetEntryNode(final QName name, final Object value) {
        LOG.debug("{}{}({}) ", ind(), value, value.getClass().getSimpleName());
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value) {
        if (value == null) {
            LOG.debug("{}{}(leaf(null))=null", ind(), name);
        } else {
            LOG.debug("{}{}(leaf({}))={}", ind(), name, value.getClass().getSimpleName(), value);
        }
    }

    @Override
    public void endNode() {
        decIndent();
        LOG.debug("{}(end)", ind());
    }

    @Override
    public void anyxmlNode(final NodeIdentifier name, final Object value) {
        LOG.debug("{}{}(anyxml)={}", ind(), name, value);
    }

    @Override
    public void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(yangModeledAnyXml)", ind(), name);
        incIndent();
    }

    @Override
    public void flush() {
        LOG.trace("<<FLUSH>>");
    }

    @Override
    public void close() {
        LOG.debug("<<END-OF-STREAM>>");
    }
}