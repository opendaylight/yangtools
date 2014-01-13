/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.base.Optional;

public final class SourceIdentifier {

    private final String name;
    private final String revision;

    public SourceIdentifier(String name, Optional<String> formattedRevision) {
        super();
        this.name = name;
        this.revision = formattedRevision.orNull();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((revision == null) ? 0 : revision.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SourceIdentifier other = (SourceIdentifier) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (revision == null) {
            if (other.revision != null)
                return false;
        } else if (!revision.equals(other.revision))
            return false;
        return true;
    }

    public String getName() {
        return name;
    }

    public String getRevision() {
        return revision;
    }

    public static SourceIdentifier create(String moduleName, Optional<String> revision) {
        return new SourceIdentifier(moduleName, revision);
    }
    
    public String toYangFilename() {
        return toYangFileName(name, Optional.fromNullable(revision));
    }
    
    @Override
    public String toString() {
        return "SourceIdentifier [name=" + name + "@" + revision + "]";
    }

    public static final String toYangFileName(String moduleName, Optional<String> revision) {
        StringBuilder filename = new StringBuilder(moduleName);
        if (revision.isPresent()) {
            filename.append("@");
            filename.append(revision.get());
        }
        filename.append(".yang");
        return filename.toString();
    }

}