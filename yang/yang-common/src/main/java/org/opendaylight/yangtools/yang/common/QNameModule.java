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

final class QNameModule implements Immutable, Serializable {
    private static final ObjectCache CACHE = ObjectCacheFactory.getObjectCache(QNameModule.class);
    private static final Logger LOG = LoggerFactory.getLogger(QNameModule.class);
    private static final long serialVersionUID = 1L;

    //Nullable
    private final URI namespace;

    //Nullable
    private final Date revision;

    //Nullable
    private final String formattedRevision;

    private QNameModule(final URI namespace, final Date revision) {
        this.namespace = namespace;
        this.revision = revision;
        if(revision != null) {
            this.formattedRevision = SimpleDateFormatUtil.getRevisionFormat().format(revision);
        } else {
            this.formattedRevision = null;
        }
    }

    public static QNameModule create(final URI namespace, final Date revision) {
        return CACHE.getReference(new QNameModule(namespace, revision));
    }

    public String getFormattedRevision() {
        return formattedRevision;
    }

    public URI getNamespace() {
        return namespace;
    }

    public Date getRevision() {
        return revision;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        result = prime * result + ((formattedRevision == null) ? 0 : formattedRevision.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QNameModule)) {
            return false;
        }
        final QNameModule other = (QNameModule) obj;
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (!namespace.equals(other.namespace)) {
            return false;
        }
        if (formattedRevision == null) {
            if (other.formattedRevision != null) {
                return false;
            }
        } else if (!revision.equals(other.revision)) {
            return false;
        }
        return true;
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
    URI getRevisionNamespace() {

        if (namespace == null) {
            return null;
        }

        String query = "";
        if (revision != null) {
            query = "revision=" + formattedRevision;
        }

        URI compositeURI = null;
        try {
            compositeURI = new URI(namespace.getScheme(), namespace.getUserInfo(), namespace.getHost(),
                    namespace.getPort(), namespace.getPath(), query, namespace.getFragment());
        } catch (URISyntaxException e) {
            LOG.error("", e);
        }
        return compositeURI;
    }
}
