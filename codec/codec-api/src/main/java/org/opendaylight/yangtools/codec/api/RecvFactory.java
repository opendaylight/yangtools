/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.codec.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.DataSchemaCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A component capable of creating {@link Recv} objects.
 */
// FIXME: well: a stateful stream that can be navigated, most likely
// FIXME: the scope here is not to do complete normalization, but rather a RecvTree that can be navigated and contains
//        partially-resolved data
//        most notably: leaf nodes do not necessarily have their correct data in face of leafrefs escaping containment,
//                      such as 'PUT' into a datastore -- which is a later binding, as we need access to the receiving
//                      data tree to resolve them in unions
//                      All that is to say: RPC input may contain a 'type union' leaf whose parsing depends on a
//                      reference to operational datastore. An ever more compelling case is instance-idetifier with
//                      'require-instance true'
//        on the other hand
//        - 'type identityref' resolution is in scope
//        - 'type uint8' etc. lexical parsing and range validation is in scope
//        - 'type string' pattern validation is in scope
//        and finally
//        - parsed 'type string' is not the value that ultimately lands in LeafNode, as that needs further normalization
// FIXME: all of that is to say that we need eager/deferred normalization of both containers and values
@NonNullByDefault
public final class RecvFactory {
    private final EffectiveModelContext modelContext;

    private RecvFactory(final EffectiveModelContext modelContext) {
        this.modelContext = requireNonNull(modelContext);
    }

    public static RecvFactory of(final EffectiveModelContext modelContext) {
        return new RecvFactory(modelContext);
    }

    public Recv receiveInstantiated(final SchemaNodeIdentifier.Absolute path, final boolean configOnly)
            throws CodecException {
        final SchemaInferenceStack stack;
        try {
            stack = SchemaInferenceStack.of(modelContext, path);
        } catch (IllegalArgumentException e) {
            throw new CodecException("Failed to resolve " + path, e);
        }

        final var current = stack.currentStatement();
        if (configOnly) {
            if (!(current instanceof DataSchemaCompat<?, ?> compat)) {
                throw new VerifyException("Unexpected statement " + current);
            }
            if (!compat.toDataSchemaNode().effectiveConfig().orElseThrow()) {
                throw new CodecException("Attempted to access non-configuration " + path);
            }
        }

        return switch (current) {
            // TODO: having a stack ready is useful
            case AnydataEffectiveStatement stmt -> new RecvAnydata(stack, stmt);
            // FIXME: CodecException
            default -> throw new UnsupportedOperationException(
                "Unhandled statement " + current.statementDefinition().humanName());
        };
    }

    public Recv receiveNotification(final SchemaNodeIdentifier.Absolute path) throws CodecException {
        throw new UnsupportedOperationException();
    }

    public Recv receiveInput(final SchemaNodeIdentifier.Absolute operationPath) throws CodecException {
        throw new UnsupportedOperationException();
    }

    public Recv receiveOutput(final SchemaNodeIdentifier.Absolute operationPath) throws CodecException {
        throw new UnsupportedOperationException();
    }

    public RecvStructure receiveStructure(final QName name) throws CodecException {
        final var stack = SchemaInferenceStack.of(modelContext);
        // FIXME: validate:
        //        try {
        //            stack.enterYangData(name);
        //        } catch (IllegalArgumentException e) {
        //            throw new CodecException("Failed to resolve" + name, e);
        //        }
        return new RecvStructure(stack, null);
    }

    public RecvYangData receiveYangData(final YangDataName name) throws CodecException {
        final var stack = SchemaInferenceStack.of(modelContext);
        final YangDataEffectiveStatement stmt;
        try {
            stmt = stack.enterYangData(name);
        } catch (IllegalArgumentException e) {
            throw new CodecException("Failed to resolve " + name, e);
        }
        return new RecvYangData(stack, stmt);
    }
}
