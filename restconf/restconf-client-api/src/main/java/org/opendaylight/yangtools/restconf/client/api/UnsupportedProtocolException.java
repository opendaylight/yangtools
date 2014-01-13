/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api;

public class UnsupportedProtocolException extends Exception {
	private static final long serialVersionUID = 1L;

	public UnsupportedProtocolException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public UnsupportedProtocolException(final String message) {
		super(message);
	}
}
