/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Node representing data instance of <code>choice</code>.
 *
 * Choice node is instance of one of possible alternatives, from which only one is allowed to exist at one time in
 * particular context of parent node.
 *
 * YANG Model and schema for choice is described by instance of
 * {@link org.opendaylight.yangtools.yang.model.api.ChoiceNode}.
 *
 * Valid alternatives of subtree are described by instances of
 * {@link org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode}, which are retrieved via
 * {@link org.opendaylight.yangtools.yang.model.api.ChoiceNode#getCases()}.
 */
public interface ChoiceNode extends //
        MixinNode, //
        DataContainerNode<NodeIdentifier>,
        DataContainerChild<NodeIdentifier, Collection<DataContainerChild<? extends PathArgument, ?>>> {

}
