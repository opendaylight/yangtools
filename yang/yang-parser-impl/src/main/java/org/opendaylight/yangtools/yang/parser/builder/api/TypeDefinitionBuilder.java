/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * Interface for builders of 'typedef' statement.
 */
public interface TypeDefinitionBuilder extends TypeAwareBuilder, SchemaNodeBuilder, GroupingMember {

    /**
     * Sets QName for resulting type definition.
     *
     * @param qname QName of resulting type
     */
    void setQName(QName qname);

    @Override
    TypeDefinition<?> build();

    /**
     *
     * Returns range restrictions of resulting type definition.
     *
     * @return range restrictions of resulting type definition.
     */
    List<RangeConstraint> getRanges();

    /**
     * Set Range restrictions for resulting type definition.
     *
     * @param ranges
     *            Range restrictions of resulting type definition.
     */
    void setRanges(List<RangeConstraint> ranges);

    /**
     *
     * Returns length restrictions of resulting type definition.
     *
     * @return length restrictions of resulting type definition.
     */
    List<LengthConstraint> getLengths();

    /**
     * Set length restrictions for resulting type definition.
     *
     * @param lengths
     *            Length restrictions of resulting type definition.
     */
    void setLengths(List<LengthConstraint> lengths);

    /**
     *
     * Returns pattern restrictions of resulting type definition.
     *
     * @return range restrictions of resulting type definition.
     */
    List<PatternConstraint> getPatterns();

    /**
     * Set pattern restrictions for resulting type definition.
     *
     * @param patterns
     *            patterns restrictions of resulting type definition.
     */
    void setPatterns(List<PatternConstraint> patterns);

    /**
     *
     * Returns fractions digits of resulting type if it is derived
     * from <code>decimal</code> built-in type.
     *
     * @return fractions digits of resulting type
     */
    Integer getFractionDigits();

    /**
     * Sets fractions digits of resulting type if it is derived from
     * <code>decimal</code> built-in type.
     *
     * @param fractionDigits fraction digits
     */
    void setFractionDigits(Integer fractionDigits);

    /**
     *
     * Returns default value of resulting type
     *
     * @return default value of resulting type
     */
    Object getDefaultValue();

    /**
     *
     * Sets default value of resulting type
     *
     * @param defaultValue Default value of resulting type
     */
    void setDefaultValue(Object defaultValue);

    /**
     * Gets unit definition for resulting type
     *
     * @return unit definition for resulting type
     */
    String getUnits();

    /**
     * Sets units definition for resulting type
     *
     * @param units units definition for resulting type
     */
    void setUnits(String units);

}
