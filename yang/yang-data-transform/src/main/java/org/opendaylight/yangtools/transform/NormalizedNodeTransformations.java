/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.transform;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

public class NormalizedNodeTransformations {

    private NormalizedNodeTransformations() {
        throw new UnsupportedOperationException("Utility class.");
    }

    public static NormalizedNode<?, ?> transformQNames(NormalizedNode<?, ?> original, Function<QName, QName> mapping) {
        NormalizedNodeResult result = new NormalizedNodeResult();
        NormalizedNodeStreamWriter nodeWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        NormalizedNodeStreamWriter transformWriter = QNameTransformingStreamWriter.fromFunction(nodeWriter, mapping);
        try {
            NormalizedNodeWriter.forStreamWriter(transformWriter).write(original);
            return result.getResult();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static NormalizedNode<?, ?> replaceQNames(NormalizedNode<?, ?> original, Map<QName, QName> mapping) {
        return transformQNames(original, new QNameReplacementFunction(mapping));
    }

    public static NormalizedNode<?, ?> replaceQNameModules(NormalizedNode<?, ?> original,
            Map<QNameModule, QNameModule> mapping) {
        return transformQNames(original, new QNameModuleReplacementFunction(mapping));
    }

}
