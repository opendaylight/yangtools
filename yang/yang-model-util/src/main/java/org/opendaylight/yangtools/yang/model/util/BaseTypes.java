/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Utility methods and constants to work with built-in YANG types
 *
 *
 */
public final class BaseTypes {

    private BaseTypes() {
    }

    public static final URI BASE_TYPES_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:1");

    public static final QName BINARY_QNAME = constructQName("binary");
    public static final QName BITS_QNAME = constructQName("bits");
    public static final QName BOOLEAN_QNAME = constructQName("boolean");
    public static final QName DECIMAL64_QNAME = constructQName("decimal64");
    public static final QName EMPTY_QNAME = constructQName("empty");
    public static final QName ENUMERATION_QNAME = constructQName("enumeration");
    public static final QName IDENTITYREF_QNAME = constructQName("identityref");
    public static final QName INSTANCE_IDENTIFIER_QNAME = constructQName("instance-identifier");
    public static final QName INT8_QNAME = constructQName("int8");
    public static final QName INT16_QNAME = constructQName("int16");
    public static final QName INT32_QNAME = constructQName("int32");
    public static final QName INT64_QNAME = constructQName("int64");
    public static final QName LEAFREF_QNAME = constructQName("leafref");
    public static final QName STRING_QNAME = constructQName("string");
    public static final QName UINT8_QNAME = constructQName("uint8");
    public static final QName UINT16_QNAME = constructQName("uint16");
    public static final QName UINT32_QNAME = constructQName("uint32");
    public static final QName UINT64_QNAME = constructQName("uint64");
    public static final QName UNION_QNAME = constructQName("union");

    private static final Set<String> BUILD_IN_TYPES = ImmutableSet.<String> builder().add(BINARY_QNAME.getLocalName()) //
            .add(BITS_QNAME.getLocalName()) //
            .add(BOOLEAN_QNAME.getLocalName()) //
            .add(DECIMAL64_QNAME.getLocalName()) //
            .add(EMPTY_QNAME.getLocalName()) //
            .add(ENUMERATION_QNAME.getLocalName()) //
            .add(IDENTITYREF_QNAME.getLocalName()) //
            .add(INSTANCE_IDENTIFIER_QNAME.getLocalName()) //
            .add(INT8_QNAME.getLocalName()) //
            .add(INT16_QNAME.getLocalName()) //
            .add(INT32_QNAME.getLocalName()) //
            .add(INT64_QNAME.getLocalName()) //
            .add(LEAFREF_QNAME.getLocalName()) //
            .add(STRING_QNAME.getLocalName()) //
            .add(UINT8_QNAME.getLocalName()) //
            .add(UINT16_QNAME.getLocalName()) //
            .add(UINT32_QNAME.getLocalName()) //
            .add(UINT64_QNAME.getLocalName()) //
            .add(UNION_QNAME.getLocalName()) //
            .build();

    /**
     * Construct QName for Built-in base Yang type. The namespace for built-in
     * base yang types is defined as: urn:ietf:params:xml:ns:yang:1
     *
     * @param typeName
     *            yang type name
     * @return built-in base yang type QName.
     */
    public static QName constructQName(final String typeName) {
        return new QName(BASE_TYPES_NAMESPACE, typeName);
    }

    /**
     * Creates Schema Path from {@link QName}.
     *
     * @param typeName
     *            yang type QName
     * @return Schema Path from Qname.
     * @deprecated Use {@link SchemaPath#create(boolean, QName...)} instead.
     */
    @Deprecated
    public static SchemaPath schemaPath(final QName typeName) {
        return SchemaPath.create(Collections.singletonList(typeName), true);
    }

    /**
     * Creates Schema Path from List of partial paths defined as Strings, module
     * Namespace and module latest Revision Date.
     *
     * @param actualPath
     *            List of partial paths
     * @param namespace
     *            Module Namespace
     * @param revision
     *            Revision Date
     * @return Schema Path
     *
     * @deprecated Use {@link SchemaPath#create(boolean, Iterable)} with QNames
     *             manually constructed.
     */
    @Deprecated
    public static SchemaPath schemaPath(final List<String> actualPath, final URI namespace, final Date revision) {
        if (actualPath == null) {
            throw new IllegalArgumentException("The actual path List MUST be specified.");
        }
        final List<QName> pathList = new ArrayList<QName>();
        for (final String path : actualPath) {
            final QName qname = new QName(namespace, revision, path);
            if (qname != null) {
                pathList.add(qname);
            }
        }
        return SchemaPath.create(pathList, true);
    }

    /**
     * Returns true if supplied type is representation of built-in YANG type as
     * per RFC 6020.
     *
     * See package documentation for description of base types.
     *
     * @param type
     * @return true if type is built-in YANG Types.
     */
    public static boolean isYangBuildInType(final String type) {
        return BUILD_IN_TYPES.contains(type);
    }

    /**
     * Returns default instance of built-in for supplied type
     *
     * See package documentation for description of base build-in types
     * with default instance.
     *
     * @param typeName
     * @return Returns default instance or {@link Optional#absent()} if default
     *         instance does not exists
     *
     */
    public static Optional<TypeDefinition<?>> defaultBaseTypeFor(final String typeName) {
        return Optional.<TypeDefinition<?>> fromNullable(defaultBaseTypeForImpl(typeName));
    }

    private static TypeDefinition<?> defaultBaseTypeForImpl(final String typeName) {
        Preconditions.checkNotNull(typeName, "typeName must not be null.");

        if (typeName.startsWith("int")) {
            if ("int8".equals(typeName)) {
                return Int8.getInstance();
            } else if ("int16".equals(typeName)) {
                return Int16.getInstance();
            } else if ("int32".equals(typeName)) {
                return Int32.getInstance();
            } else if ("int64".equals(typeName)) {
                return Int64.getInstance();
            }
        } else if (typeName.startsWith("uint")) {
            if ("uint8".equals(typeName)) {
                return Uint8.getInstance();
            } else if ("uint16".equals(typeName)) {
                return Uint16.getInstance();
            } else if ("uint32".equals(typeName)) {
                return Uint32.getInstance();
            } else if ("uint64".equals(typeName)) {
                return Uint64.getInstance();
            }
        } else if ("string".equals(typeName)) {
            return StringType.getInstance();
        } else if ("binary".equals(typeName)) {
            return BinaryType.getInstance();
        } else if ("boolean".equals(typeName)) {
            return BooleanType.getInstance();
        } else if ("empty".equals(typeName)) {
            return EmptyType.getInstance();
        } else if ("instance-identifier".equals(typeName)) {
            return InstanceIdentifier.getInstance();
        }
        return null;
    }

}
