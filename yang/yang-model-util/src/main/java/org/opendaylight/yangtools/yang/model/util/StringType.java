/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
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
public final class StringType implements StringTypeDefinition, Immutable {
    private static final QName NAME = BaseTypes.STRING_QNAME;
    private static final SchemaPath PATH = SchemaPath.create(true, NAME);
    private static final String DEFAULT_VALUE = null;
    private static final String DESCRIPTION = "";
    private static final String REFERENCE = "";
    private final List<LengthConstraint> lengthStatements;
    private final List<PatternConstraint> patterns;
    private static final String UNITS = "";

    private static final StringType INSTANCE = new StringType();

    /**
     * Default Constructor.
     */
    private StringType() {
        lengthStatements = Collections.singletonList(BaseConstraints.newLengthConstraint(0, Integer.MAX_VALUE, Optional.of(""), Optional.of("")));
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
        return NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getPath()
     */
    @Override
    public SchemaPath getPath() {
        return PATH;
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
        result = prime * result + Objects.hashCode(lengthStatements);
        result = prime * result + NAME.hashCode();
        result = prime * result + PATH.hashCode();
        result = prime * result + Objects.hashCode(patterns);
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
        StringType other = (StringType) obj;
        return Objects.equals(lengthStatements, other.lengthStatements) && Objects.equals(patterns, other.patterns);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StringType [name=");
        builder.append(NAME);
        builder.append(", path=");
        builder.append(PATH);
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
