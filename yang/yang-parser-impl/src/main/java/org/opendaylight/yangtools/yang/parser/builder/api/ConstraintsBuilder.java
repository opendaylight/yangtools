package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;

public interface ConstraintsBuilder {

    ConstraintDefinition build();

    String getModuleName();

    int getLine();

    Integer getMinElements();

    void setMinElements(Integer minElements);

    Integer getMaxElements();

    void setMaxElements(Integer maxElements);

    Set<MustDefinition> getMustDefinitions();

    void addMustDefinition(MustDefinition must);

    String getWhenCondition();

    void addWhenCondition(String whenCondition);

    boolean isMandatory();

    void setMandatory(boolean mandatory);

}