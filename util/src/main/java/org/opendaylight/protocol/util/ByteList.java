/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DTO class to avoid class cast warnings.
 */
public class ByteList {

	private final List<byte[]> bytes;

	/**
	 * Creates an empty list of byte arrays.
	 */
	public ByteList() {
		this.bytes = new ArrayList<byte[]>();
	}

	/**
	 * Returns underlying list of byte arrays
	 * 
	 * @return underlying list of byte arrays
	 */
	public List<byte[]> getBytes() {
		return this.bytes;
	}

	/**
	 * Adds byte array to underlying list
	 * 
	 * @param value byte array
	 */
	public void add(byte[] value) {
		this.bytes.add(value);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.bytes == null) ? 0 : this.bytes.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ByteList)) {
			return false;
		}
		final ByteList other = (ByteList) obj;
		if (this.bytes == null) {
			if (other.bytes != null) {
				return false;
			}
		} else if (this.bytes.size() != other.bytes.size()) {
			return false;
		} else {
			for (int i = 0; i < this.bytes.size(); i++) {
				if (!Arrays.equals(this.bytes.get(i), other.bytes.get(i))) {
					return false;
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ByteList [bytes=");
		for (final byte[] b : this.bytes) {
			builder.append(Arrays.toString(b));
		}
		builder.append("]");
		return builder.toString();
	}
}
