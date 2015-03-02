/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.codec.InstanceIdentifierCodec;

/**
 * Abstract utility class for representations which encode {@link YangInstanceIdentifier} as a
 * prefix:name tuple. Typical uses are RESTCONF/JSON (module:name) and XML (prefix:name).
 */
@Beta
public abstract class AbstractStringInstanceIdentifierCodec extends AbstractNamespaceCodec implements InstanceIdentifierCodec<String> {

    @Override
    public final String serialize(final YangInstanceIdentifier data) {
        StringBuilder sb = new StringBuilder();
        for (PathArgument arg : data.getPathArguments()) {
            if(arg instanceof AugmentationIdentifier) {
                /*
                 * XML/YANG instance identifier does not have concept
                 * of augmentation identifier, which identifies
                 * mixin (same as paretn element), so we can safely
                 * ignore it if it is part of path (since child node)
                 * is identified in same fashion.
                 *
                 */
                continue;
            }

            sb.append('/');
            appendQName(sb, arg.getNodeType());

            if (arg instanceof NodeIdentifierWithPredicates) {
                for (Map.Entry<QName, Object> entry : ((NodeIdentifierWithPredicates) arg).getKeyValues().entrySet()) {
                    sb.append('[');
                    appendQName(sb, entry.getKey());
                    sb.append("='");
                    sb.append(String.valueOf(entry.getValue()));
                    sb.append("']");
                }
            } else if (arg instanceof NodeWithValue) {
                sb.append("[.='");
                sb.append(((NodeWithValue) arg).getValue());
                sb.append("']");
            }
        }

        return sb.toString();
    }

    @Override
    public final YangInstanceIdentifier deserialize(final String data) {
        Preconditions.checkNotNull(data, "Data may not be null");
        PathArgumentParsingIterator parsingIterator = new PathArgumentParsingIterator(this, data);
        ImmutableList<PathArgument> result = ImmutableList.copyOf(parsingIterator);
        return YangInstanceIdentifier.create(result);
    }

}
