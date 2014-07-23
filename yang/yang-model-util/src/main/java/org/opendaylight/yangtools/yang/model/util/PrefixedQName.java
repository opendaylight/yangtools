/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * The PrefixedQName from XML consists of local name of element and XML
 * namespace, but for our use, we added module revision to it.
 *
 * In YANG context PrefixedQName is full name of defined node, type, procedure
 * or notification. PrefixedQName consists of XML namespace, YANG model revision
 * and local name of defined type. It is used to prevent name clashes between
 * nodes with same local name, but from different schemas.
 *
 * <ul>
 * <li><b>XMLNamespace</b> - {@link #getNamespace()} - the namespace assigned to
 * the YANG module which defined element, type, procedure or notification.</li>
 * <li><b>Revision</b> - {@link #getRevision()} - the revision of the YANG
 * module which describes the element</li>
 * <li><b>Prefix</b> - {@link #getPrefix()} - the prefix to namespace assigned
 * to the YANG module. Prefix does not affect equality and identity of two
 * PrefixedQNames</li>
 * <li><b>LocalName</b> - {@link #getLocalName()} - the YANG schema identifier
 * which were defined for this node in the YANG module</li>
 * </ul>
 *
 */
public final class PrefixedQName implements Immutable, Serializable, Comparable<PrefixedQName> {
    private static final long serialVersionUID = 5398411242927766414L;

    private static final char[] ILLEGAL_CHARACTERS = new char[] { '?', '(', ')', '&' };

    private QName qname;

    // Nullable
    private URI namespace;

    // Nullable
    private Date revision;

    // Nullable
    private final String prefix;

    // Mandatory
    private final String localName;

    public PrefixedQName(final String prefix, final String localName) {
        checkLocalName(localName);
        this.prefix = prefix;
        this.localName = localName;
    }

    public PrefixedQName(final URI namespace, final Date revision, final String localName) {
        this((String) null, localName);
        checkLocalName(localName);
        this.namespace = namespace;
        this.revision = revision;
    }

    public PrefixedQName(final QName qname) {
        this.localName = qname.getLocalName();
        checkLocalName(localName);
        this.namespace = qname.getNamespace();
        this.revision = qname.getRevision();
        this.prefix = null;
        this.qname = qname;
    }

    private static String checkLocalName(final String localName) {
        if (localName == null) {
            throw new IllegalArgumentException("Parameter 'localName' may not be null.");
        }
        if (localName.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'localName' must be a non-empty string.");
        }

        for (char c : ILLEGAL_CHARACTERS) {
            if (localName.indexOf(c) != -1) {
                throw new IllegalArgumentException(String.format(
                        "Parameter 'localName':'%s' contains illegal character '%s'", localName, c));
            }
        }
        return localName;
    }

    /**
     * Returns XMLNamespace assigned to the YANG module.
     *
     * @return XMLNamespace assigned to the YANG module.
     */
    public URI getNamespace() {
        return namespace;
    }

    public void setNamespace(final URI namespace) {
        this.namespace = namespace;
    }

    /**
     * Returns YANG schema identifier which were defined for this node in the
     * YANG module
     *
     * @return YANG schema identifier which were defined for this node in the
     *         YANG module
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Returns revision of the YANG module if the module has defined revision,
     * otherwise returns <code>null</code>
     *
     * @return revision of the YANG module if the module has defined revision,
     *         otherwise returns <code>null</code>
     */
    public Date getRevision() {
        return revision;
    }

    public void setRevision(final Date revision) {
        this.revision = revision;
    }

    /**
     * Returns locally defined prefix assigned to local name
     *
     * @return locally defined prefix assigned to local name
     */
    public String getPrefix() {
        return prefix;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((localName == null) ? 0 : localName.hashCode());
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        result = prime * result + ((revision == null) ? 0 : revision.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PrefixedQName)) {
            return false;
        }
        final PrefixedQName other = (PrefixedQName) obj;
        if (localName == null) {
            if (other.localName != null) {
                return false;
            }
        } else if (!localName.equals(other.localName)) {
            return false;
        }
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (!namespace.equals(other.namespace)) {
            return false;
        }
        if (revision == null) {
            if (other.revision != null) {
                return false;
            }
        } else if (!revision.equals(other.revision)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (prefix == null) {
            sb.append('(').append(namespace).append(')');
        } else {
            sb.append(prefix).append(':');
        }
        sb.append(localName);
        return sb.toString();
    }

    @Override
    public int compareTo(final PrefixedQName other) {
        // compare mandatory localName parameter
        int result = localName.compareTo(other.localName);
        if (result != 0) {
            return result;
        }

        // compare nullable namespace parameter
        if (getNamespace() == null) {
            if (other.getNamespace() != null) {
                return -1;
            }
        } else {
            if (other.getNamespace() == null) {
                return 1;
            }
            result = getNamespace().compareTo(other.getNamespace());
            if (result != 0) {
                return result;
            }
        }

        // compare nullable revision parameter
        if (getRevision() == null) {
            if (other.getRevision() != null) {
                return -1;
            }
        } else {
            if (other.getRevision() == null) {
                return 1;
            }
            result = getRevision().compareTo(other.getRevision());
            if (result != 0) {
                return result;
            }
        }

        return result;
    }

    public QName createQName() {
        if (qname != null) {
            return qname;
        }
        this.qname = QName.create(namespace, revision, localName);
        return qname;
    }

}
