/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.objcache.ObjectCache;
import org.opendaylight.yangtools.objcache.ObjectCacheFactory;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * YANG Schema source identifier
 *
 * Simple transfer object represents identifier of source for YANG schema (module or submodule),
 * which consists of
 * <ul>
 * <li>YANG schema name ({@link #getName()}
 * <li>Module revision (optional) ({link {@link #getRevision()})
 * </ul>
 *
 * Source identifier is designated to be carry only necessary information
 * to look-up YANG model source and to be used by various SchemaSourceProviders.
 *
 * <b>Note:</b>On source retrieval layer it is impossible to distinguish
 * between YANG module and/or submodule unless source is present.
 *
 * <p>
 * (For further reference see: http://tools.ietf.org/html/rfc6020#section-5.2 and
 * http://tools.ietf.org/html/rfc6022#section-3.1 ).
 */
@Beta
public final class SemVerSourceIdentifier extends SourceIdentifier {
   private static final ObjectCache CACHE = ObjectCacheFactory.getObjectCache(SemVerSourceIdentifier.class);
    private static final long serialVersionUID = 1L;
    private final SemVer semVer;

    /**
     * Creates new YANG Schema source identifier.
     *
     * @param name Name of schema
     * @param formattedRevision Revision of source in format YYYY-mm-dd
     * @param semVer semantic version of source
     */
    public SemVerSourceIdentifier(final String name, final String formattedRevision, final SemVer semVer) {
        super(name, formattedRevision);
        this.semVer = semVer == null ? Module.DEFAULT_SEMANTIC_VERSION : semVer;
    }

    /**
     * Creates new YANG Schema source identifier.
     *
     * @param name Name of schema
     * @param semVer semantic version of source
     */
    public SemVerSourceIdentifier(String name, SemVer semVer) {
        super(name, NOT_PRESENT_FORMATTED_REVISION);
        this.semVer = semVer == null ? Module.DEFAULT_SEMANTIC_VERSION : semVer;
    }

    /**
     * Return a cached reference to an object equal to this object.
     *
     * @return A potentially shared reference, not guaranteed to be unique.
     */
    public SemVerSourceIdentifier cachedReference() {
        return CACHE.getReference(this);
    }

    /**
     * Returns semantic version of source or {@link Module#DEFAULT_SEMANTIC_VERSION} if semantic version was not supplied.
     *
     * @return revision of source or {@link Module#DEFAULT_SEMANTIC_VERSION} if revision was not supplied.
     */
    public SemVer getSemanticVersion() {
        return semVer;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(semVer);
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
        SemVerSourceIdentifier other = (SemVerSourceIdentifier) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(semVer, other.semVer);
    }

    public static SemVerSourceIdentifier create(final String moduleName, final Optional<String> revision, final SemVer semVer) {
        return new SemVerSourceIdentifier(moduleName, revision.or(NOT_PRESENT_FORMATTED_REVISION), semVer);
    }

    @Override
    public String toString() {
        return "SemVerSourceIdentifier [name=" + getName() + "(" + semVer + ")" + "@" + getRevision() + "]";
    }
}
