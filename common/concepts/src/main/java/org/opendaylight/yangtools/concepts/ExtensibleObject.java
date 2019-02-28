/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Interface specifying access to extensions attached to a particular object. This functionality is loosely based on
 * <a href="https://docs.microsoft.com/en-us/dotnet/framework/wcf/extending/extensible-objects">Extensible Object</a>
 * pattern.
 *
 * @param <T> Type of extensible object
 * @param <E> Extension marker interface
 * @author Robert Varga
 */
@Beta
public interface ExtensibleObject<T extends ExtensibleObject<T, E>, E extends ObjectExtension<T, E>> {
    /**
     * Return a map of currently-supported extensions, along with accessor objects which provide access to the specific
     * functionality bound to this object.
     *
     * @return A map of supported functionality.
     */
    default @NonNull ClassToInstanceMap<E> getExtensions() {
        return ImmutableClassToInstanceMap.of();
    }
}
