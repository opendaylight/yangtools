/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
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

    private static InputStream getInputStream(final String resourceName) {
        return TypeProviderModel.class.getResourceAsStream(resourceName);
    }

    private static List<InputStream> provideTestModelStreams() {
        final List<InputStream> arrayList = new ArrayList<>();

        arrayList.add(getInputStream(BASE_YANG_TYPES_PATH));
        arrayList.add(getInputStream(TEST_TYPE_PROVIDER_PATH));
        arrayList.add(getInputStream(TEST_TYPE_PROVIDER_B_PATH));
        return arrayList;
    }

    public static SchemaContext createTestContext() {
        YangParserImpl parser = new YangParserImpl();
        Set<Module> modules = parser.parseYangModelsFromStreams(provideTestModelStreams());
        return parser.resolveSchemaContext(modules);
    }
}
