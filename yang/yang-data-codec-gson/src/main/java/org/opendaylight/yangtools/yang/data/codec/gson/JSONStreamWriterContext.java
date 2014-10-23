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
    private final boolean mandatory;
    private final int indentetionLevel;

    //non integer type is used because type as list can consist of 3 levels (list, list entry, list key) 
    //which are transformed to one level. Therefore list can have depth 1.5 and list entry 2. If elements to
    //depth 1 should be written then list isn't there. If elements to level 2 then also list and list entry are 
    //written
    private final double currentDepth;
    private boolean emittedMyself = false;
    private boolean haveChild = false;

    /**
     * Construct a new context.
     *
     * @param parent Parent context, usually non-null.
     * @param mandatory Mandatory flag. If set to true, the corresponding node
     *                  will be emitted even if it has no children.
     */
    protected JSONStreamWriterContext(final JSONStreamWriterContext parent, final boolean mandatory, final double depth) {
        this.mandatory = mandatory;
        this.parent = parent;

        if (parent != null) {
            indentetionLevel = parent.indentetionLevel + 1;
        } else {
            indentetionLevel = 0;
        }
        this.currentDepth = depth;
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
        if (!ns.equals(getNamespace()) || this instanceof JSONStreamWriterRootContext) {
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
    protected abstract void emitEnd(final Writer writer) throws IOException;

    private final void emitMyself(final SchemaContext schema, final Writer writer, final String indent, final double maxDepth) throws IOException {
        if (!emittedMyself) {
            if (parent != null) {
                parent.emittingChild(schema, writer, indent, maxDepth);
            }
            if (Double.compare(currentDepth, maxDepth) <= 0) {
                emitStart(schema, writer);
            }
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
    final void emittingChild(final SchemaContext schema, final Writer writer, final String indent, final double maxDepth) throws IOException {
        emitMyself(schema, writer, indent, maxDepth);
        if (Double.compare(currentDepth+1, maxDepth) <= 0) {
            if (haveChild) {
                writer.append(',');
            }

            if (indent != null) {
                writer.append('\n');

                for (int i = 0; i < indentetionLevel; i++) {
                    writer.append(indent);
                }
            }
            haveChild = true;
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
    final JSONStreamWriterContext endNode(final SchemaContext schema, final Writer writer, final String indent, final double maxDepth) throws IOException {
        if (!emittedMyself && mandatory) {
            emitMyself(schema, writer, indent, maxDepth);
        }

        if (Double.compare(currentDepth, maxDepth) <= 0) {
            if (emittedMyself) {
                emitEnd(writer);
            }
        }
        return parent;
    }

    public double getDepth() {
        return currentDepth;
    }

    protected static double addOneDepthLevel(final double currentDepth) {
        final double ceilInteger = Math.ceil(currentDepth);
        if (Double.compare(currentDepth, ceilInteger) == 0) {
            return currentDepth + 1;
        }
        return ceilInteger;
    }

}
