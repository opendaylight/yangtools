/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import java.util.Map;
import java.util.Map.Entry;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Serialization service, which provides two-way serialization between
 * Java Binding Data representation and NormalizedNode representation.
 *
 */
public interface BindingNormalizedNodeSerializer {

     /**
      * Translates supplied Binding Instance Identifier into NormalizedNode instance identifier.
      *
      * @param binding Binding Instance Identifier
      * @return DOM Instance Identifier
      */
     YangInstanceIdentifier toYangInstanceIdentifier(InstanceIdentifier<?> binding);

     /**
      * Translates supplied YANG Instance Identifier into Binding instance identifier.
      *
      * @param dom YANG Instance Identifier
      * @return Binding Instance Identifier
      */
     InstanceIdentifier<?> fromYangInstanceIdentifier(YangInstanceIdentifier dom);

     /**
      * Translates supplied Binding Instance Identifier and data into NormalizedNode representation.
      *
      * @param path Binding Instance Identifier pointing to data
      * @param data Data object representing data
      * @return NormalizedNode representation
      */
     <T extends DataObject> Entry<YangInstanceIdentifier,NormalizedNode<?,?>> toNormalizedNode(InstanceIdentifier<T> path, T data);

     /**
      * Translates supplied YANG Instance Identifier and NormalizedNode into Binding data.
      *
      * @param path Binding Instance Identifier
      * @param data NormalizedNode representing data
      * @return DOM Instance Identifier
      */
     Entry<InstanceIdentifier<?>,DataObject> fromNormalizedNode(YangInstanceIdentifier path, NormalizedNode<?, ?> data);

     /**
      * Returns map view which contains translated set of entries to normalized nodes.
      * Returned set will not contain representation of leaf nodes.
      *
      * @param dom Map of YANG Instance Identifier to Data
      * @return Map of Binding Instance Identifier to data.
      */
     Map<InstanceIdentifier<?>,DataObject> fromNormalizedNodes(Map<YangInstanceIdentifier,NormalizedNode<?,?>> dom);

}
