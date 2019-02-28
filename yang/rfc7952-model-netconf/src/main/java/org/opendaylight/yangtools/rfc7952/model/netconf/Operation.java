/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.netconf;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationEffectiveStatement;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.rfc7952.parser.AnnotationStatementSupport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;

/**
 * Enumeration covering possible values of "operation" attribute as defined in RFC6241 section 7.2. This class mimics
 * to a large extent what would be generated for MD-SAL Binding representation of the type.
 */
@Beta
public enum Operation {
    /**
     * The configuration data identified by the element containing this attribute is
     * merged with the configuration at the corresponding level in the configuration
     * datastore identified by the &lt;target&gt;parameter. This is the default
     * behavior.
     */
    Merge(0, "merge"),

    /**
     * The configuration data identified by the element containing this attribute
     * replaces any related configuration in the configuration datastore identified by
     * the &lt;target&gt; parameter. If no such configuration data exists in the
     * configuration datastore, it is created. Unlike a &lt;copy-config&gt;
     * operation, which replaces the entire target configuration, only the configuration
     * actually present in the &lt;config&gt; parameter is affected.
     */
    Replace(1, "replace"),

    /**
     * The configuration data identified by the element containing this attribute is
     * added to the configuration if and only if the configuration data does not
     * already exist in the configuration datastore. If the configuration data exists, an
     * &lt;rpc-error&gt; element is returned with an &lt;error-tag&gt;
     * value of "data-exists".
     */
    Create(2, "create"),

    /**
     * The configuration data identified by the element containing this attribute is
     * deleted from the configuration if and only if the configuration data currently
     * exists in the configuration datastore. If the configuration data does not exist,
     * an &lt;rpc-error&gt;element is returned with an
     * &lt;error-tag&gt; value of "data-missing".
     */
    Delete(3, "delete"),

    /**
     * The configuration data identified by the element containing this attribute is
     * deleted from the configuration if the configuration data currently exists in the
     * configuration datastore. If the configuration data does not exist, the "remove"
     * operation is silently ignored by the server.
     *
     */
    Remove(4, "remove");

    private static final Map<String, Operation> NAME_MAP;
    private static final Map<Integer, Operation> VALUE_MAP;

    static {
        final Builder<String, Operation> nb = ImmutableMap.builder();
        final Builder<Integer, Operation> vb = ImmutableMap.builder();
        for (Operation enumItem : Operation.values()) {
            vb.put(enumItem.value, enumItem);
            nb.put(enumItem.name, enumItem);
        }

        NAME_MAP = nb.build();
        VALUE_MAP = vb.build();
    }

    /**
     * SchemaNode representing the definition of "nc:operation" metadata.
     */
    private static final AnnotationEffectiveStatement OPERATION_ANNOTATION;

    static {
        final SchemaContext context;
        try {
            context = new YangParserFactoryImpl().createParser()
                    .addSource(YangTextSchemaSource.forResource(Operation.class, "/ietf-netconf.yang"))
                    .addLibSource(YangTextSchemaSource.forResource(AnnotationStatementSupport.class,
                        "/ietf-yang-metadata@2016-08-05.yang"))
                    .buildSchemaContext();
        } catch (YangParserException | IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        final List<UnknownSchemaNode> nodes = context.getUnknownSchemaNodes();
        checkState(nodes.size() == 1, "Unexpected unknown nodes %s", nodes);
        final UnknownSchemaNode first = nodes.get(0);
        checkState(first instanceof AnnotationEffectiveStatement, "Unexpected node %s", first);
        final AnnotationEffectiveStatement node = (AnnotationEffectiveStatement)first;
        checkState("operation".equals(node.getNodeParameter()), "Unexpected node parameter in %s", node);
        OPERATION_ANNOTATION = node;
    }

    private final String name;
    private final int value;

    Operation(final int value, final String name) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getIntValue() {
        return value;
    }

    /**
     * Return the enumeration member whose {@link #getName()} matches specified value.
     *
     * @param name YANG assigned name
     * @return corresponding Operation item, if present
     * @throws NullPointerException if name is null
     */
    public static Optional<Operation> forName(final String name) {
        return Optional.ofNullable(NAME_MAP.get(requireNonNull(name)));
    }

    /**
     * Return the enumeration member whose {@link #getIntValue()} matches specified value.
     *
     * @param intValue integer value
     * @return corresponding Operation item, or null if no such item exists
     */
    public static Operation forValue(final int intValue) {
        return VALUE_MAP.get(intValue);
    }

    public static AnnotationSchemaNode getSchemaNode() {
        return OPERATION_ANNOTATION;
    }

    public static AnnotationEffectiveStatement getEffectiveStatement() {
        return OPERATION_ANNOTATION;
    }
}
