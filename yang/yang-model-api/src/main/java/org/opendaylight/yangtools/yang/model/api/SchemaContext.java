/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.net.URI;
import java.util.Date;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;

/**
 * The interface contains the methods for manipulating all the top level context
 * data (data from all red modules) like YANG notifications, extensions,
 * operations...
 */
public interface SchemaContext extends ContainerSchemaNode {

    public static final QName NAME = QName.create(URI.create("urn:ietf:params:xml:ns:netconf:base:1.0"), null, "data");

    /**
     * Returns data schema node instances which represents direct subnodes (like
     * leaf, leaf-list, list, container) in all YANG modules in the context.
     * 
     * @return set of <code>DataSchemaNode</code> instances which represents
     *         YANG data nodes at the module top level
     */
    Set<DataSchemaNode> getDataDefinitions();

    /**
     * Returns modules which are part of the schema context.
     * 
     * @return set of the modules which belong to the schema context
     */
    Set<Module> getModules();

    /**
     * 
     * Returns notification definition instances which are defined as the direct
     * subelements in all YANG modules in the context.
     * 
     * @return set of <code>NotificationDefinition</code> instances which
     *         represents nodes defined via <code>notification</code> YANG
     *         keyword
     */
    Set<NotificationDefinition> getNotifications();

    /**
     * Returns rpc definition instances which are defined as the direct
     * subelements in all YANG modules in the context.
     * 
     * @return set of <code>RpcDefinition</code> instances which represents
     *         nodes defined via <code>rpc</code> YANG keyword
     */
    Set<RpcDefinition> getOperations();

    /**
     * Returns extencion definition instances which are defined as the direct
     * subelements in all YANG modules in the context
     * 
     * @return set of <code>ExtensionDefinition</code> instances which
     *         represents nodes defined via <code>extension</code> YANG keyword
     */
    Set<ExtensionDefinition> getExtensions();

    /**
     * Returns module instance (from the context) with concrete name and
     * revision date.
     * 
     * @param name
     *            string with the module name
     * @param revision
     *            date of the module revision
     * @return module instance which has name and revision (if specified) the
     *         same as are the values specified in parameters <code>name</code>
     *         and <code>revision</code>. In other cases the <code>null</code>
     *         value is returned.
     * 
     */
    Module findModuleByName(final String name, final Date revision);

    /**
     * 
     * Returns module instance (from the context) with concrete namespace.
     * 
     * @param namespace
     *            URI instance with specified namespace
     * @return module instance which has namespace equal to the
     *         <code>namespace</code> or <code>null</code> in other cases
     */
    Set<Module> findModuleByNamespace(final URI namespace);
    
    Module findModuleByNamespaceAndRevision(final URI namespace,final Date revision);
}
