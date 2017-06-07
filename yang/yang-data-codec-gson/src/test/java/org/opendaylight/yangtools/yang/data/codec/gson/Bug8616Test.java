/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.codec.gson.TestUtils.loadModules;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug8616Test {
    private static final QName ROOT_NAME = QName.create("foo", "2017-06-07", "root");
    private static final QName LIST_NAME = QName.create("foo", "2017-06-07", "list-in-container");
    private static final QName CONTAINER_NAME = QName.create("foo", "2017-06-07", "container-in-list");

    private JSONStringInstanceIdentifierCodec codec;

    @Before
    public void initialization() throws Exception {
        final SchemaContext schemaContext = loadModules("/bug-8616/yang/");
        codec = new JSONStringInstanceIdentifierCodec(schemaContext, JSONCodecFactory.create(schemaContext), true);
    }

    @Test
    public void testDeserializeWildcardId() {
        final YangInstanceIdentifier yid = codec.deserialize("/foo:root/foo:list-in-container/foo:container-in-list");
        final YangInstanceIdentifier expected = YangInstanceIdentifier.create(
            new NodeIdentifier(ROOT_NAME),
            new NodeIdentifier(LIST_NAME),
            new NodeIdentifierWithPredicates(LIST_NAME, Collections.emptyMap()),
            new NodeIdentifier(CONTAINER_NAME));
        assertEquals(expected, yid);
    }
}
