/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.util.Set;

/**
 * @deprecated Unused undocumented concept. Scheduled for removal.
 */
@Deprecated
public interface Namespace<K,V> {

    V get(K key);

    Namespace<K,V> getParent();

    Set<Namespace<K,V>> getSubnamespaces();

    Namespace<K,V> getSubnamespace(V key);
}
