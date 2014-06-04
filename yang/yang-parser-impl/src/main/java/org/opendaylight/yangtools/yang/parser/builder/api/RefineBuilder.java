package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.List;

import org.opendaylight.yangtools.yang.model.api.MustDefinition;

public interface RefineBuilder {

    String getDescription();

    void setDescription(String description);

    String getReference();

    void setReference(String reference);

    Boolean isConfiguration();

    void setConfiguration(Boolean config);

    Boolean isMandatory();

    void setMandatory(Boolean mandatory);

    Boolean isPresence();

    void setPresence(Boolean presence);

    MustDefinition getMust();

    void setMust(MustDefinition must);

    Integer getMinElements();

    void setMinElements(Integer minElements);

    Integer getMaxElements();

    void setMaxElements(Integer maxElements);

    String getName();

    String getModuleName();

    int getLine();

    List<UnknownSchemaNodeBuilder> getUnknownNodes();

    String getDefaultStr();

}