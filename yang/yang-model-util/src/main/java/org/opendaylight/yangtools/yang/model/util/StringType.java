/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

/**
 * The <code>default</code> implementation of String Type Definition interface.
 *
 * @see StringTypeDefinition
 */
public final class StringType implements StringTypeDefinition {
    private static final StringType INSTANCE = new StringType();
    private final QName name = BaseTypes.constructQName("string");
    private final SchemaPath path = new SchemaPath(Collections.singletonList(name), true);
    private static final String DEFAULT_VALUE = "";
    private static final String DESCRIPTION = "";
    private static final String REFERENCE = "";
    private final List<LengthConstraint> lengthStatements;
    private final List<PatternConstraint> patterns;
    private static final String UNITS = "";

    /**
     * Default Constructor.
     */
    private StringType() {
        final List<LengthConstraint> constraints = new ArrayList<LengthConstraint>();
        constraints.add(BaseConstraints.lengthConstraint(0, Long.MAX_VALUE, "", ""));
        lengthStatements = Collections.unmodifiableList(constraints);
        patterns = Collections.emptyList();
    }

    public static StringType getInstance() {
        return INSTANCE;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.TypeDefinition#getBaseType()
     */
    @Override
    public StringTypeDefinition getBaseType() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.TypeDefinition#getUnits()
     */
    @Override
    public String getUnits() {
        return UNITS;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.TypeDefinition#getDefaultValue
     * ()
     */
    @Override
    public Object getDefaultValue() {
        return DEFAULT_VALUE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getQName()
     */
    @Override
    public QName getQName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getPath()
     */
    @Override
    public SchemaPath getPath() {
        return path;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.SchemaNode#getDescription()
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getReference()
     */
    @Override
    public String getReference() {
        return REFERENCE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getStatus()
     */
    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.csico.yang.model.base.type.api.StringTypeDefinition#getLengthStatements
     * ()
     */
    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return lengthStatements;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.csico.yang.model.base.type.api.StringTypeDefinition#getPatterns()
     */
    @Override
    public List<PatternConstraint> getPatternConstraints() {
        return patterns;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lengthStatements == null) ? 0 : lengthStatements.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((patterns == null) ? 0 : patterns.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StringType other = (StringType) obj;
        if (lengthStatements == null) {
            if (other.lengthStatements != null) {
                return false;
            }
        } else if (!lengthStatements.equals(other.lengthStatements)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.getPath().equals(other.path.getPath())) {
            return false;
        }
        if (patterns == null) {
            if (other.patterns != null) {
                return false;
            }
        } else if (!patterns.equals(other.patterns)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StringType [name=");
        builder.append(name);
        builder.append(", path=");
        builder.append(path);
        builder.append(", defaultValue=");
        builder.append(DEFAULT_VALUE);
        builder.append(", description=");
        builder.append(DESCRIPTION);
        builder.append(", reference=");
        builder.append(REFERENCE);
        builder.append(", lengthStatements=");
        builder.append(lengthStatements);
        builder.append(", patterns=");
        builder.append(patterns);
        builder.append(", units=");
        builder.append(UNITS);
        builder.append("]");
        return builder.toString();
    }
}
