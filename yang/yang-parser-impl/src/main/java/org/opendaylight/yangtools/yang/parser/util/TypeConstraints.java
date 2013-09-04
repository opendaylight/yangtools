/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;

/**
 * Holder object for holding YANG type constraints.
 */
public final class TypeConstraints {
    private final String moduleName;
    private final int line;
    private final List<List<RangeConstraint>> ranges = new ArrayList<List<RangeConstraint>>();
    private final List<List<LengthConstraint>> lengths = new ArrayList<List<LengthConstraint>>();
    private final List<List<PatternConstraint>> patterns = new ArrayList<List<PatternConstraint>>();
    private final List<Integer> fractionDigits = new ArrayList<Integer>();

    public TypeConstraints(final String moduleName, final int line) {
        this.moduleName = moduleName;
        this.line = line;
    }

    List<List<RangeConstraint>> getAllRanges() {
        return ranges;
    }

    public List<RangeConstraint> getRange() {
        if (ranges.size() < 2) {
            return Collections.emptyList();
        }

        final List<RangeConstraint> resolved = ranges.get(0);
        RangeConstraint firstRange = resolved.get(0);
        RangeConstraint lastRange = resolved.get(resolved.size() - 1);
        Number min = firstRange.getMin();
        Number max = lastRange.getMax();

        if (!(min instanceof UnknownBoundaryNumber) && !(max instanceof UnknownBoundaryNumber)) {
            if (ranges.size() > 1) {
                validateRange(resolved);
            }
            return resolved;
        }

        if (firstRange.equals(lastRange)) {
            if (min instanceof UnknownBoundaryNumber) {
                min = resolveMinRange(min);
            }
            if (max instanceof UnknownBoundaryNumber) {
                max = resolveMaxRange(max);
            }
            firstRange = BaseConstraints.rangeConstraint(min, max, firstRange.getDescription(),
                    firstRange.getReference());
            resolved.set(0, firstRange);
            lastRange = BaseConstraints.rangeConstraint(min, max, lastRange.getDescription(), lastRange.getReference());
            resolved.set(resolved.size() - 1, lastRange);
        } else {
            if (min instanceof UnknownBoundaryNumber) {
                min = resolveMinRange(min);
                firstRange = BaseConstraints.rangeConstraint(min, firstRange.getMax(), firstRange.getDescription(),
                        firstRange.getReference());
                resolved.set(0, firstRange);
            }
            if (max instanceof UnknownBoundaryNumber) {
                max = resolveMaxRange(max);
                lastRange = BaseConstraints.rangeConstraint(lastRange.getMin(), max, lastRange.getDescription(),
                        lastRange.getReference());
                resolved.set(resolved.size() - 1, lastRange);
            }
        }
        if (this.ranges.size() > 1) {
            validateRange(resolved);
        }
        return resolved;
    }

    private Number resolveMinRange(final Number min) {
        int i = 1;
        Number newMin = min;
        while (newMin instanceof UnknownBoundaryNumber) {
            final List<RangeConstraint> act = ranges.get(i);
            newMin = act.get(0).getMin();
            i++;
        }
        return newMin;
    }

    private Number resolveMaxRange(final Number max) {
        int i = 1;
        Number newMax = max;
        while (newMax instanceof UnknownBoundaryNumber) {
            final List<RangeConstraint> act = ranges.get(i);
            newMax = act.get(act.size() - 1).getMax();
            i++;
        }
        return newMax;
    }

    public void addRanges(final List<RangeConstraint> ranges) {
        if (ranges != null && !(ranges.isEmpty())) {
            this.ranges.add(ranges);
        }
    }

    public List<List<LengthConstraint>> getAllLengths() {
        return lengths;
    }

    public List<LengthConstraint> getLength() {
        if (lengths.size() < 2) {
            return Collections.emptyList();
        }

        final List<LengthConstraint> resolved = lengths.get(0);
        LengthConstraint firstLength = resolved.get(0);
        LengthConstraint lastLength = resolved.get(resolved.size() - 1);
        Number min = firstLength.getMin();
        Number max = lastLength.getMax();

        if (!(min instanceof UnknownBoundaryNumber) && !(max instanceof UnknownBoundaryNumber)) {
            if (lengths.size() > 1) {
                validateLength(resolved);
            }
            return resolved;
        }

        if (firstLength.equals(lastLength)) {
            if (min instanceof UnknownBoundaryNumber) {
                min = resolveMinLength(min);
            }
            if (max instanceof UnknownBoundaryNumber) {
                max = resolveMaxLength(max);
            }
            firstLength = BaseConstraints.lengthConstraint(min, max, firstLength.getDescription(),
                    firstLength.getReference());
            resolved.set(0, firstLength);
            lastLength = BaseConstraints.lengthConstraint(min, max, lastLength.getDescription(),
                    lastLength.getReference());
            resolved.set(resolved.size() - 1, lastLength);
        } else {
            if (min instanceof UnknownBoundaryNumber) {
                min = resolveMinLength(min);
                firstLength = BaseConstraints.lengthConstraint(min, firstLength.getMax(), firstLength.getDescription(),
                        firstLength.getReference());
                resolved.set(0, firstLength);
            }
            if (max instanceof UnknownBoundaryNumber) {
                max = resolveMaxLength(max);
                lastLength = BaseConstraints.lengthConstraint(lastLength.getMin(), max, lastLength.getDescription(),
                        lastLength.getReference());
                resolved.set(resolved.size() - 1, lastLength);
            }
        }

        if (lengths.size() > 1) {
            validateLength(resolved);
        }
        return resolved;
    }

