/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;

/**
 * A {@link NormalizedNodeStreamWriter} which maintains some amount of indentation.
 */
abstract class AbstractIndentingNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {
    private static final int DEFAULT_INDENT_SIZE = 2;

    private final Deque<String> indent = new ArrayDeque<>();
    private final String indentStr;

    AbstractIndentingNormalizedNodeStreamWriter() {
        this(DEFAULT_INDENT_SIZE);
    }

    AbstractIndentingNormalizedNodeStreamWriter(final int indentSize) {
        indentStr = " ".repeat(indentSize);
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
    public final void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) {
        enterUnkeyedListItem(name, ind());
        incIndent();
    }

    abstract void enterUnkeyedListItem(NodeIdentifier name, String indent);

    @Override
    public final void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) {
        enterUnkeyedList(name, ind());
        incIndent();
    }

    abstract void enterUnkeyedList(NodeIdentifier name, String indent);

    @Override
    public final void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) {
        startMapNode(name, childSizeHint);
    }

    @Override
    public final void startMapNode(final NodeIdentifier name, final int childSizeHint) {
        enterMapNode(name, ind());
        incIndent();
    }

    abstract void enterMapNode(NodeIdentifier name, String indent);

    @Override
    public final void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint) {
        enterMapEntryNode(identifier, ind());
        incIndent();
    }

    abstract void enterMapEntryNode(NodeIdentifierWithPredicates identifier, String indent);

    @Override
    public final void startLeafSet(final NodeIdentifier name, final int childSizeHint) {
        enterLeafSet(name, ind());
        incIndent();
    }

    abstract void enterLeafSet(NodeIdentifier name, String indent);

    @Override
    public final void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) {
        startLeafSet(name, childSizeHint);
    }

    @Override
    public final void startContainerNode(final NodeIdentifier name, final int childSizeHint) {
        enterContainerNode(name, ind());
        incIndent();
    }

    abstract void enterContainerNode(NodeIdentifier name, String indent);

    @Override
    public final void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        enterChoiceNode(name, ind());
        incIndent();
    }

    abstract void enterChoiceNode(NodeIdentifier name, String indent);

    @Override
    public final void startLeafSetEntryNode(final NodeWithValue<?> name) {
        enterLeafSetEntryNode(name, ind());
        incIndent();
    }

    abstract void enterLeafSetEntryNode(NodeWithValue<?> name, String indent);

    @Override
    public final void startLeafNode(final NodeIdentifier name) {
        enterLeafNode(name, ind());
        incIndent();
    }

    abstract void enterLeafNode(NodeIdentifier name, String indent);

    @Override
    public final boolean startAnyxmlNode(final NodeIdentifier name, final Class<?> objectModel) {
        enterAnyxmlNode(name, ind());
        incIndent();
        return true;
    }

    abstract void enterAnyxmlNode(NodeIdentifier name, String indent);

    @Override
    public final boolean startAnydataNode(final NodeIdentifier name, final Class<?> objectModel) {
        enterAnydataNode(name, ind());
        incIndent();
        return true;
    }

    abstract void enterAnydataNode(NodeIdentifier name, String indent);

    @Override
    public final void endNode() {
        decIndent();
        exitNode(ind());
    }

    abstract void exitNode(String indent);

    @Override
    public final void scalarValue(final Object value) {
        scalarValue(value, ind());
    }

    abstract void scalarValue(@NonNull Object value, String indent);

    @Override
    public final void domSourceValue(final DOMSource value) {
        scalarValue(requireNonNull(value));
    }
}
