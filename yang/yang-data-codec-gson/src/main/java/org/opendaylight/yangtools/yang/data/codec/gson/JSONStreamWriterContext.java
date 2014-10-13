/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Abstract base class for a single level of {@link JSONNormalizedNodeStreamWriter}
 * recursion. Provides the base API towards the writer, which is then specialized
 * by subclasses.
 */
abstract class JSONStreamWriterContext {
    private final JSONStreamWriterContext parent;
    private JSONStreamWriterContext child = null;
    private final boolean mandatory;
    private final int indentLevel;
    private boolean emittedMyself = false;
    private boolean haveChild = false;

    /**
     * Construct a new context.
     *
     * @param parent Parent context, usually non-null.
     * @param mandatory Mandatory flag. If set to true, the corresponding node
     *                  will be emitted even if it has no children.
     */
    protected JSONStreamWriterContext(final JSONStreamWriterContext parent, final boolean mandatory) {
        this.mandatory = mandatory;
        this.parent = parent;

        if (parent != null) {
            indentLevel = parent.indentLevel + 1;
            parent.setChild(this);
        } else {
            indentLevel = 0;
        }
    }

    /**
     * Construct a new context with indentLevel specified
     * @param parent Parent context, usually non-null.
     * @param mandatory Mandatory flag. If set to true, the corresponding node
     *                  will be emitted even if it has no children.
     * @param indentLevel indentation level
     */
    protected JSONStreamWriterContext(final JSONStreamWriterContext parent, final boolean mandatory, final int indentLevel) {
        this.mandatory = mandatory;
        this.parent = parent;
        this.indentLevel = indentLevel;
    }

    /**
     * Write a child JSON node identifier, optionally prefixing it with the module name
     * corresponding to its namespace.
     *
     * @param schema Schema context
     * @param writer Output writer
     * @param qname Namespace/name tuple
     * @throws IOException when the writer reports it
     */
    final void writeChildJsonIdentifier(final SchemaContext schema, final Writer writer, final QName qname) throws IOException {
        writer.append('"');

        // Prepend module name if namespaces do not match
        final URI ns = qname.getNamespace();
        if (!ns.equals(getNamespace())) {
            final Module module = schema.findModuleByNamespaceAndRevision(ns, null);
            Preconditions.checkArgument(module != null, "Could not find module for namespace {}", ns);

            writer.append(module.getName());
            writer.append(':');
        }

        writer.append(qname.getLocalName());
        writer.append("\":");
    }

    /**
     * Write our JSON node identifier, optionally prefixing it with the module name
     * corresponding to its namespace.
     *
     * @param schema Schema context
     * @param writer Output writer
     * @param qname Namespace/name tuple
     * @throws IOException when the writer reports it
     */
    protected final void writeMyJsonIdentifier(final SchemaContext schema, final Writer writer, final QName qname) throws IOException {
        parent.writeChildJsonIdentifier(schema, writer, qname);
    }

    /**
     * Return the namespace associated with current node.
     *
     * @return Namespace as URI
     */
    protected abstract @Nonnull URI getNamespace();

    /**
     * Emit the start of an element.
     *
     * @param schema Schema context
     * @param writer Output writer
     * @throws IOException
     */
    protected abstract void emitStart(final SchemaContext schema, final Writer writer) throws IOException;

    /**
     * Emit the end of an element.
     *
     * @param schema Schema context
     * @param writer Output writer
     * @throws IOException
     */
    protected void emitEnd(final Writer writer, final String indent) throws IOException {
        if (indent != null) {
            writer.append('\n');
            for (int i=0; i<indentLevel; i++) {
                writer.append(indent);
            }
        }
    }

    private final void emitMyself(final SchemaContext schema, final Writer writer, final String indent) throws IOException {
        if (!emittedMyself) {
            if (parent != null) {
                parent.emittingChild(schema, writer, indent);
            }

            emitStart(schema, writer);
            emittedMyself = true;
        }
    }

    /**
     * Invoked whenever a child node is being emitted. Checks whether this node has
     * been emitted, and takes care of that if necessary. Also makes sure separator
     * is emitted before a second and subsequent child.
     *
     * @param schema Schema context
     * @param writer Output writer
     * @param indent Indentation string
     * @throws IOException when writer reports it
     */
    final void emittingChild(final SchemaContext schema, final Writer writer, final String indent) throws IOException {
        emitMyself(schema, writer, indent);
        if (haveChild) {
            writer.append(',');
        }

        writeWhiteSpaces(writer, indent);

        haveChild = true;
    }

    protected void writeWhiteSpaces(final Writer writer, final String indent/*, final boolean forSimpleNode*/) throws IOException {
        if (indent != null) {
            writer.append('\n');
            for (int i = 0; i <= indentLevel; i++) {
                writer.append(indent);
            }
        }
    }

    /**
     * Invoked by the writer when it is leaving this node. Checks whether this node
     * needs to be emitted and takes of that if necessary.
     *
     * @param schema Schema context
     * @param writer Output writer
     * @param indent Indentation string
     * @return Parent node context
     * @throws IOException when writer reports it
     * @throws IllegalArgumentException if this node cannot be ended (e.g. root)
     */
    final JSONStreamWriterContext endNode(final SchemaContext schema, final Writer writer, final String indent) throws IOException {
        if (!emittedMyself && mandatory) {
            emitMyself(schema, writer, indent);
        }

        if (emittedMyself) {
            emitEnd(writer, indent);
        }
        return parent;
    }

    protected int getIndentLevel() {
        return indentLevel;
    }

    private void setChild(final JSONStreamWriterContext jsonStreamWriterContext) {
        this.child = jsonStreamWriterContext;
    }

    protected boolean hasChildContext() {
        return child != null;
    }
}
