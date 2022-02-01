/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
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

    private final StringBuilder content = new StringBuilder();

    public LoggingNormalizedNodeStreamWriter() {
        this(DEFAULT_INDENT_SIZE);
    }

    public LoggingNormalizedNodeStreamWriter(final int indentSize) {
        this.indentStr = " ".repeat(indentSize);
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
        content.append(String.format("%s%s[](no key)", ind(), name));
        content.append("\n");
        incIndent();
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(no key)", ind(), name);
        content.append(String.format("%s%s(no key)", ind(), name));
        content.append("\n");
        incIndent();
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) {
        startMapNode(name, childSizeHint);
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(key)", ind(), name);
        content.append(String.format("%s%s(key)", ind(), name));
        content.append("\n");
        incIndent();
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint) {
        LOG.debug("{}{}[](key)", ind(), identifier);
        content.append(String.format("%s%s[](key)", ind(), identifier));
        content.append("\n");
        incIndent();
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(leaf-list)", ind(), name);
        content.append(String.format("%s%s(leaf-list)", ind(), name));
        content.append("\n");
        incIndent();
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) {
        startLeafSet(name, childSizeHint);
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(container)", ind(), name);
        content.append(String.format("%s%s(container)", ind(), name));
        content.append("\n");
        incIndent();
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        LOG.debug("{}{}(choice)", ind(), name);
        content.append(String.format("%s%s(choice)", ind(), name));
        content.append("\n");
        incIndent();
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) {
        LOG.debug("{}{}(augmentation)", ind(), identifier);
        content.append(String.format("%s%s(augmentation)", ind(), identifier));
        content.append("\n");
        incIndent();
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) {
        LOG.debug("{}{}(entry}", ind(), name.getNodeType());
        content.append(String.format("%s%s(entry)", ind(), name.getNodeType()));
        content.append("\n");
        incIndent();
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) {
        LOG.debug("{}{}(leaf)", ind(), name);
        content.append(String.format("%s%s(leaf)", ind(), name));
        content.append("\n");
        incIndent();
    }

    @Override
    public void endNode() {
        decIndent();
        LOG.debug("{}(end)", ind());
        content.append(String.format("%s(end)", ind()));
        content.append("\n");
    }

    @Override
    public boolean startAnyxmlNode(final NodeIdentifier name, final Class<?> objectModel) {
        LOG.debug("{}{}(anyxml)", ind(), name);
        content.append(String.format("%s%s(anyxml)", ind(), name));
        content.append("\n");
        incIndent();
        return true;
    }

    @Override
    public boolean startAnydataNode(final NodeIdentifier name, final Class<?> objectModel) {
        LOG.debug("{}{}(anydata)", ind(), name);
        content.append(String.format("%s%s(anydata)", ind(), name));
        content.append("\n");
        incIndent();
        return true;
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
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void scalarValue(final Object value) {
        LOG.debug("{}({})={}", ind(), requireNonNull(value).getClass().getSimpleName(), value);
        content.append(String.format("%s(%s)=%s", ind(), requireNonNull(value).getClass().getSimpleName(), value));
        content.append("\n");
    }

    @Override
    public void domSourceValue(final DOMSource value) {
        scalarValue(value);
    }

    public String getContent() {
        return content.toString();
    }
}
