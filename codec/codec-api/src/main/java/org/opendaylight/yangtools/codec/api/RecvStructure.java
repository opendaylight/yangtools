/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.codec.api;

import java.util.LinkedHashMap;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A {@link Recv} receiving {@code sx:structure} as expressed by an {@link StructureEffectiveStatement}.
 *
 * @since 16.2.0
 */
@NonNullByDefault
public final class RecvStructure extends ResultRecv {
    private final LinkedHashMap<QName, Recv.OfDataNode> children = new LinkedHashMap<>();

    RecvStructure(final SchemaInferenceStack stack, final StructureEffectiveStatement stmt) {
        super(stack);
    }

    // FIXME: reconsider these two: namespace model should be established during initial bind and we should be receiving
    //        'String namespaceRef' and call Unqualified.of()/XMLNamesoace.of() and wrap any parsing errors with
    //        CodecException but also:
    //        - .sid-aware CBOR codec should be capable of resolving to exact schema, right?
    //        - JSON/XML-captured anyxml and anydata should be able to store the validated name, perhaps as a tuple
    //        So do we really have a ChildRef composed of a type-safe nsref and local name?

    // JSON semantics: namespace identified by module name
    public Recv.OfDataNode receiveChild(final Unqualified moduleName, final Unqualified localName)
            throws CodecException {

        throw new UnsupportedOperationException();
    }

    // XML semantics: namespace identified by
    public Recv.OfDataNode receiveChild(final XMLNamespace namespace, final Unqualified localName)
            throws CodecException {


        throw new UnsupportedOperationException();
    }
}
