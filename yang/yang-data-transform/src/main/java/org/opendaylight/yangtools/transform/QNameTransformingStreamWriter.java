/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.transform;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

/**
 *
 * Stateless Normalized Node Stream Writer decorator, which performs QName translation.
 *
 * This class serves as base for Normalized Node Stream Writer decorators with option to transform
 * QNames by user-implemented {@link #transform(QName)} function.
 *
 */
public abstract class QNameTransformingStreamWriter extends ForwardingObject implements NormalizedNodeStreamWriter {

    // FIXME: Probably use loading cache to decrease memory
    @Override
    protected abstract NormalizedNodeStreamWriter delegate();

    /**
     * Returns decorator, which uses supplied function to transform QNames.
     *
     * @param delegate Underlying normalized node stream writer
     * @param transformation Transformation function, function is required to return non-null
     *        values.
     * @return decorator, which uses supplied function to transform QNames.
     */
    public static NormalizedNodeStreamWriter fromFunction(final NormalizedNodeStreamWriter delegate,
            final Function<QName, QName> transformation) {
        return new QNameTransformingStreamWriter() {

            @Override
            protected NormalizedNodeStreamWriter delegate() {
              return delegate;
            }

            @Override
            protected QName transform(QName key) {
                return transformation.apply(key);
            }

        };
    }

    /**
     * Returns decorator, which uses supplied map to transform QNames.
     *
     * QNames not present in map are left unchanged.
     *
     * @param delegate Underlying normalized node stream writer
     * @param mapping Immutable map which represent mapping from original to new values.
     * @return decorator, which uses supplied mapping to transform QNames.
     */
    public static NormalizedNodeStreamWriter createQNameReplacing(NormalizedNodeStreamWriter delegate,
            final Map<QName, QName> mapping) {
        return fromFunction(delegate, new QNameReplacementFunction(mapping));
    }

    /**
     * Returns decorator, which uses supplied map to transform QNameModules.
     *
     * QNameModules not present in map are left unchanged.
     *
     * @param delegate Underlying normalized node stream writer
     * @param mapping Immutable map which represent mapping from original to new values.
     * @return decorator, which uses supplied mapping to transform QNameModules.
     */
    public static NormalizedNodeStreamWriter createQNameModuleReplacing(NormalizedNodeStreamWriter delegate,
            final Map<QNameModule, QNameModule> mapping) {
        return fromFunction(delegate, new QNameModuleReplacementFunction(mapping));
    }

    @Override
    public void leafNode(NodeIdentifier name, Object value) throws IOException, IllegalArgumentException {
        delegate.leafNode(transform(name), value);
    }

    /**
     * Transforms a QName to new mapping.
     *
     * NOTE: If QName should be unchanged implemention needs to return original QName.
     *
     * @param key QName to transform.
     * @return Returns new value of QName.
     */
    protected abstract @Nonnull QName transform(@Nonnull QName key);

    @Override
    public void startLeafSet(NodeIdentifier name, int childSizeHint) throws IOException, IllegalArgumentException {
        delegate().startLeafSet(transform(name), childSizeHint);
    }

    @Override
    public void leafSetEntryNode(Object value) throws IOException, IllegalArgumentException {
        delegate().leafSetEntryNode(value);
    }

    @Override
    public void startContainerNode(NodeIdentifier name, int childSizeHint) throws IOException, IllegalArgumentException {
        delegate().startContainerNode(transform(name), childSizeHint);
    }

    @Override
    public void startUnkeyedList(NodeIdentifier name, int childSizeHint) throws IOException, IllegalArgumentException {
        delegate().startUnkeyedList(transform(name), childSizeHint);
    }

    @Override
    public void startUnkeyedListItem(NodeIdentifier name, int childSizeHint) throws IOException, IllegalStateException {
        delegate().startUnkeyedListItem(transform(name), childSizeHint);
    }

    @Override
    public void startMapNode(NodeIdentifier name, int childSizeHint) throws IOException, IllegalArgumentException {
        delegate.startMapNode(transform(name), childSizeHint);
    }

    @Override
    public void startMapEntryNode(NodeIdentifierWithPredicates identifier, int childSizeHint) throws IOException,
            IllegalArgumentException {
        delegate().startMapEntryNode(transform(identifier), childSizeHint);
    }

    @Override
    public void startOrderedMapNode(NodeIdentifier name, int childSizeHint) throws IOException,
            IllegalArgumentException {
        delegate().startOrderedMapNode(transform(name), childSizeHint);
    }

    @Override
    public void startChoiceNode(NodeIdentifier name, int childSizeHint) throws IOException, IllegalArgumentException {
        delegate().startChoiceNode(transform(name), childSizeHint);
    }

    @Override
    public void startAugmentationNode(AugmentationIdentifier identifier) throws IOException, IllegalArgumentException {
        delegate().startAugmentationNode(transform(identifier));
    }

    @Override
    public void anyxmlNode(NodeIdentifier name, Object value) throws IOException, IllegalArgumentException {
        delegate().anyxmlNode(transform(name), value);
    }

    @Override
    public void endNode() throws IOException, IllegalStateException {
        delegate().endNode();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }

    @Override
    public void flush() throws IOException {
        delegate().flush();
    }

    private NodeIdentifier transform(NodeIdentifier name) {
        return new NodeIdentifier(transform(name.getNodeType()));
    }

    private AugmentationIdentifier transform(AugmentationIdentifier identifier) {
        ImmutableSet.Builder<QName> builder = ImmutableSet.builder();
        for (QName original : identifier.getPossibleChildNames()) {
            builder.add(transform(original));
        }
        return new AugmentationIdentifier(builder.build());
    }

    private NodeIdentifierWithPredicates transform(NodeIdentifierWithPredicates identifier) {
        Map<QName, Object> keyValues = new HashMap<>();
        for (Map.Entry<QName, Object> original : identifier.getKeyValues().entrySet()) {
            keyValues.put(transform(original.getKey()), original.getValue());
        }
        return new NodeIdentifierWithPredicates(transform(identifier.getNodeType()), keyValues);
    }
}
