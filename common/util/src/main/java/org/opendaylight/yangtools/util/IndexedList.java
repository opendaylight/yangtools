/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Identifier;

@Beta
public interface IndexedList<I extends Identifier, T extends Identifiable<I>> extends List<T> {

    int getItemOffset(@NonNull I identifier);

    @Nullable T getItem(@NonNull I identifier);
}
