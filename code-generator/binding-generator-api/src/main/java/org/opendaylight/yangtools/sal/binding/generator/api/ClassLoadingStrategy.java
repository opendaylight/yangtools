/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.api;

import org.opendaylight.yangtools.sal.binding.model.api.Type;

public interface ClassLoadingStrategy {

    Class<?> loadClass(Type type) throws ClassNotFoundException;

    Class<?> loadClass(String fullyQualifiedName) throws ClassNotFoundException;

}
