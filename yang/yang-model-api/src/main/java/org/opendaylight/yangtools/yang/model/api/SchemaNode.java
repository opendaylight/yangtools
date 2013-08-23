/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;

/**
 * SchemaNode represents a node in schema tree.
 */
public interface SchemaNode {

    /**
     * Returns QName of the instance of the type <code>SchemaNode</code>.
     * 
     * @return QName with the name of the schema node
     */
    public QName getQName();

    /**
     * Returns the schema path of the instance of the type
     * <code>SchemaNode</code> <code>SchemaNode</code>.
     * 
     * @return schema path of the schema node
     */
    public SchemaPath getPath();

    /**
     * Returns description of the instance of the type <code>SchemaNode</code>
     * 
     * @return string with textual description the node which represents the
     *         argument of the YANG <code>description</code> substatement
     */
    public String getDescription();

    /**
     * Returns reference of the instance of the type <code>SchemaNode</code>
     * 
     * The reference refers to external document that provides additional
     * information relevant for the instance of this type.
     * 
     * @return string with the reference to some external document which
     *         represents the argument of the YANG <code>reference</code>
     *         substatement
     */
    public String getReference();

    /**
     * Returns status of the instance of the type <code>SchemaNode</code>
     * 
     * @return status of this node which represents the argument of the YANG
     *         <code>status</code> substatement
     */
    public Status getStatus();

    /**
     * 
     * Returns unknown schema nodes which belongs to this instance of the type
     * <code>SchemaNode</code>.
     * 
     * @return list of unknown schema nodes defined under this schema node.
     */
    public List<UnknownSchemaNode> getUnknownSchemaNodes();

}
