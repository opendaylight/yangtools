/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.io.Serializable;
import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

/**
 * The <code>helper</code> implementation of Instance Rewision Aware XPath
 * interface.
 *
 * @see RevisionAwareXPath
 */
public class RevisionAwareXPathImpl implements RevisionAwareXPath, Serializable {
    private static final long serialVersionUID = 1L;

    private final String xpath;
    private final boolean absolute;

    private static final int HASH_BOOLEAN_TRUE = 1231;
    private static final int HASH_BOOLEAN_FALSE = 1237;

    public RevisionAwareXPathImpl(final String xpath, final boolean absolute) {
        this.xpath = xpath;
        this.absolute = absolute;
    }

    @Override
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(xpath);
        result = prime * result + (absolute ? HASH_BOOLEAN_TRUE : HASH_BOOLEAN_FALSE);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RevisionAwareXPathImpl other = (RevisionAwareXPathImpl) obj;
        return absolute == other.absolute && Objects.equals(xpath, other.xpath);
    }

    @Override
    public String toString() {
        return xpath;
    }
}
