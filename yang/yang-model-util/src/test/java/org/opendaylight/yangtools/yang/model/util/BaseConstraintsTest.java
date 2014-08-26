package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

import static org.junit.Assert.*;

public class BaseConstraintsTest {

    @Test
    public void canCreateConstraints() {
        Number min = 5;
        Number max = 99;
        String description = "Any description";
        String reference = "any_ref";
        String reg_exp = "x|z";
        Optional<String> desc = Optional.of(description);
        Optional<String> ref = Optional.of(reference);

        LengthConstraint lengthCons = BaseConstraints.newLengthConstraint(min, max, desc, ref);

        assertEquals("LengthConstraints Get min", min, lengthCons.getMin());
        assertEquals("LengthConstraints Get max", max, lengthCons.getMax());
        assertEquals("LengthConstraints Get description", description, lengthCons.getDescription());
        assertEquals("LengthConstraints Get reference", reference, lengthCons.getReference());

        // covered already
        //RangeConstraint rangeCons = BaseConstraints.newRangeConstraint(min, max, desc, ref);

        //assertEquals("RangeConstraints Get min", min, rangeCons.getMin());
        //assertEquals("RangeConstraints Get max", max, rangeCons.getMax());
        //assertEquals("RangeConstraints Get description", description, rangeCons.getDescription());
        //assertEquals("RangeConstraints Get reference", reference, rangeCons.getReference());

        PatternConstraint patternCons = BaseConstraints.newPatternConstraint(reg_exp, desc, ref);

        assertEquals("PatternConstraints Get regex", reg_exp, patternCons.getRegularExpression());
        assertEquals("PatternConstraints Get description", description, patternCons.getDescription());
        assertEquals("PatternConstraints Get reference", reference, patternCons.getReference());
    }
}