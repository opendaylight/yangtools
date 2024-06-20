/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib.test.mock;

import org.opendaylight.yangtools.binding.lib.Augmentable;
import org.opendaylight.yangtools.binding.lib.ChildOf;
import org.opendaylight.yangtools.binding.lib.DataObject;
import org.opendaylight.yangtools.binding.lib.KeyAware;

public interface Node extends
    DataObject,
    KeyAware<NodeKey>,
    ChildOf<Nodes>,
    Augmentable<Node> {

}
