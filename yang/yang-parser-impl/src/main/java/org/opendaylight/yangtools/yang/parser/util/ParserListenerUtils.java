/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import static com.google.common.base.Preconditions.checkState;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Argument_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Base_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Bit_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Bits_specificationContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Config_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Config_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Decimal64_specificationContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Default_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Description_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Enum_specificationContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Enum_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Error_app_tag_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Error_message_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Fraction_digits_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Identityref_specificationContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Instance_identifier_specificationContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Key_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Leafref_specificationContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Length_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Mandatory_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Mandatory_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Max_elements_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Max_value_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Min_elements_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Min_value_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Module_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Must_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Numerical_restrictionsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Ordered_by_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Ordered_by_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Path_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Pattern_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Position_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Presence_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Range_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Reference_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Refine_anyxml_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Refine_choice_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Refine_container_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Refine_leaf_list_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Refine_leaf_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Refine_list_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Refine_pomContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Refine_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Require_instance_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Require_instance_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Status_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Status_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.StringContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.String_restrictionsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Type_body_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Units_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Value_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.When_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Yin_element_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Yin_element_stmtContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.BinaryType;
import org.opendaylight.yangtools.yang.model.util.BitsType;
import org.opendaylight.yangtools.yang.model.util.Decimal64;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.util.Int16;
import org.opendaylight.yangtools.yang.model.util.Int32;
import org.opendaylight.yangtools.yang.model.util.Int64;
import org.opendaylight.yangtools.yang.model.util.Int8;
import org.opendaylight.yangtools.yang.model.util.Leafref;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.model.util.StringType;
import org.opendaylight.yangtools.yang.model.util.Uint16;
import org.opendaylight.yangtools.yang.model.util.Uint32;
import org.opendaylight.yangtools.yang.model.util.Uint64;
import org.opendaylight.yangtools.yang.model.util.Uint8;
import org.opendaylight.yangtools.yang.model.util.UnknownType;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public final class ParserListenerUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ParserListenerUtils.class);

    private ParserListenerUtils() {
    }

    /**
     * Parse given tree and get first string value.
     *
     * @param treeNode
     *            tree to parse
     * @return first string value from given tree
     */
    public static String stringFromNode(final ParseTree treeNode) {
        String result = "";
        for (int i = 0; i < treeNode.getChildCount(); ++i) {
            if (treeNode.getChild(i) instanceof StringContext) {
                final StringContext context = (StringContext) treeNode.getChild(i);
                if (context != null) {
                    return stringFromStringContext(context);

                }
            }
        }
        return result;
    }

    public static String stringFromStringContext(final StringContext context) {
        StringBuilder str = new StringBuilder();
        for (TerminalNode stringNode : context.STRING()) {
            String result = stringNode.getText();
            if(!result.contains("\"")){
                str.append(result);
            } else if (!(result.startsWith("\"")) && result.endsWith("\"")) {
                LOG.error("Syntax error in module {} at line {}: missing '\"'.", getParentModule(context),
                        context.getStart().getLine());
            } else {
                str.append(result.replace("\"", ""));
            }
        }
        return str.toString();
    }

    private static String getParentModule(final ParseTree ctx) {
        ParseTree current = ctx;
        while (current != null && !(current instanceof Module_stmtContext)) {
            current = current.getParent();
        }
        if (current instanceof Module_stmtContext) {
            Module_stmtContext module = (Module_stmtContext) current;
            for (int i = 0; i < module.getChildCount(); i++) {
                if (module.getChild(i) instanceof StringContext) {
                    final StringContext str = (StringContext) module.getChild(i);
                    return str.getChild(0).getText();
                }
            }
        }
        return "";
    }

    /**
     * Parse 'description', 'reference' and 'status' statements and fill in
     * given builder.
     *
     * @param ctx
     *            context to parse
     * @param builder
     *            builder to fill in with parsed statements
     */
    public static void parseSchemaNodeArgs(final ParseTree ctx, final SchemaNodeBuilder builder) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                final String desc = stringFromNode(child);
                builder.setDescription(desc);
            } else if (child instanceof Reference_stmtContext) {
                final String ref = stringFromNode(child);
                builder.setReference(ref);
            } else if (child instanceof Status_stmtContext) {
                final Status status = parseStatus((Status_stmtContext) child);
                builder.setStatus(status);
            }
        }
    }

    /**
     * Parse given context and return its value;
     *
     * @param ctx
     *            status context
     * @return value parsed from context
     */
    public static Status parseStatus(final Status_stmtContext ctx) {
        Status result = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree statusArg = ctx.getChild(i);
            if (statusArg instanceof Status_argContext) {
                String statusArgStr = stringFromNode(statusArg);
                switch (statusArgStr) {
                case "current":
                    result = Status.CURRENT;
                    break;
                case "deprecated":
                    result = Status.DEPRECATED;
                    break;
                case "obsolete":
                    result = Status.OBSOLETE;
                    break;
                default:
                    LOG.warn("Invalid 'status' statement: " + statusArgStr);
                }
            }
        }
        return result;
    }

    /**
     * Parse given tree and returns units statement as string.
     *
     * @param ctx
     *            context to parse
     * @return value of units statement as string or null if there is no units
     *         statement
     */
    public static String parseUnits(final ParseTree ctx) {
        String units = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Units_stmtContext) {
                units = stringFromNode(child);
                break;
            }
        }
        return units;
    }

    /**
     * Parse given tree and returns default statement as string.
     *
     * @param ctx
     *            context to parse
     * @return value of default statement as string or null if there is no
     *         default statement
     */
    public static String parseDefault(final ParseTree ctx) {
        String defaultValue = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Default_stmtContext) {
                defaultValue = stringFromNode(child);
                break;
            }
        }
        return defaultValue;
    }

    /**
     * Create SchemaPath from actualPath and new node name.
     *
     * @param actualPath
     *            current position in model
     * @return SchemaPath object
     */
    public static SchemaPath createActualSchemaPath(final Stack<QName> actualPath) {
        return SchemaPath.create(actualPath, true);
    }

    /**
     * Create java.util.List of key node names.
     *
     * @param ctx
     *            Key_stmtContext context
     * @return YANG list key as java.util.List of key node names
     */
    public static List<String> createListKey(final Key_stmtContext ctx) {
        String keyDefinition = stringFromNode(ctx);
        List<String> keys = new ArrayList<>();
        String[] splittedKey = keyDefinition.split(" ");
        for (String keyElement : splittedKey) {
            if (!keyElement.isEmpty()) {
                keys.add(keyElement);
            }
        }
        return keys;
    }

    /**
     * Parse given type body of enumeration statement.
     *
     * @param ctx
     *            type body context to parse
     * @param path
     *            actual position in YANG model
     * @param moduleName
     *            current module name
     * @return List of EnumPair object parsed from given context
     */
    private static List<EnumTypeDefinition.EnumPair> getEnumConstants(final Type_body_stmtsContext ctx,
            final Stack<QName> path, final String moduleName) {
        List<EnumTypeDefinition.EnumPair> enumConstants = new ArrayList<>();

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree enumSpecChild = ctx.getChild(i);
            if (enumSpecChild instanceof Enum_specificationContext) {
                int highestValue = -1;
                for (int j = 0; j < enumSpecChild.getChildCount(); j++) {
                    ParseTree enumChild = enumSpecChild.getChild(j);
                    if (enumChild instanceof Enum_stmtContext) {
                        EnumPair enumPair = createEnumPair((Enum_stmtContext) enumChild, highestValue, path, moduleName);
                        if (enumPair.getValue() > highestValue) {
                            highestValue = enumPair.getValue();
                        }
                        enumConstants.add(enumPair);
                    }
                }
            }
        }
        return enumConstants;
    }

    /**
     * Parse enum statement context
     *
     * @param ctx
     *            enum statement context
     * @param highestValue
     *            current highest value in enumeration
     * @param actualPath
     *            actual position in YANG model
     * @param moduleName
     *            current module name
     * @return EnumPair object parsed from given context
     */
    private static EnumTypeDefinition.EnumPair createEnumPair(final Enum_stmtContext ctx, final int highestValue,
            final Stack<QName> actualPath, final String moduleName) {
        final String name = stringFromNode(ctx);
        SchemaPath path = createTypePath(actualPath, name);
        Integer value = null;

        String description = null;
        String reference = null;
        Status status = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Value_stmtContext) {
                String valueStr = stringFromNode(child);
                value = Integer.valueOf(valueStr);
            } else if (child instanceof Description_stmtContext) {
                description = stringFromNode(child);
            } else if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            } else if (child instanceof Status_stmtContext) {
                status = parseStatus((Status_stmtContext) child);
            }
        }

        if (value == null) {
            value = highestValue + 1;
        }
        if (value < -2147483648 || value > 2147483647) {
            throw new YangParseException(moduleName, ctx.getStart().getLine(), "Error on enum '" + name
                    + "': the enum value MUST be in the range from -2147483648 to 2147483647, but was: " + value);
        }

        EnumPairImpl result = new EnumPairImpl();
        result.qname = path.getPath().get(path.getPath().size() - 1);
        result.path = path;
        result.description = description;
        result.reference = reference;
        result.status = status;
        result.name = name;
        result.value = value;
        return result;
    }

    /**
     * Internal implementation of EnumPair.
     */
    private static class EnumPairImpl implements EnumTypeDefinition.EnumPair {
        private QName qname;
        private SchemaPath path;
        private String description;
        private String reference;
        private Status status;
        private final List<UnknownSchemaNode> unknownNodes = Collections.emptyList();
        private String name;
        private Integer value;

        @Override
        public QName getQName() {
            return qname;
        }

        @Override
        public SchemaPath getPath() {
            return path;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((qname == null) ? 0 : qname.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            result = prime * result + ((unknownNodes == null) ? 0 : unknownNodes.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
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
            EnumPairImpl other = (EnumPairImpl) obj;
            if (qname == null) {
                if (other.qname != null) {
                    return false;
                }
            } else if (!qname.equals(other.qname)) {
                return false;
            }
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            if (unknownNodes == null) {
                if (other.unknownNodes != null) {
                    return false;
                }
            } else if (!unknownNodes.equals(other.unknownNodes)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (value == null) {
                if (other.value != null) {
                    return false;
                }
            } else if (!value.equals(other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return EnumTypeDefinition.EnumPair.class.getSimpleName() + "[name=" + name + ", value=" + value + "]";
        }
    }

    /**
     * Get and parse range from given type body context.
     *
     * @param ctx
     *            type body context to parse
     * @param moduleName
     *            name of current module
     * @return List of RangeConstraint created from this context
     */
    private static List<RangeConstraint> getRangeConstraints(final Type_body_stmtsContext ctx, final String moduleName) {
        List<RangeConstraint> rangeConstraints = Collections.emptyList();
        outer: for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree numRestrChild = ctx.getChild(i);

            if (numRestrChild instanceof Numerical_restrictionsContext) {
                for (int j = 0; j < numRestrChild.getChildCount(); j++) {
                    ParseTree rangeChild = numRestrChild.getChild(j);
                    if (rangeChild instanceof Range_stmtContext) {
                        rangeConstraints = parseRangeConstraints((Range_stmtContext) rangeChild, moduleName);
                        break outer;
                    }
                }
            }

            if (numRestrChild instanceof Decimal64_specificationContext) {
                for (int j = 0; j < numRestrChild.getChildCount(); j++) {
                    ParseTree decRestr = numRestrChild.getChild(j);
                    if (decRestr instanceof Numerical_restrictionsContext) {
                        for (int k = 0; k < decRestr.getChildCount(); k++) {
                            ParseTree rangeChild = decRestr.getChild(k);
                            if (rangeChild instanceof Range_stmtContext) {
                                rangeConstraints = parseRangeConstraints((Range_stmtContext) rangeChild, moduleName);
                                break outer;
                            }
                        }

                    }
                }
            }
        }
        return rangeConstraints;
    }

    /**
     * Parse given range context.
     *
     * @param ctx
     *            range context to parse
     * @param moduleName
     *            name of current module
     * @return List of RangeConstraints parsed from this context
     */
    private static List<RangeConstraint> parseRangeConstraints(final Range_stmtContext ctx, final String moduleName) {
        final int line = ctx.getStart().getLine();
        List<RangeConstraint> rangeConstraints = new ArrayList<>();
        String description = null;
        String reference = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                description = stringFromNode(child);
            } else if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            }
        }

        String rangeStr = stringFromNode(ctx);
        String trimmed = rangeStr.replace(" ", "");
        String[] splittedRange = trimmed.split("\\|");
        for (String rangeDef : splittedRange) {
            String[] splittedRangeDef = rangeDef.split("\\.\\.");
            Number min;
            Number max;
            if (splittedRangeDef.length == 1) {
                min = max = parseNumberConstraintValue(splittedRangeDef[0], moduleName, line);
            } else {
                min = parseNumberConstraintValue(splittedRangeDef[0], moduleName, line);
                max = parseNumberConstraintValue(splittedRangeDef[1], moduleName, line);
            }
            RangeConstraint range = BaseConstraints.rangeConstraint(min, max, description, reference);
            rangeConstraints.add(range);
        }

        return rangeConstraints;
    }

    /**
     * Get and parse length from given type body context.
     *
     * @param ctx
     *            type body context to parse
     * @param moduleName
     *            name of current module
     * @return List of LengthConstraint created from this context
     */
    private static List<LengthConstraint> getLengthConstraints(final Type_body_stmtsContext ctx, final String moduleName) {
        List<LengthConstraint> lengthConstraints = Collections.emptyList();
        outer: for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree stringRestrChild = ctx.getChild(i);
            if (stringRestrChild instanceof String_restrictionsContext) {
                for (int j = 0; j < stringRestrChild.getChildCount(); j++) {
                    ParseTree lengthChild = stringRestrChild.getChild(j);
                    if (lengthChild instanceof Length_stmtContext) {
                        lengthConstraints = parseLengthConstraints((Length_stmtContext) lengthChild, moduleName);
                        break outer;
                    }
                }
            }
        }
        return lengthConstraints;
    }

    /**
     * Parse given length context.
     *
     * @param ctx
     *            length context to parse
     * @param moduleName
     *            name of current module
     * @return List of LengthConstraints parsed from this context
     */
    private static List<LengthConstraint> parseLengthConstraints(final Length_stmtContext ctx, final String moduleName) {
        final int line = ctx.getStart().getLine();
        List<LengthConstraint> lengthConstraints = new ArrayList<>();
        String description = null;
        String reference = null;

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                description = stringFromNode(child);
            } else if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            }
        }

        String lengthStr = stringFromNode(ctx);
        String trimmed = lengthStr.replace(" ", "");
        String[] splittedRange = trimmed.split("\\|");
        for (String rangeDef : splittedRange) {
            String[] splittedRangeDef = rangeDef.split("\\.\\.");
            Number min;
            Number max;
            if (splittedRangeDef.length == 1) {
                min = max = parseNumberConstraintValue(splittedRangeDef[0], moduleName, line);
            } else {
                min = parseNumberConstraintValue(splittedRangeDef[0], moduleName, line);
                max = parseNumberConstraintValue(splittedRangeDef[1], moduleName, line);
            }
            LengthConstraint range = BaseConstraints.lengthConstraint(min, max, description, reference);
            lengthConstraints.add(range);
        }

        return lengthConstraints;
    }

    /**
     * @param value
     *            value to parse
     * @param moduleName
     *            name of current module
     * @param line
     *            current line in module
     * @return wrapper object of primitive java type or UnknownBoundaryNumber if
     *         type is one of special YANG values 'min' or 'max'
     */
    private static Number parseNumberConstraintValue(final String value, final String moduleName, final int line) {
        Number result;
        if ("min".equals(value) || "max".equals(value)) {
            result = new UnknownBoundaryNumber(value);
        } else {
            try {
                if (value.contains(".")) {
                    result = new BigDecimal(value);
                } else {
                    result = Long.valueOf(value);
                }
            } catch (NumberFormatException e) {
                throw new YangParseException(moduleName, line, "Unable to parse range value '" + value + "'.", e);
            }
        }
        return result;
    }

    /**
     * Parse type body and return pattern constraints.
     *
     * @param ctx
     *            type body
     * @return list of pattern constraints
     */
    private static List<PatternConstraint> getPatternConstraint(final Type_body_stmtsContext ctx) {
        List<PatternConstraint> patterns = new ArrayList<>();

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree stringRestrChild = ctx.getChild(i);
            if (stringRestrChild instanceof String_restrictionsContext) {
                for (int j = 0; j < stringRestrChild.getChildCount(); j++) {
                    ParseTree lengthChild = stringRestrChild.getChild(j);
                    if (lengthChild instanceof Pattern_stmtContext) {
                        patterns.add(parsePatternConstraint((Pattern_stmtContext) lengthChild));
                    }
                }
            }
        }
        return patterns;
    }

    /**
     * Internal helper method.
     *
     * @param ctx
     *            pattern context
     * @return PatternConstraint object
     */
    private static PatternConstraint parsePatternConstraint(final Pattern_stmtContext ctx) {
        String description = null;
        String reference = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                description = stringFromNode(child);
            } else if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            }
        }
        String pattern = parsePatternString(ctx);
        return BaseConstraints.patternConstraint(pattern, description, reference);
    }

    /**
     * Parse given context and return pattern value.
     *
     * @param ctx
     *            context to parse
     * @return pattern value as String
     */
    private static String parsePatternString(final Pattern_stmtContext ctx) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < ctx.getChildCount(); ++i) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof StringContext) {
                for (int j = 0; j < child.getChildCount(); j++) {
                    if (j % 2 == 0) {
                        String patternToken = child.getChild(j).getText();
                        result.append(patternToken.substring(1, patternToken.length() - 1));
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * Get fraction digits value from type body.
     *
     * @param ctx
     *            type body context to parse
     * @param moduleName
     *            name of current module
     * @return 'fraction-digits' value if present in given context, null
     *         otherwise
     */
    private static Integer getFractionDigits(final Type_body_stmtsContext ctx, final String moduleName) {
        Integer result = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree dec64specChild = ctx.getChild(i);
            if (dec64specChild instanceof Decimal64_specificationContext) {
                result = parseFractionDigits((Decimal64_specificationContext) dec64specChild, moduleName);
            }
        }
        return result;
    }

    /**
     * Parse decimal64 fraction-digits value.
     *
     * @param ctx
     *            decimal64 context
     * @param moduleName
     *            name of current module
     * @return fraction-digits value as Integer
     */
    private static Integer parseFractionDigits(final Decimal64_specificationContext ctx, final String moduleName) {
        Integer result = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree fdChild = ctx.getChild(i);
            if (fdChild instanceof Fraction_digits_stmtContext) {
                String value = stringFromNode(fdChild);
                try {
                    result = Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    throw new YangParseException(moduleName, ctx.getStart().getLine(),
                            "Unable to parse fraction digits value '" + value + "'.", e);
                }
            }
        }
        return result;
    }

    /**
     * Internal helper method for parsing bit statements from given type body
     * context.
     *
     * @param ctx
     *            type body context to parse
     * @param actualPath
     *            current position in YANG model
     * @param moduleName
     *            current module name
     * @return List of Bit objects created from this context
     */
    private static List<BitsTypeDefinition.Bit> getBits(final Type_body_stmtsContext ctx, final Stack<QName> actualPath,
            final String moduleName) {
        final List<BitsTypeDefinition.Bit> bits = new ArrayList<>();
        for (int j = 0; j < ctx.getChildCount(); j++) {
            ParseTree bitsSpecChild = ctx.getChild(j);
            if (bitsSpecChild instanceof Bits_specificationContext) {
                long highestPosition = -1;
                for (int k = 0; k < bitsSpecChild.getChildCount(); k++) {
                    ParseTree bitChild = bitsSpecChild.getChild(k);
                    if (bitChild instanceof Bit_stmtContext) {
                        Bit bit = parseBit((Bit_stmtContext) bitChild, highestPosition, actualPath, moduleName);
                        if (bit.getPosition() > highestPosition) {
                            highestPosition = bit.getPosition();
                        }
                        bits.add(bit);
                    }
                }
            }
        }
        return bits;
    }

    /**
     * Internal helper method for parsing bit context.
     *
     * @param ctx
     *            bit statement context to parse
     * @param highestPosition
     *            current highest position in bits type
     * @param actualPath
     *            current position in YANG model
     * @param moduleName
     *            current module name
     * @return Bit object parsed from this context
     */
    private static BitsTypeDefinition.Bit parseBit(final Bit_stmtContext ctx, final long highestPosition,
            final Stack<QName> actualPath, final String moduleName) {
        String name = stringFromNode(ctx);
        Long position = null;

        String description = null;
        String reference = null;
        Status status = Status.CURRENT;

        SchemaPath schemaPath = createBaseTypePath(actualPath, name);

        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Position_stmtContext) {
                String positionStr = stringFromNode(child);
                position = Long.valueOf(positionStr);
            } else if (child instanceof Description_stmtContext) {
                description = stringFromNode(child);
            } else if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            } else if (child instanceof Status_stmtContext) {
                status = parseStatus((Status_stmtContext) child);
            }
        }

        if (position == null) {
            position = highestPosition + 1;
        }
        if (position < 0 || position > 4294967295L) {
            throw new YangParseException(moduleName, ctx.getStart().getLine(), "Error on bit '" + name
                    + "': the position value MUST be in the range 0 to 4294967295");
        }

        final List<UnknownSchemaNode> unknownNodes = Collections.emptyList();
        return new BitImpl(position, schemaPath.getPath().get(schemaPath.getPath().size() - 1), schemaPath,
                description, reference, status, unknownNodes);
    }

    /**
     * Parse 'ordered-by' statement.
     *
     * The 'ordered-by' statement defines whether the order of entries within a
     * list are determined by the user or the system. The argument is one of the
     * strings "system" or "user". If not present, order defaults to "system".
     *
     * @param ctx
     *            Ordered_by_stmtContext
     * @return true, if ordered-by contains value 'user', false otherwise
     */
    public static boolean parseUserOrdered(final Ordered_by_stmtContext ctx) {
        boolean result = false;
        for (int j = 0; j < ctx.getChildCount(); j++) {
            ParseTree orderArg = ctx.getChild(j);
            if (orderArg instanceof Ordered_by_argContext) {
                String orderStr = stringFromNode(orderArg);
                switch (orderStr) {
                case "system":
                    result = false;
                    break;
                case "user":
                    result = true;
                    break;
                default:
                    LOG.warn("Invalid 'ordered-by' statement.");
                }
            }
        }
        return result;
    }

    /**
     * Get config statement from given context. If there is no config statement,
     * return config value of parent
     *
     * @param ctx
     *            context to parse
     * @param node
     *            current node
     * @param moduleName
     *            name of current module
     * @param line
     *            line in current module
     * @return config statement parsed from given context
     */
    public static boolean getConfig(final ParseTree ctx, final Builder node, final String moduleName, final int line) {
        boolean result;
        // parse configuration statement
        Boolean config = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Config_stmtContext) {
                config = parseConfig((Config_stmtContext) child, moduleName);
                break;
            }
        }

        // If 'config' is not specified, the default is the same as the parent
        // schema node's 'config' value
        boolean parentConfig = getParentConfig(node);
        if (config == null) {
            result = parentConfig;
        } else {
            // Check: if a node has 'config' set to 'false', no node underneath
            // it can have 'config' set to 'true'
            if (!parentConfig && config) {
                throw new YangParseException(moduleName, line,
                        "Can not set 'config' to 'true' if parent node has 'config' set to 'false'");
            }
            result = config;
        }

        return result;
    }

    private static boolean getParentConfig(final Builder node) {
        Builder parent = node.getParent();
        boolean config = false;

        if (parent instanceof ChoiceCaseBuilder) {
            parent = parent.getParent();
        }
        if (parent instanceof DataSchemaNodeBuilder) {
            config = ((DataSchemaNodeBuilder) parent).isConfiguration();
        } else {
            config = true;
        }
        return config;
    }

    /**
     * Parse config statement.
     *
     * @param ctx
     *            config context to parse
     * @param moduleName
     *            current module name
     * @return true if given context contains string 'true', false otherwise
     */
    private static Boolean parseConfig(final Config_stmtContext ctx, final String moduleName) {
        Boolean result = null;
        if (ctx != null) {
            for (int i = 0; i < ctx.getChildCount(); ++i) {
                final ParseTree configContext = ctx.getChild(i);
                if (configContext instanceof Config_argContext) {
                    final String value = stringFromNode(configContext);
                    switch (value) {
                    case "true":
                        result = true;
                        break;
                    case "false":
                        result = false;
                        break;
                    default:
                        throw new YangParseException(moduleName, ctx.getStart().getLine(),
                                "Failed to parse 'config' statement value: '" + value + "'.");
                    }
                }
            }
        }
        return result;
    }

    /**
     * Parse type body and create UnknownType definition.
     *
     * @param typedefQName
     *            qname of current type
     * @param ctx
     *            type body
     * @param actualPath
     *            actual path in model
     * @param namespace
     *            module namespace
     * @param revision
     *            module revision
     * @param prefix
     *            module prefix
     * @param parent
     *            current node parent
     * @return UnknownType object with constraints from parsed type body
     */
    public static TypeDefinition<?> parseUnknownTypeWithBody(final QName typedefQName,
            final Type_body_stmtsContext ctx, final Stack<QName> actualPath, final URI namespace, final Date revision,
            final String prefix, final Builder parent) {
        String moduleName = parent.getModuleName();
        String typeName = typedefQName.getLocalName();

        UnknownType.Builder unknownType = new UnknownType.Builder(typedefQName);

        if (ctx != null) {
            List<RangeConstraint> rangeStatements = getRangeConstraints(ctx, moduleName);
            List<LengthConstraint> lengthStatements = getLengthConstraints(ctx, moduleName);
            List<PatternConstraint> patternStatements = getPatternConstraint(ctx);
            Integer fractionDigits = getFractionDigits(ctx, moduleName);

            if (parent instanceof TypeDefinitionBuilder) {
                TypeDefinitionBuilder typedef = (TypeDefinitionBuilder) parent;
                typedef.setRanges(rangeStatements);
                typedef.setLengths(lengthStatements);
                typedef.setPatterns(patternStatements);
                typedef.setFractionDigits(fractionDigits);
                return unknownType.build();
            } else {
                TypeDefinition<?> baseType = unknownType.build();
                QName qname = new QName(namespace, revision, prefix, typeName);
                SchemaPath schemaPath = createTypePath(actualPath, typeName);

                ExtendedType.Builder typeBuilder = new ExtendedType.Builder(qname, baseType, null, null, schemaPath);
                typeBuilder.ranges(rangeStatements);
                typeBuilder.lengths(lengthStatements);
                typeBuilder.patterns(patternStatements);
                typeBuilder.fractionDigits(fractionDigits);

                return typeBuilder.build();
            }
        }

        return unknownType.build();
    }

    /**
     * Create TypeDefinition object based on given type name and type body.
     *
     * @param typeName
     *            name of type
     * @param typeBody
     *            type body context
     * @param actualPath
     *            current path in schema
     * @param namespace
     *            current namespace
     * @param revision
     *            current revision
     * @param prefix
     *            current prefix
     * @param parent
     *            parent builder
     * @return TypeDefinition object based on parsed values.
     */
    public static TypeDefinition<?> parseTypeWithBody(final String typeName, final Type_body_stmtsContext typeBody,
            final Stack<QName> actualPath, final URI namespace, final Date revision, final String prefix,
            final Builder parent) {

        final String moduleName = parent.getModuleName();
        final int line = typeBody.getStart().getLine();
        TypeDefinition<?> baseType = null;

        Integer fractionDigits = getFractionDigits(typeBody, moduleName);
        List<LengthConstraint> lengthStatements = getLengthConstraints(typeBody, moduleName);
        List<PatternConstraint> patternStatements = getPatternConstraint(typeBody);
        List<RangeConstraint> rangeStatements = getRangeConstraints(typeBody, moduleName);

        TypeConstraints constraints = new TypeConstraints(moduleName, line);
        constraints.addFractionDigits(fractionDigits);
        constraints.addLengths(lengthStatements);
        constraints.addPatterns(patternStatements);
        constraints.addRanges(rangeStatements);

        SchemaPath baseTypePath = createBaseTypePath(actualPath, typeName);
        SchemaPath extBaseTypePath = createExtendedBaseTypePath(actualPath, namespace, revision, prefix, typeName);

        if (parent instanceof TypeDefinitionBuilder && !(parent instanceof UnionTypeBuilder)) {
            extBaseTypePath = baseTypePath;
        }

        if ("decimal64".equals(typeName)) {
            if (rangeStatements.isEmpty()) {
                try {
                    return new Decimal64(baseTypePath, fractionDigits);
                } catch(Exception e) {
                    throw new YangParseException(moduleName, line, e.getMessage());
                }
            }
            Decimal64 decimalType = new Decimal64(extBaseTypePath, fractionDigits);
            constraints.addRanges(decimalType.getRangeConstraints());
            baseType = decimalType;
        } else if (typeName.startsWith("int")) {
            IntegerTypeDefinition intType = null;
            switch (typeName) {
            case "int8":
                intType = Int8.getInstance();
                break;
            case "int16":
                intType = Int16.getInstance();
                break;
            case "int32":
                intType = Int32.getInstance();
                break;
            case "int64":
                intType = Int64.getInstance();
                break;
            }
            if (intType == null) {
                throw new YangParseException(moduleName, line, "Unknown yang type " + typeName);
            }
            constraints.addRanges(intType.getRangeConstraints());
            baseType = intType;
        } else if (typeName.startsWith("uint")) {
            UnsignedIntegerTypeDefinition uintType = null;
            switch (typeName) {
            case "uint8":
                uintType = Uint8.getInstance();
                break;
            case "uint16":
                uintType = Uint16.getInstance();
                break;
            case "uint32":
                uintType = Uint32.getInstance();
                break;
            case "uint64":
                uintType = Uint64.getInstance();
                break;
            }
            if (uintType == null) {
                throw new YangParseException(moduleName, line, "Unknown yang type " + typeName);
            }
            constraints.addRanges(uintType.getRangeConstraints());
            baseType = uintType;
        } else if ("enumeration".equals(typeName)) {
            List<EnumTypeDefinition.EnumPair> enumConstants = getEnumConstants(typeBody, actualPath, moduleName);
            return new EnumerationType(baseTypePath, enumConstants);
        } else if ("string".equals(typeName)) {
            StringTypeDefinition stringType = StringType.getInstance();
            constraints.addLengths(stringType.getLengthConstraints());
            baseType = stringType;
        } else if ("bits".equals(typeName)) {
            return new BitsType(baseTypePath, getBits(typeBody, actualPath, moduleName));
        } else if ("leafref".equals(typeName)) {
            final String path = parseLeafrefPath(typeBody);
            final boolean absolute = path.startsWith("/");
            RevisionAwareXPath xpath = new RevisionAwareXPathImpl(path, absolute);
            return new Leafref(xpath);
        } else if ("binary".equals(typeName)) {
            BinaryTypeDefinition binaryType = BinaryType.getInstance();
            constraints.addLengths(binaryType.getLengthConstraints());
            baseType = binaryType;
        } else if ("instance-identifier".equals(typeName)) {
            boolean requireInstance = isRequireInstance(typeBody);
            return new InstanceIdentifier(null, requireInstance);
        }

        if (parent instanceof TypeDefinitionBuilder && !(parent instanceof UnionTypeBuilder)) {
            TypeDefinitionBuilder typedef = (TypeDefinitionBuilder) parent;
            typedef.setRanges(constraints.getRange());
            typedef.setLengths(constraints.getLength());
            typedef.setPatterns(constraints.getPatterns());
            typedef.setFractionDigits(constraints.getFractionDigits());
            return baseType;
        }

        List<QName> path = new ArrayList<>(actualPath);
        path.add(new QName(namespace, revision, prefix, typeName));
        SchemaPath schemaPath = SchemaPath.create(path, true);

        QName qname = schemaPath.getPath().get(schemaPath.getPath().size() - 1);
        ExtendedType.Builder typeBuilder = new ExtendedType.Builder(qname, baseType, "", "", schemaPath);

        typeBuilder.ranges(constraints.getRange());
        typeBuilder.lengths(constraints.getLength());
        typeBuilder.patterns(constraints.getPatterns());
        typeBuilder.fractionDigits(constraints.getFractionDigits());

        return typeBuilder.build();
    }

    private static SchemaPath createTypePath(final Stack<QName> actual, final String typeName) {
        QName last = actual.peek();
        QName typeQName = new QName(last.getNamespace(), last.getRevision(), last.getPrefix(), typeName);
        List<QName> path = new ArrayList<>(actual);
        path.add(typeQName);
        return SchemaPath.create(path, true);
    }

    private static SchemaPath createBaseTypePath(final Stack<QName> actual, final String typeName) {
        List<QName> path = new ArrayList<>(actual);
        path.add(BaseTypes.constructQName(typeName));
        return SchemaPath.create(path, true);
    }

    private static SchemaPath createExtendedBaseTypePath(final Stack<QName> actual, final URI namespace, final Date revision,
            final String prefix, final String typeName) {
        QName extTypeName = new QName(namespace, revision, prefix, typeName);
        QName baseTypeName = BaseTypes.constructQName(typeName);
        List<QName> path = new ArrayList<>(actual);
        path.add(extTypeName);
        path.add(baseTypeName);
        return SchemaPath.create(path, true);
    }

    /**
     * Parse given context and find identityref base value.
     *
     * @param ctx
     *            type body
     * @return identityref base value as String
     */
    public static String getIdentityrefBase(final Type_body_stmtsContext ctx) {
        String result = null;
        outer: for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Identityref_specificationContext) {
                for (int j = 0; j < child.getChildCount(); j++) {
                    ParseTree baseArg = child.getChild(j);
                    if (baseArg instanceof Base_stmtContext) {
                        result = stringFromNode(baseArg);
                        break outer;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Parse type body statement and find require-instance value.
     *
     * @param ctx
     *            type body context
     * @return require-instance value
     */
    private static boolean isRequireInstance(final Type_body_stmtsContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Instance_identifier_specificationContext) {
                for (int j = 0; j < child.getChildCount(); j++) {
                    ParseTree reqStmt = child.getChild(j);
                    if (reqStmt instanceof Require_instance_stmtContext) {
                        for (int k = 0; k < reqStmt.getChildCount(); k++) {
                            ParseTree reqArg = reqStmt.getChild(k);
                            if (reqArg instanceof Require_instance_argContext) {
                                return Boolean.valueOf(stringFromNode(reqArg));
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Parse type body statement and find leafref path.
     *
     * @param ctx
     *            type body context
     * @return leafref path as String
     */
    private static String parseLeafrefPath(final Type_body_stmtsContext ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Leafref_specificationContext) {
                for (int j = 0; j < child.getChildCount(); j++) {
                    ParseTree leafRefSpec = child.getChild(j);
                    if (leafRefSpec instanceof Path_stmtContext) {
                        return stringFromNode(leafRefSpec);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Internal helper method for parsing must statement.
     *
     * @param ctx
     *            Must_stmtContext
     * @return MustDefinition object based on parsed context
     */
    public static MustDefinition parseMust(final YangParser.Must_stmtContext ctx) {
        StringBuilder mustText = new StringBuilder();
        String description = null;
        String reference = null;
        String errorAppTag = null;
        String errorMessage = null;
        for (int i = 0; i < ctx.getChildCount(); ++i) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof StringContext) {
                final StringContext context = (StringContext) child;
                if (context.getChildCount() == 1) {
                    String mustPart = context.getChild(0).getText();
                    // trim start and end quotation
                    mustText.append(mustPart.substring(1, mustPart.length() - 1));
                } else {
                    for (int j = 0; j < context.getChildCount(); j++) {
                        String mustPart = context.getChild(j).getText();
                        if (j == 0) {
                            mustText.append(mustPart.substring(0, mustPart.length() - 1));
                            continue;
                        }
                        if (j % 2 == 0) {
                            mustText.append(mustPart.substring(1));
                        }
                    }
                }
            } else if (child instanceof Description_stmtContext) {
                description = stringFromNode(child);
            } else if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            } else if (child instanceof Error_app_tag_stmtContext) {
                errorAppTag = stringFromNode(child);
            } else if (child instanceof Error_message_stmtContext) {
                errorMessage = stringFromNode(child);
            }
        }

        return new MustDefinitionImpl(mustText.toString(), description, reference, errorAppTag, errorMessage);
    }

    /**
     * Parse given context and set constraints to constraints builder.
     *
     * @param ctx
     *            context to parse
     * @param constraints
     *            ConstraintsBuilder to fill
     */
    public static void parseConstraints(final ParseTree ctx, final ConstraintsBuilder constraints) {
        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree childNode = ctx.getChild(i);
            if (childNode instanceof Max_elements_stmtContext) {
                Integer max = parseMaxElements((Max_elements_stmtContext) childNode, constraints.getModuleName());
                constraints.setMaxElements(max);
            } else if (childNode instanceof Min_elements_stmtContext) {
                Integer min = parseMinElements((Min_elements_stmtContext) childNode, constraints.getModuleName());
                constraints.setMinElements(min);
            } else if (childNode instanceof Must_stmtContext) {
                MustDefinition must = parseMust((Must_stmtContext) childNode);
                constraints.addMustDefinition(must);
            } else if (childNode instanceof Mandatory_stmtContext) {
                for (int j = 0; j < childNode.getChildCount(); j++) {
                    ParseTree mandatoryTree = childNode.getChild(j);
                    if (mandatoryTree instanceof Mandatory_argContext) {
                        Boolean mandatory = Boolean.valueOf(stringFromNode(mandatoryTree));
                        constraints.setMandatory(mandatory);
                    }
                }
            } else if (childNode instanceof When_stmtContext) {
                constraints.addWhenCondition(stringFromNode(childNode));
            }
        }
    }

    private static Integer parseMinElements(final Min_elements_stmtContext ctx, final String moduleName) {
        Integer result = null;
        try {
            for (int i = 0; i < ctx.getChildCount(); i++) {
                ParseTree minArg = ctx.getChild(i);
                if (minArg instanceof Min_value_argContext) {
                    result = Integer.valueOf(stringFromNode(minArg));
                }
            }
            if (result == null) {
                throw new IllegalArgumentException();
            }
            return result;
        } catch (Exception e) {
            throw new YangParseException(moduleName, ctx.getStart().getLine(), "Failed to parse min-elements.", e);
        }
    }

    private static Integer parseMaxElements(final Max_elements_stmtContext ctx, final String moduleName) {
        Integer result = null;
        try {
            for (int i = 0; i < ctx.getChildCount(); i++) {
                ParseTree maxArg = ctx.getChild(i);
                if (maxArg instanceof Max_value_argContext) {
                    result = Integer.valueOf(stringFromNode(maxArg));
                }
            }
            if (result == null) {
                throw new IllegalArgumentException();
            }
            return result;
        } catch (Exception e) {
            throw new YangParseException(moduleName, ctx.getStart().getLine(), "Failed to parse max-elements.", e);
        }
    }

    /**
     * Parse given context and return yin value.
     *
     * @param ctx
     *            context to parse
     * @return true if value is 'true', false otherwise
     */
    public static boolean parseYinValue(final Argument_stmtContext ctx) {
        boolean yinValue = false;
        outer: for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree yin = ctx.getChild(i);
            if (yin instanceof Yin_element_stmtContext) {
                for (int j = 0; j < yin.getChildCount(); j++) {
                    ParseTree yinArg = yin.getChild(j);
                    if (yinArg instanceof Yin_element_argContext) {
                        String yinString = stringFromNode(yinArg);
                        if ("true".equals(yinString)) {
                            yinValue = true;
                            break outer;
                        }
                    }
                }
            }
        }
        return yinValue;
    }

    /**
     * Check this base type.
     *
     * @param typeName
     *            base YANG type name
     * @param moduleName
     *            name of current module
     * @param line
     *            line in module
     * @throws YangParseException
     *             if this is one of YANG type which MUST contain additional
     *             informations in its body
     */
    public static void checkMissingBody(final String typeName, final String moduleName, final int line) {
        switch (typeName) {
        case "decimal64":
            throw new YangParseException(moduleName, line,
                    "The 'fraction-digits' statement MUST be present if the type is 'decimal64'.");
        case "identityref":
            throw new YangParseException(moduleName, line,
                    "The 'base' statement MUST be present if the type is 'identityref'.");
        case "leafref":
            throw new YangParseException(moduleName, line,
                    "The 'path' statement MUST be present if the type is 'leafref'.");
        case "bits":
            throw new YangParseException(moduleName, line, "The 'bit' statement MUST be present if the type is 'bits'.");
        case "enumeration":
            throw new YangParseException(moduleName, line,
                    "The 'enum' statement MUST be present if the type is 'enumeration'.");
        }
    }

    /**
     * Parse refine statement.
     *
     * @param refineCtx
     *            refine statement
     * @param moduleName
     *            name of current module
     * @return RefineHolder object representing this refine statement
     */
    public static RefineHolder parseRefine(final Refine_stmtContext refineCtx, final String moduleName) {
        final String refineTarget = stringFromNode(refineCtx);
        final RefineHolder refine = new RefineHolder(moduleName, refineCtx.getStart().getLine(), refineTarget);
        for (int i = 0; i < refineCtx.getChildCount(); i++) {
            ParseTree refinePom = refineCtx.getChild(i);
            if (refinePom instanceof Refine_pomContext) {
                for (int j = 0; j < refinePom.getChildCount(); j++) {
                    ParseTree refineStmt = refinePom.getChild(j);
                    parseRefineDefault(refine, refineStmt);

                    if (refineStmt instanceof Refine_leaf_stmtsContext) {
                        parseRefine(refine, (Refine_leaf_stmtsContext) refineStmt);
                    } else if (refineStmt instanceof Refine_container_stmtsContext) {
                        parseRefine(refine, (Refine_container_stmtsContext) refineStmt);
                    } else if (refineStmt instanceof Refine_list_stmtsContext) {
                        parseRefine(refine, (Refine_list_stmtsContext) refineStmt);
                    } else if (refineStmt instanceof Refine_leaf_list_stmtsContext) {
                        parseRefine(refine, (Refine_leaf_list_stmtsContext) refineStmt);
                    } else if (refineStmt instanceof Refine_choice_stmtsContext) {
                        parseRefine(refine, (Refine_choice_stmtsContext) refineStmt);
                    } else if (refineStmt instanceof Refine_anyxml_stmtsContext) {
                        parseRefine(refine, (Refine_anyxml_stmtsContext) refineStmt);
                    }
                }
            }
        }
        return refine;
    }

    private static void parseRefineDefault(final RefineHolder refine, final ParseTree refineStmt) {
        for (int i = 0; i < refineStmt.getChildCount(); i++) {
            ParseTree refineArg = refineStmt.getChild(i);
            if (refineArg instanceof Description_stmtContext) {
                String description = stringFromNode(refineArg);
                refine.setDescription(description);
            } else if (refineArg instanceof Reference_stmtContext) {
                String reference = stringFromNode(refineArg);
                refine.setReference(reference);
            } else if (refineArg instanceof Config_stmtContext) {
                Boolean config = parseConfig((Config_stmtContext) refineArg, refine.getModuleName());
                refine.setConfiguration(config);
            }
        }
    }

    private static RefineHolder parseRefine(final RefineHolder refine, final Refine_leaf_stmtsContext refineStmt) {
        for (int i = 0; i < refineStmt.getChildCount(); i++) {
            ParseTree refineArg = refineStmt.getChild(i);
            if (refineArg instanceof Default_stmtContext) {
                String defaultStr = stringFromNode(refineArg);
                refine.setDefaultStr(defaultStr);
            } else if (refineArg instanceof Mandatory_stmtContext) {
                for (int j = 0; j < refineArg.getChildCount(); j++) {
                    ParseTree mandatoryTree = refineArg.getChild(j);
                    if (mandatoryTree instanceof Mandatory_argContext) {
                        Boolean mandatory = Boolean.valueOf(stringFromNode(mandatoryTree));
                        refine.setMandatory(mandatory);
                    }
                }
            } else if (refineArg instanceof Must_stmtContext) {
                MustDefinition must = parseMust((Must_stmtContext) refineArg);
                refine.setMust(must);

            }
        }
        return refine;
    }

    private static RefineHolder parseRefine(final RefineHolder refine, final Refine_container_stmtsContext refineStmt) {
        for (int i = 0; i < refineStmt.getChildCount(); i++) {
            ParseTree refineArg = refineStmt.getChild(i);
            if (refineArg instanceof Must_stmtContext) {
                MustDefinition must = parseMust((Must_stmtContext) refineArg);
                refine.setMust(must);
            } else if (refineArg instanceof Presence_stmtContext) {
                refine.setPresence(true);
            }
        }
        return refine;
    }

    private static RefineHolder parseRefine(final RefineHolder refine, final Refine_list_stmtsContext refineStmt) {
        for (int i = 0; i < refineStmt.getChildCount(); i++) {
            ParseTree refineArg = refineStmt.getChild(i);
            if (refineArg instanceof Must_stmtContext) {
                MustDefinition must = parseMust((Must_stmtContext) refineArg);
                refine.setMust(must);
            } else if (refineArg instanceof Max_elements_stmtContext) {
                Integer max = parseMaxElements((Max_elements_stmtContext) refineArg, refine.getModuleName());
                refine.setMaxElements(max);
            } else if (refineArg instanceof Min_elements_stmtContext) {
                Integer min = parseMinElements((Min_elements_stmtContext) refineArg, refine.getModuleName());
                refine.setMinElements(min);
            }
        }
        return refine;
    }

    private static RefineHolder parseRefine(final RefineHolder refine, final Refine_leaf_list_stmtsContext refineStmt) {
        for (int i = 0; i < refineStmt.getChildCount(); i++) {
            ParseTree refineArg = refineStmt.getChild(i);
            if (refineArg instanceof Must_stmtContext) {
                MustDefinition must = parseMust((Must_stmtContext) refineArg);
                refine.setMust(must);
            } else if (refineArg instanceof Max_elements_stmtContext) {
                Integer max = parseMaxElements((Max_elements_stmtContext) refineArg, refine.getModuleName());
                refine.setMaxElements(max);
            } else if (refineArg instanceof Min_elements_stmtContext) {
                Integer min = parseMinElements((Min_elements_stmtContext) refineArg, refine.getModuleName());
                refine.setMinElements(min);
            }
        }
        return refine;
    }

    private static RefineHolder parseRefine(final RefineHolder refine, final Refine_choice_stmtsContext refineStmt) {
        for (int i = 0; i < refineStmt.getChildCount(); i++) {
            ParseTree refineArg = refineStmt.getChild(i);
            if (refineArg instanceof Default_stmtContext) {
                String defaultStr = stringFromNode(refineArg);
                refine.setDefaultStr(defaultStr);
            } else if (refineArg instanceof Mandatory_stmtContext) {
                for (int j = 0; j < refineArg.getChildCount(); j++) {
                    ParseTree mandatoryTree = refineArg.getChild(j);
                    if (mandatoryTree instanceof Mandatory_argContext) {
                        Boolean mandatory = Boolean.valueOf(stringFromNode(mandatoryTree));
                        refine.setMandatory(mandatory);
                    }
                }
            }
        }
        return refine;
    }

    private static RefineHolder parseRefine(final RefineHolder refine, final Refine_anyxml_stmtsContext refineStmt) {
        for (int i = 0; i < refineStmt.getChildCount(); i++) {
            ParseTree refineArg = refineStmt.getChild(i);
            if (refineArg instanceof Must_stmtContext) {
                MustDefinition must = parseMust((Must_stmtContext) refineArg);
                refine.setMust(must);
            } else if (refineArg instanceof Mandatory_stmtContext) {
                for (int j = 0; j < refineArg.getChildCount(); j++) {
                    ParseTree mandatoryTree = refineArg.getChild(j);
                    if (mandatoryTree instanceof Mandatory_argContext) {
                        Boolean mandatory = Boolean.valueOf(stringFromNode(mandatoryTree));
                        refine.setMandatory(mandatory);
                    }
                }
            }
        }
        return refine;
    }

    public static String getArgumentString(final org.antlr.v4.runtime.ParserRuleContext ctx) {
        List<StringContext> potentialValues = ctx.getRuleContexts(StringContext.class);
        checkState(!potentialValues.isEmpty());
        return ParserListenerUtils.stringFromStringContext(potentialValues.get(0));
    }

    public static <T extends ParserRuleContext> Optional<T> getFirstContext(final ParserRuleContext context,final Class<T> contextType) {
        List<T> potential = context.getRuleContexts(contextType);
        if(potential.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(potential.get(0));
    }

}
