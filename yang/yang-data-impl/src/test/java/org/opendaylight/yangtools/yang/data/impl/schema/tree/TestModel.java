/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.io.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public final class TestModel {

    public static final QName TEST_QNAME = QName.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test", "2014-03-13",
        "test");
    public static final QName OUTER_LIST_QNAME = QName.create(TEST_QNAME, "outer-list");
    public static final QName INNER_LIST_QNAME = QName.create(TEST_QNAME, "inner-list");
    public static final QName OUTER_CHOICE_QNAME = QName.create(TEST_QNAME, "outer-choice");
    public static final QName ID_QNAME = QName.create(TEST_QNAME, "id");
    public static final QName NAME_QNAME = QName.create(TEST_QNAME, "name");
    public static final QName VALUE_QNAME = QName.create(TEST_QNAME, "value");
    private static final String DATASTORE_TEST_YANG = "/odl-datastore-test.yang";

    public static final YangInstanceIdentifier TEST_PATH = YangInstanceIdentifier.of(TEST_QNAME);
    public static final YangInstanceIdentifier OUTER_LIST_PATH = YangInstanceIdentifier.builder(TEST_PATH).node(OUTER_LIST_QNAME).build();
    public static final QName TWO_QNAME = QName.create(TEST_QNAME, "two");
    public static final QName THREE_QNAME = QName.create(TEST_QNAME, "three");

    private TestModel() {
        throw new UnsupportedOperationException();
    }

    public static InputStream getDatastoreTestInputStream() {
        return TestModel.class.getResourceAsStream(DATASTORE_TEST_YANG);
    }

    public static SchemaContext createTestContext() {
        return createTestContext(DATASTORE_TEST_YANG);
    }

    public static SchemaContext createTestContext(String resourcePath) {
        YangParserImpl parser = new YangParserImpl();
        try {
            return parser.parseSources(Collections.singleton(Resources.asByteSource(TestModel.class
                    .getResource(resourcePath))));
        } catch (IOException | YangSyntaxErrorException e) {
            throw new IllegalStateException("Failed to create context", e);
        }
    }
}
