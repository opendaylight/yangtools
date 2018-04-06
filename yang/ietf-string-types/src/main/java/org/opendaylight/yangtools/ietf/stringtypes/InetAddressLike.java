/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import com.google.common.annotations.Beta;
import java.net.InetAddress;
import org.eclipse.jdt.annotation.NonNullByDefault;

@Beta
@NonNullByDefault
public interface InetAddressLike {

    InetAddress toInetAddress();
}
