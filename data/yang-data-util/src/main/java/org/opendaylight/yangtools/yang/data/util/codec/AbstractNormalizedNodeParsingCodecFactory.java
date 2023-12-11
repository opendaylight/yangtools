/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Qualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.codec.YangInvalidValueException;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;

/**
 * An {@link AbstractCodecFactory} which additionally provides services defined in {@link NormalizedNodeParser}.
 */
public abstract class AbstractNormalizedNodeParsingCodecFactory<T extends TypeAwareCodec<?, ?, ?>>
        extends AbstractCodecFactory<T> implements NormalizedNodeParser {
    protected AbstractNormalizedNodeParsingCodecFactory(final @NonNull EffectiveModelContext schemaContext,
            final @NonNull CodecCache<T> cache) {
        super(schemaContext, cache);
    }

    @Override
    public final ContainerNode parseDatastore(final XMLNamespace rootNamespace, final Qualified rootName,
            final InputStream stream) throws NormalizedNodeParserException {
        final NormalizedNode data;
        try {
            data = parseDatastoreImpl(requireNonNull(rootNamespace), requireNonNull(rootName), requireNonNull(stream));
        } catch (IOException e) {
            throw new NormalizedNodeParserException(ErrorType.PROTOCOL, ErrorTag.MALFORMED_MESSAGE, e.getMessage(), e);
        } catch (YangInvalidValueException e) {
            throw new NormalizedNodeParserException(e.getNetconfErrors(), e);
        }
        return checkNodeContainer(data);
    }

    protected abstract @NonNull NormalizedNode parseDatastoreImpl(@NonNull XMLNamespace rootNamespace,
        @NonNull Qualified rootName, @NonNull InputStream stream) throws IOException, NormalizedNodeParserException;

    @Override
    public final NormalizedNode parseData(final EffectiveStatementInference inference, final InputStream stream)
            throws NormalizedNodeParserException {
        final var stack = checkInferenceNotEmpty(inference);
        final var stmt = stack.currentStatement();
        if (!(stmt instanceof DataTreeEffectiveStatement<?> dataStmt)) {
            throw new IllegalArgumentException("Invalid inference statement " + stmt);
        }

        final NormalizedNode data;
        try {
            data = parseDataImpl(stack, requireNonNull(stream));
        } catch (IOException e) {
            throw new NormalizedNodeParserException(ErrorType.PROTOCOL, ErrorTag.MALFORMED_MESSAGE, e.getMessage(), e);
        } catch (YangInvalidValueException e) {
            throw new NormalizedNodeParserException(e.getNetconfErrors(), e);
        }
        return checkNodeName(data, dataStmt.argument());
    }

    protected abstract @NonNull NormalizedNode parseDataImpl(@NonNull SchemaInferenceStack stack,
        @NonNull InputStream stream) throws IOException, NormalizedNodeParserException;

    @Override
    public final SuffixAndData parseChildData(final EffectiveStatementInference inference, final InputStream stream)
            throws NormalizedNodeParserException {
        checkInference(inference);

        final NormalizedNode data;
        try {
            data = parseChildDataImpl(inference, requireNonNull(stream));
        } catch (IOException e) {
            throw new NormalizedNodeParserException(ErrorType.PROTOCOL, ErrorTag.MALFORMED_MESSAGE, e.getMessage(), e);
        } catch (YangInvalidValueException e) {
            throw new NormalizedNodeParserException(e.getNetconfErrors(), e);
        }

        final var suffix = new ArrayList<@NonNull PathArgument>();
        var result = data;

        while (result instanceof ChoiceNode choice) {
            final var childNode = choice.body().iterator().next();
            suffix.add(result.name());
            result = childNode;
        }

        final var resultName = result.name();
        if (result instanceof MapEntryNode) {
            suffix.add(new NodeIdentifier(resultName.getNodeType()));
        }
        suffix.add(resultName);

        return new SuffixAndData(suffix, result);
    }

    protected abstract @NonNull NormalizedNode parseChildDataImpl(@NonNull EffectiveStatementInference inference,
            @NonNull InputStream stream) throws IOException, NormalizedNodeParserException;

    @Override
    public final ContainerNode parseInput(final EffectiveStatementInference inference, final InputStream stream)
            throws NormalizedNodeParserException {
        final var stack = checkInferenceNotEmpty(inference);
        final var stmt = stack.currentStatement();
        final QName expected;
        if (stmt instanceof RpcEffectiveStatement rpc) {
            expected = rpc.input().argument();
        } else if (stmt instanceof ActionEffectiveStatement action) {
            expected = action.input().argument();
        } else {
            throw new IllegalArgumentException("Invalid inference statement " + stmt);
        }
        return parseInputOutput(stack, expected, requireNonNull(stream));
    }

    @Override
    public final ContainerNode parseOutput(final EffectiveStatementInference inference, final InputStream stream)
            throws NormalizedNodeParserException {
        final var stack = checkInferenceNotEmpty(inference);
        final var stmt = stack.currentStatement();
        final QName expected;
        if (stmt instanceof RpcEffectiveStatement rpc) {
            expected = rpc.output().argument();
        } else if (stmt instanceof ActionEffectiveStatement action) {
            expected = action.output().argument();
        } else {
            throw new IllegalArgumentException("Invalid inference statement " + stmt);
        }
        return parseInputOutput(stack, expected, requireNonNull(stream));
    }

    private @NonNull ContainerNode parseInputOutput(final @NonNull SchemaInferenceStack stack,
            final @NonNull QName expected, final @NonNull InputStream stream) throws NormalizedNodeParserException {
        final NormalizedNode data;
        try {
            data = parseInputOutput(stack.toInference(), stream);
        } catch (IOException e) {
            throw new NormalizedNodeParserException(ErrorType.PROTOCOL, ErrorTag.MALFORMED_MESSAGE, e.getMessage(), e);
        } catch (YangInvalidValueException e) {
            throw new NormalizedNodeParserException(e.getNetconfErrors(), e);
        }
        return checkNodeContainer(checkNodeName(data, expected));
    }

    protected abstract @NonNull NormalizedNode parseInputOutput(@NonNull Inference inference, InputStream stream)
            throws IOException, NormalizedNodeParserException;

    private void checkInference(final EffectiveStatementInference inference) {
        final var modelContext = inference.getEffectiveModelContext();
        final var local = getEffectiveModelContext();
        if (!local.equals(modelContext)) {
            throw new IllegalArgumentException("Mismatched inference, expecting backing by " + local);
        }
    }

    private @NonNull SchemaInferenceStack checkInferenceNotEmpty(final EffectiveStatementInference inference) {
        checkInference(inference);
        final var stack = SchemaInferenceStack.ofInference(inference);
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Inference must not be empty");
        }
        return stack;
    }

    private static @NonNull ContainerNode checkNodeContainer(final NormalizedNode node)
            throws NormalizedNodeParserException {
        if (node instanceof ContainerNode container) {
            return container;
        }
        throw new NormalizedNodeParserException(ErrorType.PROTOCOL, ErrorTag.MALFORMED_MESSAGE,
            "Unexpected payload type " + node.contract());
    }

    private static @NonNull NormalizedNode checkNodeName(final NormalizedNode node, final QName expected)
            throws NormalizedNodeParserException {
        final var qname = node.name().getNodeType();
        if (qname.equals(expected)) {
            return node;
        }
        throw new NormalizedNodeParserException(ErrorType.PROTOCOL, ErrorTag.MALFORMED_MESSAGE,
            "Payload name " + qname + " is different from identifier name " + expected);
    }
}
