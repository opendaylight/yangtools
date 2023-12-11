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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.InputStreamNormalizer;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationException;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * An {@link AbstractCodecFactory} which additionally provides services defined in {@link InputStreamNormalizer}.
 *
 * <p>
 * This class existsonly because both JSON and XML implementations of {@link InputStreamNormalizer} are naturally hosted
 * in their respective {@link AbstractCodecFactory} implementations and therefore it is a convenient place to share
 * common implementation bits.
 */
public abstract class AbstractInputStreamNormalizer<T extends TypeAwareCodec<?, ?, ?>>
        extends AbstractCodecFactory<T> implements InputStreamNormalizer {
    protected AbstractInputStreamNormalizer(final @NonNull EffectiveModelContext schemaContext,
            final @NonNull CodecCache<T> cache) {
        super(schemaContext, cache);
    }

    @Override
    public final ContainerNode parseDatastore(final NodeIdentifier containerName, final Unqualified moduleName,
            final InputStream stream) throws NormalizationException {
        try {
            return parseDatastore(requireNonNull(stream), requireNonNull(containerName), requireNonNull(moduleName));
        } catch (IOException | IllegalArgumentException e) {
            throw NormalizationException.ofCause(e);
        }
    }

    protected abstract @NonNull ContainerNode parseDatastore(@NonNull InputStream stream,
        @NonNull NodeIdentifier containerName, @NonNull Unqualified moduleName)
            throws IOException, NormalizationException;

    @Override
    public final NormalizedNode parseData(final EffectiveStatementInference inference, final InputStream stream)
            throws NormalizationException {
        final var stack = checkInferenceNotEmpty(inference);
        final var stmt = stack.currentStatement();
        if (!(stmt instanceof DataTreeEffectiveStatement<?> dataStmt)) {
            throw new IllegalArgumentException("Invalid inference statement " + stmt);
        }

        final NormalizedNode data;
        try {
            data = parseData(stack, requireNonNull(stream));
        } catch (IOException | IllegalArgumentException e) {
            throw NormalizationException.ofCause(e);
        }
        return checkNodeName(data, dataStmt.argument());
    }

    protected abstract @NonNull NormalizedNode parseData(@NonNull SchemaInferenceStack stack,
        @NonNull InputStream stream) throws IOException, NormalizationException;

    @Override
    public final PrefixAndData parseChildData(final EffectiveStatementInference inference, final InputStream stream)
            throws NormalizationException {
        checkInference(inference);

        final NormalizedNode data;
        try {
            data = parseChildData(requireNonNull(stream), inference);
        } catch (IOException | IllegalArgumentException e) {
            throw NormalizationException.ofCause(e);
        }

        final var prefix = new ArrayList<@NonNull PathArgument>();
        var result = data;

        // Deal with the semantic differences of what "child" means in NormalizedNode versus in YANG data tree
        // structure.

        // NormalizedNode structure has 'choice' statements visible and addressable, whereas YANG data tree makes
        // them completely transparent.
        //
        // Therefore we need to peel any ChoiceNode from the result and shift them to the prefix. Since each choice was
        // created implicitly to contain the element mentioned in the stream.
        while (result instanceof ChoiceNode choice) {
            final var childNode = choice.body().iterator().next();
            prefix.add(result.name());
            result = childNode;
        }

        // NormalizedNode structure has 'list' and 'leaf-list' statements visible and addressable, whereas YANG data
        // tree addressing can only point to individual instances. RFC8040 section 4.4.1 states:
        //
        //        The message-body is expected to contain the
        //        content of a child resource to create within the parent (target
        //        resource).  The message-body MUST contain exactly one instance of the
        //        expected data resource.  The data model for the child tree is the
        //        subtree, as defined by YANG for the child resource.
        //
        // Therefore we need to peel any UnkeyedListNode, MapNode and LeafSetNodes from the top-level and shift them
        // to the prefix. Note that from the parser perspective, each such node can legally contain zero, one or more
        // entries, but this method is restricted to allowing only a single entry.
        if (result instanceof MapNode || result instanceof LeafSetNode || result instanceof UnkeyedListNode) {
            final var body = ((NormalizedNodeContainer<?>) result).body();
            final var size = body.size();
            if (body.size() != 1) {
                throw NormalizationException.ofMessage(
                    "Exactly one instance of " + result.name().getNodeType() + " is required, " + size + " supplied");
            }
            prefix.add(result.name());
            result = body.iterator().next();
        }

        return new PrefixAndData(prefix, result);
    }

    protected abstract @NonNull NormalizedNode parseChildData(@NonNull InputStream stream,
        @NonNull EffectiveStatementInference inference) throws IOException, NormalizationException;

    @Override
    public final ContainerNode parseInput(final EffectiveStatementInference inference, final InputStream stream)
            throws NormalizationException {
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
        return parseInputOutput(stream, stack, expected);
    }

    @Override
    public final ContainerNode parseOutput(final EffectiveStatementInference inference, final InputStream stream)
            throws NormalizationException {
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
        return parseInputOutput(stream, stack, expected);
    }

    private @NonNull ContainerNode parseInputOutput(final @NonNull InputStream stream,
            final @NonNull SchemaInferenceStack stack, final @NonNull QName expected) throws NormalizationException {
        final NormalizedNode data;
        try {
            data = parseInputOutput(stack, expected, requireNonNull(stream));
        } catch (IOException | IllegalArgumentException e) {
            throw NormalizationException.ofCause(e);
        }
        return checkNodeContainer(data);
    }

    protected abstract @NonNull NormalizedNode parseInputOutput(@NonNull SchemaInferenceStack stack,
        @NonNull QName expected, @NonNull InputStream stream) throws IOException, NormalizationException;

    private void checkInference(final EffectiveStatementInference inference) {
        final var modelContext = inference.getEffectiveModelContext();
        final var local = getEffectiveModelContext();
        if (!local.equals(modelContext)) {
            throw new IllegalArgumentException("Mismatched inference, expecting model context " + local);
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

    protected static final @NonNull ContainerNode checkNodeContainer(final NormalizedNode node)
            throws NormalizationException {
        if (node instanceof ContainerNode container) {
            return container;
        }
        throw NormalizationException.ofMessage("Unexpected payload type " + node.contract());
    }

    protected static final @NonNull NormalizedNode checkNodeName(final NormalizedNode node, final QName expected)
            throws NormalizationException {
        final var qname = node.name().getNodeType();
        if (qname.equals(expected)) {
            return node;
        }
        throw NormalizationException.ofMessage(
            "Payload name " + qname + " is different from identifier name " + expected);
    }
}
