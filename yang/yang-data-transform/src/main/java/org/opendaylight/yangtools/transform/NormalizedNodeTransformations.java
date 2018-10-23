/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.transform;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

@Beta
public final class NormalizedNodeTransformations {

    private NormalizedNodeTransformations() {
        throw new UnsupportedOperationException("Utility class.");
    }

    public static NormalizedNode<?, ?> transformQNames(final NormalizedNode<?, ?> original,
            final Function<QName, QName> mapping) {
        NormalizedNodeResult result = new NormalizedNodeResult();
        NormalizedNodeStreamWriter nodeWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        NormalizedNodeStreamWriter transformWriter = QNameTransformingStreamWriter.fromFunction(nodeWriter, mapping);
        try {
            NormalizedNodeWriter.forStreamWriter(transformWriter).write(original);
            return result.getResult();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a {@link NormalizedNode} with QNames replaced by supplied mapping.
     *
     * @param original Original Normalized Node
     * @param mapping Map of QNames to transform. Not listed QNames are preserved.
     * @return Normalized Node with replaced QNames.
     */
    public static NormalizedNode<?, ?> replaceQNames(final @NonNull NormalizedNode<?, ?> original,
            final @NonNull Map<QName, QName> mapping) {
        return transformQNames(original, new QNameReplacementFunction(mapping));
    }

    /**
     * Returns a {@link NormalizedNode} with QNameModules replaced by supplied mapping.
     *
     * @param original Original Normalized Node
     * @param mapping Map of QNameModules to transform. Not listed QNameModules are preserved.
     * @return Normalized Node with replaced QNameModules.
     */
    public static NormalizedNode<?, ?> replaceQNameModules(final @NonNull NormalizedNode<?, ?> original,
            final @NonNull Map<QNameModule, QNameModule> mapping) {
        return transformQNames(original, new QNameModuleReplacementFunction(mapping));
    }
}
