/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

/**
 * Marker interface for nodes, which are mixins - their content belongs to parent node and in serialized form this node
 * does not exist, but it's children are present.
 *
 * @deprecated This interface assumes XML encoding. In JSON encoding only a {@link ChoiceNode} is a mixin. Users are
 *             advised to move to explicit checking.
 */
@Deprecated(since = "11.0.0", forRemoval = true)
public sealed interface MixinNode permits ChoiceNode, LeafSetNode, MapNode, UnkeyedListNode {

}
