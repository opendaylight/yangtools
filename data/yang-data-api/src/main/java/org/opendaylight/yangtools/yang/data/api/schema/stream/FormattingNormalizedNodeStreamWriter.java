/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;

/**
 * A {@link NormalizedNodeStreamWriter} which formats events into a String, available via #result().
 */
@Beta
public final class FormattingNormalizedNodeStreamWriter extends AbstractIndentingNormalizedNodeStreamWriter {
    private final StringBuilder sb = new StringBuilder();

    private boolean closed;

    public FormattingNormalizedNodeStreamWriter() {
        // Default constructor
    }

    public FormattingNormalizedNodeStreamWriter(final int indentSize) {
        super(indentSize);
    }

    /**
     * Return the formatted String result capturing the events which have been streamed into this writer.
     *
     * @return Formatted string
     * @throws IllegalStateException if this writer was not {@link #close()}d
     */
    public @NonNull String result() {
        checkState(closed, "Attempted to access the result of unclosed writer");
        return sb.toString();
    }

    @Override
    public void flush() {
        // No-op
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    void enterUnkeyedListItem(final NodeIdentifier name, final String indent) {
        sb.append(indent).append(name).append("[](no key)\n");
    }

    @Override
    void enterUnkeyedList(final NodeIdentifier name, final String indent) {
        sb.append(indent).append(name).append("(no key)\n");
    }

    @Override
    void enterMapNode(final NodeIdentifier name, final String indent) {
        sb.append(indent).append(name).append("(key)\n");
    }

    @Override
    void enterMapEntryNode(final NodeIdentifierWithPredicates identifier, final String indent) {
        sb.append(indent).append(identifier).append("[](key)\n");
    }

    @Override
    void enterLeafSet(final NodeIdentifier name, final String indent) {
        sb.append(indent).append(name).append("(leaf-list)\n");
    }

    @Override
    void enterContainerNode(final NodeIdentifier name, final String indent) {
        sb.append(indent).append(name).append("(container)\n");
    }

    @Override
    void enterChoiceNode(final NodeIdentifier name, final String indent) {
        sb.append(indent).append(name).append("(choice)\n");
    }

    @Override
    void enterLeafSetEntryNode(final NodeWithValue<?> name, final String indent) {
        sb.append(indent).append(name.getNodeType()).append("(entry)\n");
    }

    @Override
    void enterLeafNode(final NodeIdentifier name, final String indent) {
        sb.append(indent).append(name).append("(leaf)\n");
    }

    @Override
    void enterAnyxmlNode(final NodeIdentifier name, final String indent) {
        sb.append(indent).append(name).append("(anyxml)\n");
    }

    @Override
    void enterAnydataNode(final NodeIdentifier name, final String indent) {
        sb.append(indent).append(name).append("(anydata)\n");
    }

    @Override
    void exitNode(final String indent) {
        sb.append(indent).append("(end)\n");
    }

    @Override
    void scalarValue(final Object value, final String indent) {
        sb.append(indent).append('(').append(value.getClass().getSimpleName()).append(")=").append(value).append('\n');
    }
}
