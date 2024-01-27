/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * Abstract base class for a single level of {@link JSONNormalizedNodeStreamWriter} recursion. Provides the base API
 * towards the writer, which is then specialized by subclasses.
 */
abstract class JSONStreamWriterContext {
    private final JSONStreamWriterContext parent;
    private final boolean mandatory;
    private final int depth;

    private boolean emittedMyself;
    private boolean inChild;

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
            depth = parent.depth + 1;
        } else {
            depth = 0;
        }
    }

    /**
     * Write a child JSON node identifier, optionally prefixing it with the module name corresponding to its namespace.
     *
     * @param schema Schema context
     * @param writer Output writer
     * @param qname Namespace/name tuple
     * @throws IOException when the writer reports it
     */
    final void writeChildJsonIdentifier(final EffectiveModelContext schema, final JsonWriter writer, final QName qname)
            throws IOException {

        final StringBuilder sb = new StringBuilder();
        // Prepend module name if namespaces do not match
        final QNameModule module = qname.getModule();
        if (!module.namespace().equals(getNamespace())) {
            final var name = schema.findModuleStatement(module)
                .map(mod -> mod.argument().getLocalName())
                .orElseThrow(() -> new IllegalArgumentException("Could not find module for namespace " + module));
            sb.append(name).append(':');
        }
        sb.append(qname.getLocalName());

        writer.name(sb.toString());
    }

    /**
     * Write our JSON node identifier, optionally prefixing it with the module name corresponding to its namespace.
     *
     * @param schema Schema context
     * @param writer Output writer
     * @param qname Namespace/name tuple
     * @throws IOException when the writer reports it
     */
    protected final void writeMyJsonIdentifier(final EffectiveModelContext schema, final JsonWriter writer,
            final QName qname) throws IOException {
        parent.writeChildJsonIdentifier(schema, writer, qname);
    }

    /**
     * Return the namespace associated with current node.
     *
     * @return Namespace as URI
     */
    protected abstract @NonNull XMLNamespace getNamespace();

    /**
     * Emit the start of an element.
     *
     * @param schema Schema context
     * @param writer Output writer
     * @throws IOException when the writer reports it
     */
    protected abstract void emitStart(EffectiveModelContext schema, JsonWriter writer) throws IOException;

    /**
     * Emit the end of an element.
     *
     * @param schema Schema context
     * @param writer Output writer
     * @throws IOException when writer reports it
     */
    protected abstract void emitEnd(JsonWriter writer) throws IOException;

    private void emitMyself(final EffectiveModelContext schema, final JsonWriter writer) throws IOException {
        if (!emittedMyself) {
            if (parent != null) {
                parent.emitMyself(schema, writer);
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
     * @throws IOException when writer reports it
     */
    final void emittingChild(final EffectiveModelContext schema, final JsonWriter writer) throws IOException {
        checkState(!inChild, "Duplicate child encountered");
        emitMyself(schema, writer);
        inChild = true;
    }

    /**
     * Invoked by the writer when it is leaving this node. Checks whether this node
     * needs to be emitted and takes of that if necessary.
     *
     * @param schema Schema context
     * @param writer Output writer
     * @return Parent node context
     * @throws IOException when writer reports it
     * @throws IllegalArgumentException if this node cannot be ended (e.g. root)
     */
    final JSONStreamWriterContext endNode(final EffectiveModelContext schema, final JsonWriter writer)
            throws IOException {
        if (inChild) {
            inChild = false;
            return this;
        }
        if (!emittedMyself && mandatory) {
            emitMyself(schema, writer);
        }
        if (emittedMyself) {
            emitEnd(writer);
        }
        return parent;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper;
    }
}
