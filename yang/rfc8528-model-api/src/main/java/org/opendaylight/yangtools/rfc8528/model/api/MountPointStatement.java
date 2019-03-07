/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement representation of 'mount-point' extension defined in
 * <a href="https://tools.ietf.org/html/rfc8528">RFC8528</a>.
 */
@Beta
public interface MountPointStatement extends UnknownStatement<QName> {

}
