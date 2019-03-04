/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.transform;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ForwardingNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

/**
 * Stateless Normalized Node Stream Writer decorator, which performs QName translation.
 *
 * <p>
 * This class serves as base for Normalized Node Stream Writer decorators with option to transform
 * QNames by user-implemented {@link #transform(QName)} function.
 */
public abstract class QNameTransformingStreamWriter extends ForwardingNormalizedNodeStreamWriter {

    // FIXME: Probably use loading cache to decrease memory

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
            protected QName transform(final QName key) {
                return transformation.apply(key);
            }

        };
    }

    /**
     * Returns decorator, which uses supplied map to transform QNames. QNames not present in map are left unchanged.
     *
     * @param delegate Underlying normalized node stream writer
     * @param mapping Immutable map which represent mapping from original to new values.
     * @return decorator, which uses supplied mapping to transform QNames.
     */
    public static NormalizedNodeStreamWriter createQNameReplacing(final NormalizedNodeStreamWriter delegate,
            final Map<QName, QName> mapping) {
        return fromFunction(delegate, new QNameReplacementFunction(mapping));
    }

    /**
     * Returns decorator, which uses supplied map to transform QNameModules. QNameModules not present in map are left
     * unchanged.
     *
     * @param delegate Underlying normalized node stream writer
     * @param mapping Immutable map which represent mapping from original to new values.
     * @return decorator, which uses supplied mapping to transform QNameModules.
     */
    public static NormalizedNodeStreamWriter createQNameModuleReplacing(final NormalizedNodeStreamWriter delegate,
            final Map<QNameModule, QNameModule> mapping) {
        return fromFunction(delegate, new QNameModuleReplacementFunction(mapping));
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value) throws IOException {
        super.leafNode(transform(name), value);
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startLeafSet(transform(name), childSizeHint);
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startOrderedLeafSet(transform(name), childSizeHint);
    }

    @Override
    public void leafSetEntryNode(final QName name, final Object value) throws IOException {
        super.leafSetEntryNode(transform(name), value);
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startContainerNode(transform(name), childSizeHint);
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startUnkeyedList(transform(name), childSizeHint);
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startUnkeyedListItem(transform(name), childSizeHint);
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startMapNode(transform(name), childSizeHint);
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
            throws IOException {
        super.startMapEntryNode(transform(identifier), childSizeHint);
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startOrderedMapNode(transform(name), childSizeHint);
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startChoiceNode(transform(name), childSizeHint);
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) throws IOException {
        super.startAugmentationNode(transform(identifier));
    }

    @Override
    public void anyxmlNode(final NodeIdentifier name, final Object value) throws IOException {
        super.anyxmlNode(transform(name), value);
    }

    @Override
    public void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startYangModeledAnyXmlNode(transform(name), childSizeHint);
    }

    /**
     * Transforms a QName to new mapping.
     *
     * <p>
     * NOTE: If QName should be unchanged implementation needs to return original QName.
     *
     * @param key QName to transform.
     * @return Returns new value of QName.
     */
    protected abstract @NonNull QName transform(@NonNull QName key);

    private NodeIdentifier transform(final NodeIdentifier name) {
        final QName original = name.getNodeType();
        final QName transformed = transform(original);
        return transformed == original ? name : new NodeIdentifier(transformed);
    }

    private AugmentationIdentifier transform(final AugmentationIdentifier identifier) {
        ImmutableSet.Builder<QName> builder = ImmutableSet.builder();
        for (QName original : identifier.getPossibleChildNames()) {
            builder.add(transform(original));
        }
        return new AugmentationIdentifier(builder.build());
    }

    private NodeIdentifierWithPredicates transform(final NodeIdentifierWithPredicates identifier) {
        Map<QName, Object> keyValues = new HashMap<>();
        for (Map.Entry<QName, Object> original : identifier.getKeyValues().entrySet()) {
            keyValues.put(transform(original.getKey()), original.getValue());
        }
        return new NodeIdentifierWithPredicates(transform(identifier.getNodeType()), keyValues);
    }
}
