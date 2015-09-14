/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

/**
 * Test Model Provider designated to load test resources and provide Schema Context
 * for testing of TypeProviderImpl
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
final class TypeProviderModel {

    public static final String TEST_TYPE_PROVIDER_MODULE_NAME = "test-type-provider";

    private static final String BASE_YANG_TYPES_PATH = "/base-yang-types.yang";
    private static final String TEST_TYPE_PROVIDER_PATH = "/"+TEST_TYPE_PROVIDER_MODULE_NAME+".yang";
    private static final String TEST_TYPE_PROVIDER_B_PATH = "/test-type-provider-b.yang";

    private static ByteSource getByteSource(final String resourceName) {
        return Resources.asByteSource(TypeProviderModel.class.getResource(resourceName));
    }

    private static List<ByteSource> provideTestModelStreams() {
        final List<ByteSource> arrayList = new ArrayList<>();

        arrayList.add(getByteSource(BASE_YANG_TYPES_PATH));
        arrayList.add(getByteSource(TEST_TYPE_PROVIDER_PATH));
        arrayList.add(getByteSource(TEST_TYPE_PROVIDER_B_PATH));
        return arrayList;
    }

    public static SchemaContext createTestContext() throws IOException, YangSyntaxErrorException {
        return new YangParserImpl().parseSources(provideTestModelStreams());
    }
}
