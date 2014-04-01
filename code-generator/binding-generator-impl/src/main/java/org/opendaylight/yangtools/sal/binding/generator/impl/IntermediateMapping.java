/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.CompositeNodeTOImpl;
import org.opendaylight.yangtools.yang.data.impl.SimpleNodeTOImpl;

@SuppressWarnings("unchecked")
public class IntermediateMapping {

    public static Node<? extends Object> toNode(final Map<? extends Object, ? extends Object> map) {
        if ((map instanceof Node<?>)) {
            return ((Node<?>) map);
        }
        final Map<QName, Object> nodeMap = ((Map<QName, Object>) map);
        Preconditions.checkArgument(map.size() == 1);
        final Entry<QName, Object> elem = nodeMap.entrySet().iterator().next();
        final QName qname = elem.getKey();
        final Object value = elem.getValue();
        return toNodeImpl(qname, value);
    }

    protected static Node<? extends Object> _toNodeImpl(final QName name, final List<? extends Object> objects) {
        List<Node<? extends Object>> values = new ArrayList<>(objects.size());
        for (Object obj : objects) {
            if ((obj instanceof Node<?>)) {
                values.add(((Node<?>) obj));
            } else {
                if ((obj instanceof Map<?, ?>)) {
                    Node<? extends Object> _node = IntermediateMapping.toNode(((Map<?, ?>) obj));
                    values.add(_node);
                }
            }
        }
        return new CompositeNodeTOImpl(name, null, values);
    }

    protected static Node<? extends Object> _toNodeImpl(final QName name, final Map<QName, Object> object) {
        throw new UnsupportedOperationException("Unsupported node hierarchy.");
    }

    protected static Node<? extends Object> _toNodeImpl(final QName name, final Object object) {
        return new SimpleNodeTOImpl<Object>(name, null, object);
    }

    public static Node<? extends Object> toNodeImpl(final QName name, final Object objects) {
        if (objects instanceof List) {
            return _toNodeImpl(name, (List<?>) objects);
        } else if (objects instanceof Map) {
            return _toNodeImpl(name, (Map<QName, Object>) objects);
        } else if (objects != null) {
            return _toNodeImpl(name, objects);
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: "
                    + Arrays.<Object> asList(name, objects).toString());
        }
    }

}