    private Number resolveMinLength(final Number min) {
        int i = 1;
        Number newMin = min;
        while (newMin instanceof UnknownBoundaryNumber) {
            final List<LengthConstraint> act = lengths.get(i);
            newMin = act.get(0).getMin();
            i++;
        }
        return newMin;
    }

    private Number resolveMaxLength(final Number max) {
        int i = 1;
        Number newMax = max;
        while (newMax instanceof UnknownBoundaryNumber) {
            final List<LengthConstraint> act = lengths.get(i);
            newMax = act.get(act.size() - 1).getMax();
            i++;
        }
        return newMax;
    }

    public void addLengths(final List<LengthConstraint> lengths) {
        if (lengths != null && !(lengths.isEmpty())) {
            this.lengths.add(lengths);
        }
    }

    public List<PatternConstraint> getPatterns() {
        if (patterns.isEmpty()) {
            return Collections.emptyList();
        }
        return patterns.get(0);
    }

    public void addPatterns(final List<PatternConstraint> patterns) {
        this.patterns.add(patterns);
    }

    public Integer getFractionDigits() {
        if (fractionDigits.isEmpty()) {
            return null;
        }
        return fractionDigits.get(0);
    }

    public void addFractionDigits(final Integer fractionDigits) {
        this.fractionDigits.add(fractionDigits);
    }

    public void validateConstraints() {
        validateLength();
        validateRange();
    }

    private void validateRange() {
        validateRange(getRange());
    }

    private void validateRange(List<RangeConstraint> typeRange) {
        if (ranges.size() < 2) {
            return;
        }

        for (RangeConstraint range : typeRange) {
            if (range.getMin() instanceof UnknownBoundaryNumber || range.getMax() instanceof UnknownBoundaryNumber) {
                throw new YangParseException(moduleName, line, "Unresolved range constraints");
            }
            final BigDecimal min = new BigDecimal(range.getMin().toString());
            final BigDecimal max = new BigDecimal(range.getMax().toString());

            List<RangeConstraint> parentRanges = ranges.get(1);
            if (!areRangesSubintervalsOfParentRanges(parentRanges, min, max)) {
                throw new YangParseException(moduleName, line, "Invalid range constraint: <" + min + ", " + max
                        + "> (parent: " + parentRanges + ").");
            }
        }
    }

    private boolean areRangesSubintervalsOfParentRanges(List<RangeConstraint> parentRanges, final BigDecimal min,
            final BigDecimal max) {
        boolean check = false;
        for (RangeConstraint r : parentRanges) {
            Number parentMinNumber = r.getMin();
            if (parentMinNumber instanceof UnknownBoundaryNumber) {
                parentMinNumber = resolveMinRange(parentMinNumber);
            }
            BigDecimal parentMin = new BigDecimal(parentMinNumber.toString());

            Number parentMaxNumber = r.getMax();
            if (parentMaxNumber instanceof UnknownBoundaryNumber) {
                parentMaxNumber = resolveMaxRange(parentMaxNumber);
            }
            BigDecimal parentMax = new BigDecimal(parentMaxNumber.toString());

            if (parentMin.compareTo(min) <= 0 && parentMax.compareTo(max) >= 0) {
                check = true;
                break;
            }
        }
        return check;
    }

    private void validateLength() {
        validateLength(getLength());
    }

    private void validateLength(List<LengthConstraint> typeLength) {
        if (lengths.size() < 2) {
            return;
        }

        for (LengthConstraint length : typeLength) {
            if (length.getMin() instanceof UnknownBoundaryNumber || length.getMax() instanceof UnknownBoundaryNumber) {
                throw new YangParseException(moduleName, line, "Unresolved length constraints");
            }
            final long min = length.getMin().longValue();
            final long max = length.getMax().longValue();

            List<LengthConstraint> parentLengths = lengths.get(1);
            if (!areLengthRangesSubintervalsOfParentLengthRanges(parentLengths, min, max)) {
                throw new YangParseException(moduleName, line, "Invalid length constraint: <" + min + ", " + max
                        + "> (parent: " + parentLengths + ").");
            }
        }
    }

    private boolean areLengthRangesSubintervalsOfParentLengthRanges(final List<LengthConstraint> parentLengths,
            final long min, final long max) {
        boolean check = false;
        for (LengthConstraint lc : parentLengths) {
            Number parentMinNumber = lc.getMin();
            if (parentMinNumber instanceof UnknownBoundaryNumber) {
                parentMinNumber = resolveMinLength(parentMinNumber);
            }
            long parentMin = parentMinNumber.longValue();

            Number parentMaxNumber = lc.getMax();
            if (parentMaxNumber instanceof UnknownBoundaryNumber) {
                parentMaxNumber = resolveMaxLength(parentMaxNumber);
            }
            long parentMax = parentMaxNumber.longValue();

            if (parentMin <= min && parentMax >= max) {
                check = true;
                break;
            }
        }
        return check;
    }

}
