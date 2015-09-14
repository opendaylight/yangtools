/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.util.List;
import java.util.Set;

/**
 * @deprecated There is no way an object can satisfy both the {@link Set} and {@link List} interface contracts, as
 *             they contradict on hashCode() specification. See {@link Set#hashCode()} and {@link List#hashCode()}.
 *             This class interface will be removed in a future release.
 *
 * @param <E> Element type
 */
@Deprecated
public interface OrderedSet<E> extends Set<E>, List<E> {

}
