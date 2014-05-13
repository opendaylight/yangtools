/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.objcache.ObjectCache;
import org.opendaylight.yangtools.objcache.ObjectCacheFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class QNameModule implements Comparable<QNameModule>, Immutable, Serializable {
    private static final ObjectCache CACHE = ObjectCacheFactory.getObjectCache(QNameModule.class);
    private static final Logger LOG = LoggerFactory.getLogger(QNameModule.class);

    //Nullable
    private final URI namespace;

    //Nullable
    private final Date revision;

    //Nullable
    private transient String formattedRevision;

    private QNameModule(final URI namespace, final Date revision) {
        this.namespace = namespace;
        this.revision = revision;
    }

    public static QNameModule create(final URI namespace, final Date revision) {
        return CACHE.getReference(new QNameModule(namespace, revision));
    }

    public URI getNamespace() {
        return namespace;
    }

    public Date getRevision() {
        return revision;
    }

    public String getFormattedRevision() {
        if (formattedRevision != null) {
            return formattedRevision;
        }
        if (revision == null) {
            return null;
        }

        synchronized (this) {
            if (formattedRevision == null) {
                formattedRevision = SimpleDateFormatUtil.getRevisionFormat().format(revision);
            }
        }

        return formattedRevision;
    }

    public boolean isEqualWithoutRevision(final QNameModule other) {
        if (namespace == other.namespace) {
            return true;
        }
        if (namespace == null) {
            // ... and the other one is not...
            return false;
        }

        return namespace.equals(other.namespace);
    }

    /**
     * Returns a namespace in form defined by section 5.6.4. of {@link https
     * ://tools.ietf.org/html/rfc6020}, if namespace is not correctly defined,
     * the method will return <code>null</code> <br>
     * example "http://example.acme.com/system?revision=2008-04-01"
     *
     * @return namespace in form defined by section 5.6.4. of {@link https
     *         ://tools.ietf.org/html/rfc6020}, if namespace is not correctly
     *         defined, the method will return <code>null</code>
     *
     */
    public URI getRevisionNameSpace() {
        if (namespace == null) {
            return null;
        }

        String query = "";
        if (revision != null) {
            query = "revision=" + getFormattedRevision();
        }

        URI compositeURI = null;
        try {
            compositeURI = new URI(namespace.getScheme(), namespace.getUserInfo(), namespace.getHost(),
                    namespace.getPort(), namespace.getPath(), query, namespace.getFragment());
        } catch (URISyntaxException e) {
            LOG.error("Failed to compose URI for module {}", this, e);
        }
        return compositeURI;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((namespace == null) ? 0 : namespace.hashCode());
        result = prime * result
                + ((revision == null) ? 0 : revision.hashCode());
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
        QNameModule other = (QNameModule) obj;
        if (!isEqualWithoutRevision(other)) {
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
    public int compareTo(final QNameModule other) {
        // Fast check
        if (this == other) {
            return 0;
        }

        // compare nullable namespace parameter
        if (namespace == null) {
            if (other.namespace != null) {
                return -1;
            }
        } else {
            if (other.namespace == null) {
                return 1;
            }
            int result = namespace.compareTo(other.namespace);
            if (result != 0) {
                return result;
            }
        }

        // compare nullable revision parameter
        if (revision == null) {
            if (other.revision != null) {
                return -1;
            }
        } else {
            if (other.revision == null) {
                return 1;
            }
            int result = revision.compareTo(other.revision);
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

}
