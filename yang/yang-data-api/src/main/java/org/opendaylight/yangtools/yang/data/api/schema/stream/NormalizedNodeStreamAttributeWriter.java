/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import java.io.IOException;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Extension to the NormalizedNodeStreamWriter with attribute support
 */
public interface NormalizedNodeStreamAttributeWriter extends NormalizedNodeStreamWriter {

    void leafNode(YangInstanceIdentifier.NodeIdentifier name, Object value, Map<QName, String> attributes) throws IOException;

    void leafSetEntryNode(Object value, Map<QName, String> attributes) throws IOException;

    void startContainerNode(YangInstanceIdentifier.NodeIdentifier name, int childSizeHint, Map<QName, String> attributes) throws IOException;

    void startUnkeyedListItem(YangInstanceIdentifier.NodeIdentifier name, int childSizeHint, Map<QName, String> attributes) throws IOException;

    void startMapEntryNode(YangInstanceIdentifier.NodeIdentifierWithPredicates identifier, int childSizeHint, Map<QName, String> attributes) throws IOException;
}
