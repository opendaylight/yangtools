//
//Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
//
//This program and the accompanying materials are made available under the
//terms of the Eclipse Public License v1.0 which accompanies this distribution,
//and is available at http://www.eclipse.org/legal/epl-v10.html
//



/**
* Parser grammar for YANG as manually derived from ABNF form of YANG from
* RFC6020 with all erratas applied as of 2014-12-03.
*
* This grammar DOES NOT reflect any current changes, which are present
* in draft-netmod-rfc6020bis,
*
* Since ANTLR and ABNF uses different syntaxt and ANTLR parser grammar does not allow for inlined literals in rules,
* there are some differences from ABNF grammar (except these differences we tried to preserve style and statement
* ordering present in RFC6020).
*
* * Dash "-" is not allowed in rule identifiers, dashes are replaced with underscores "_".
*      E.g. ABNF: module-stmt, ANTLR module_stmt
* * Literals are defined in RFC6020YangLexer as ANTLR Tokens, and referenced here. See module_stmt.
* * Different syntax for repetitions and optional statements:
*    * 0-1: ABNF: [statement] ANTLR: statement?
*    * 0-n: ABNF: *(statement) ANTLR: statement*
*    * 1-n: ABNF 1*(statement) ANTLR: statement+
*
* This grammar have some additional rules and rules which have bit different definition, which are not present in
* original YANG ABNF, but are required to proper express ABNF behaviour in ANTLR.
* These rules MUST be documented, are annotated by @Workaround annotation in comment.
* Most notable examples of this rules are:
*   * identifier rule (bit different definition)
*   * any-keyword rule (additional rule).
*
*
* Other notable difference is that YANG has string concatenation and defines also rules for content,
* Each of these rules, which were defined in style:
*    {example}-str = < A string which matches the rule {example}-str >
*
* Are defined in ANTLR as:
*    {example}-str : string
*
* Value validation for this rules will be done in code, which consumes AST using custom writen code,
* or other rules present in this grammar.
*
* Each of these rules MUST have documentation in comments, how they are enforced.
*/
parser grammar RFC6020YangParser;

@header {
package org.opendaylight.yangtools.antlrv4.code.gen;
}

options{
 tokenVocab = RFC6020YangLexer;
}


/**
*
*
* @EntryPoint Entry point for parsing YANG modules and submodules.
*/
yang : module_stmt | submodule_stmt ;

module_stmt : SEP? MODULE_KEYWORD SEP string SEP?                           // string validated in YangModelBasicValidationListener.enterModule_stmt()
           LEFT_BRACE stmtsep
               module_header_stmts
               linkage_stmts
               meta_stmts
               revision_stmts
               body_stmts
           RIGHT_BRACE;

submodule_stmt : SEP? SUBMODULE_KEYWORD SEP string SEP?                     // string validated in YangModelBasicValidationListener.enterSubmodule_stmt()
              LEFT_BRACE stmtsep
                  submodule_header_stmts
                  linkage_stmts meta_stmts
                  revision_stmts
                  body_stmts
              RIGHT_BRACE;

module_header_stmts :  (
                         yang_version_stmt stmtsep
                       | namespace_stmt stmtsep
                       | prefix_stmt stmtsep
                    )+ ;

submodule_header_stmts : (
                           yang_version_stmt stmtsep
                         | belongs_to_stmt stmtsep
                      )+ ;

meta_stmts : (
               organization_stmt stmtsep
             | contact_stmt stmtsep
             | description_stmt stmtsep
             | reference_stmt stmtsep
         )*;

linkage_stmts : (
               import_stmt stmtsep
             | include_stmt stmtsep
             )*;

revision_stmts :  (revision_stmt stmtsep)*;

body_stmts :   (( extension_stmt
             | feature_stmt
             | identity_stmt
             | typedef_stmt
             | grouping_stmt
             | data_def_stmt
             | augment_stmt
             | rpc_stmt
             | notification_stmt
             | deviation_stmt
             )
             stmtsep)*;

data_def_stmt :   container_stmt
             | leaf_stmt
             | leaf_list_stmt
             | list_stmt
             | choice_stmt
             | anyxml_stmt
             | uses_stmt
             ;

yang_version_stmt : YANG_VERSION_KEYWORD SEP string SEP? stmtend;           // string validated in YangModelBasicValidationListener.enterModule_header_stmts() and/or YangModelBasicValidationListener.enterSubmodule_header_stmts()

import_stmt : IMPORT_KEYWORD SEP string SEP?                                // string validated in YangModelBasicValidationListener.enterImport_stmt()
           LEFT_BRACE stmtsep
               prefix_stmt stmtsep
               (revision_date_stmt stmtsep)?
           RIGHT_BRACE;

include_stmt : INCLUDE_KEYWORD SEP string SEP?                              // string validated in YangModelBasicValidationListener.enterInclude_stmt()
            (SEMICOLON |
             LEFT_BRACE stmtsep
                 (revision_date_stmt stmtsep)?
             RIGHT_BRACE);

namespace_stmt : NAMESPACE_KEYWORD SEP string SEP? stmtend;                 // string validated in YangModelBasicValidationListener.enterNamespace_stmt()

prefix_stmt : PREFIX_KEYWORD SEP string SEP? stmtend;                       // string validated in YangModelBasicValidationListener.enterPrefix_stmt()

belongs_to_stmt : BELONGS_TO_KEYWORD SEP string SEP?                        // string validated in YangModelBasicValidationListener.enterBelongs_to_stmt()
               LEFT_BRACE stmtsep
                   prefix_stmt stmtsep
               RIGHT_BRACE;

organization_stmt : ORGANIZATION_KEYWORD SEP string SEP? stmtend;           // string not validated

contact_stmt : CONTACT_KEYWORD SEP string SEP? stmtend;                     // string not validated

description_stmt : DESCRIPTION_KEYWORD SEP string SEP? stmtend;             // string not validated

reference_stmt : REFERENCE_KEYWORD SEP string SEP? stmtend;                 // string not validated

units_stmt : UNITS_KEYWORD SEP string SEP? stmtend;                         // string not validated

revision_stmt : REVISION_KEYWORD SEP string SEP?                            // string validated in YangModelBasicValidationListener.enterRevision_stmt()
             (SEMICOLON |
              LEFT_BRACE stmtsep
                  (description_stmt stmtsep)?
                  (reference_stmt stmtsep)?
              RIGHT_BRACE);

revision_date_stmt : REVISION_DATE_KEYWORD SEP string stmtend;              // string validated in YangModelBasicValidationListener.enterRevision_date_stmt()

extension_stmt : EXTENSION_KEYWORD SEP string SEP?                          // string validated in YangModelBasicValidationListener.enterExtension_stmt()
              (SEMICOLON |
                 LEFT_BRACE stmtsep
                     (   argument_stmt stmtsep
                       | status_stmt stmtsep
                       | description_stmt stmtsep
                       | reference_stmt stmtsep
                     )*
                 RIGHT_BRACE
              );

argument_stmt : ARGUMENT_KEYWORD SEP string SEP?                            // string validated in YangModelBasicValidationListener.enterArgument_stmt()
             (SEMICOLON |
              LEFT_BRACE stmtsep
                  (yin_element_stmt stmtsep)?
              RIGHT_BRACE
             );

yin_element_stmt : YIN_ELEMENT_KEYWORD SEP yin_element_arg stmtend;

yin_element_arg : string;                                                   // string validated as TRUE_KEYWORD = 'true' | FALSE_KEYWORD = 'false' in YangModelBasicValidationListener.enterYin_element_arg()

identity_stmt : IDENTITY_KEYWORD SEP string SEP?                            // string validated in YangModelBasicValidationListener.enterIdentity_stmt()
             (SEMICOLON |
              LEFT_BRACE stmtsep
                  (   base_stmt stmtsep
                    | status_stmt stmtsep
                    | description_stmt stmtsep
                    | reference_stmt stmtsep
                  )*
              RIGHT_BRACE
             );

base_stmt : BASE_KEYWORD SEP identifier_ref SEP? stmtend;                   // string validated in YangModelBasicValidationListener.enterBase_stmt()

feature_stmt : FEATURE_KEYWORD SEP string SEP?                              // string validated in YangModelBasicValidationListener.enterFeature_stmt()
             (SEMICOLON |
              LEFT_BRACE stmtsep
                 (   if_feature_stmt stmtsep
                   | status_stmt stmtsep
                   | description_stmt stmtsep
                   | reference_stmt stmtsep
                 )*
              RIGHT_BRACE
             );

if_feature_stmt : IF_FEATURE_KEYWORD SEP identifier_ref SEP? stmtend;       // string validated in YangModelBasicValidationListener.enterIf_feature_stmt()

typedef_stmt : TYPEDEF_KEYWORD SEP string SEP?                              // string validated in YangModelBasicValidationListener.enterTypedef_stmt()
            LEFT_BRACE stmtsep
               (   type_stmt stmtsep
                 | units_stmt stmtsep
                 | default_stmt stmtsep
                 | status_stmt stmtsep
                 | description_stmt stmtsep
                 | reference_stmt stmtsep
               )+
            RIGHT_BRACE;

type_stmt : TYPE_KEYWORD SEP identifier_ref SEP?                            // string validated in YangModelBasicValidationListener.enterType_stmt()
         (SEMICOLON |
          LEFT_BRACE stmtsep
              type_body_stmts
          RIGHT_BRACE
         );

type_body_stmts :  numerical_restrictions
              | decimal64_specification
              | string_restrictions
              | enum_specification
              | leafref_specification
              | identityref_specification
              | instance_identifier_specification
              | bits_specification
              | union_specification
              ;

numerical_restrictions : range_stmt stmtsep;

range_stmt : RANGE_KEYWORD SEP string SEP?                                  // see @Entrypoint Entry point for parsing range-arg-str (string validated in ParserListenerUtils.parseRangeConstraints())
          (SEMICOLON |
           LEFT_BRACE stmtsep
               (   error_message_stmt stmtsep
                 | error_app_tag_stmt stmtsep
                 | description_stmt stmtsep
                 | reference_stmt stmtsep
               )*
           RIGHT_BRACE
          );

decimal64_specification : (fraction_digits_stmt stmtsep | range_stmt stmtsep)*;
                                                                            // decimal64_specification - RFC6020 errata ID 3290 applied here

fraction_digits_stmt : FRACTION_DIGITS_KEYWORD SEP string stmtend;          // string validated in ParserListenerUtils.parseFractionDigits()

string_restrictions : (length_stmt stmtsep | pattern_stmt stmtsep)*;

length_stmt : LENGTH_KEYWORD SEP string SEP?                                // see @Entrypoint Entry point for parsing length-arg-str (string validated in ParserListenerUtils.parseLengthConstraints())
           (SEMICOLON |
            LEFT_BRACE stmtsep
               (   error_message_stmt stmtsep
                 | error_app_tag_stmt stmtsep
                 | description_stmt stmtsep
                 | reference_stmt stmtsep
               )*
             RIGHT_BRACE
           );

pattern_stmt : PATTERN_KEYWORD SEP string SEP?                              // string validated in ParserListenerUtils.parsePatternConstraint()
            (SEMICOLON |
             LEFT_BRACE stmtsep
                 (   error_message_stmt stmtsep
                   | error_app_tag_stmt stmtsep
                   | description_stmt stmtsep
                   | reference_stmt stmtsep
                 )*
              RIGHT_BRACE
            );

default_stmt : DEFAULT_KEYWORD SEP string stmtend;                          // string not validated

enum_specification : (enum_stmt stmtsep)+;

enum_stmt : ENUM_KEYWORD SEP string SEP?                                    // string validated in ParserListenerUtils.createEnumPair()
         (SEMICOLON |
          LEFT_BRACE stmtsep
             (   value_stmt stmtsep
               | status_stmt stmtsep
               | description_stmt stmtsep
               | reference_stmt stmtsep
             )*
          RIGHT_BRACE
         );

leafref_specification : path_stmt stmtsep;

path_stmt : PATH_KEYWORD SEP string stmtend;                                // see @Entrypoint Entry point for parsing path-arg-str (string validated in ParserListenerUtils.parseLeafrefPath())

require_instance_stmt : REQUIRE_INSTANCE_KEYWORD SEP require_instance_arg stmtend;

require_instance_arg : string;                                              // string validated as TRUE_KEYWORD = 'true' | FALSE_KEYWORD = 'false' in ParserListenerUtils.isRequireInstance()

instance_identifier_specification : (require_instance_stmt stmtsep)?;

identityref_specification : base_stmt stmtsep;

union_specification : (type_stmt stmtsep)+;

bits_specification : (bit_stmt stmtsep)+;

bit_stmt : BIT_KEYWORD SEP string SEP?                                      // string validated in ParserListenerUtils.parseBit()
        (SEMICOLON |
         LEFT_BRACE  stmtsep
              (   position_stmt stmtsep
                | status_stmt stmtsep
                | description_stmt stmtsep
                | reference_stmt stmtsep
              )*
         RIGHT_BRACE
        );

position_stmt : POSITION_KEYWORD SEP string stmtend;                        // string validated in ParserListenerUtils.parseBit()

status_stmt : STATUS_KEYWORD SEP status_arg stmtend;

status_arg : string;      // string validated as CURRENT_KEYWORD = 'current' | OBSOLETE_KEYWORD = 'obsolete' | DEPRECATED_KEYWORD = 'deprecated' in YangModelBasicValidationListener.enterStatus_arg()

config_stmt : CONFIG_KEYWORD SEP config_arg stmtend;

config_arg : string;      // string validated as TRUE_KEYWORD = 'true' | FALSE_KEYWORD = 'false' in YangModelBasicValidationListener.enterConfig_arg()

mandatory_stmt : MANDATORY_KEYWORD SEP mandatory_arg stmtend;

mandatory_arg :string;    // string validated as TRUE_KEYWORD = 'true' | FALSE_KEYWORD = 'false' in YangModelBasicValidationListener.enterMandatory_arg()

presence_stmt : PRESENCE_KEYWORD SEP string stmtend;                        // string not validated

ordered_by_stmt : ORDERED_BY_KEYWORD SEP ordered_by_arg stmtend;

ordered_by_arg : string;  // string validated as USER_KEYWORD = 'user' | SYSTEM_KEYWORD = 'system' in YangModelBasicValidationListener.enterOrdered_by_arg()

must_stmt : MUST_KEYWORD SEP string SEP?                                    // string validated in ParserListenerUtils.parseMust()
         (SEMICOLON |
          LEFT_BRACE stmtsep
             (    error_message_stmt stmtsep
                | error_app_tag_stmt stmtsep
                | description_stmt stmtsep
                | reference_stmt stmtsep
             )*
          RIGHT_BRACE
         );

error_message_stmt : ERROR_MESSAGE_KEYWORD SEP string stmtend;              // string validated in ParserListenerUtils.parseMust()

error_app_tag_stmt : ERROR_APP_TAG_KEYWORD SEP string stmtend;              // string validated in ParserListenerUtils.parseMust()

min_elements_stmt : MIN_ELEMENTS_KEYWORD SEP min_value_arg stmtend;

min_value_arg : non_negative_integer_value;

max_elements_stmt : MAX_ELEMENTS_KEYWORD SEP max_value_arg stmtend;

max_value_arg : UNBOUNDED_KEYWORD | POSITIVE_INTEGER_VALUE | UNQUOTED_POSITIVE_INTEGER_VALUE;

value_stmt : VALUE_KEYWORD SEP string stmtend;                              // string validated in ParserListenerUtils.createEnumPair()

grouping_stmt : GROUPING_KEYWORD SEP string SEP?                            // string validated in YangModelBasicValidationListener.enterGrouping_stmt()
             (SEMICOLON |
              LEFT_BRACE stmtsep
                  (   status_stmt stmtsep
                    | description_stmt stmtsep
                    | reference_stmt  stmtsep
                    | typedef_stmt stmtsep
                    | grouping_stmt stmtsep
                    | data_def_stmt stmtsep

                  )*
              RIGHT_BRACE
            );

container_stmt : CONTAINER_KEYWORD SEP string SEP?                          // string validated in YangModelBasicValidationListener.enterContainer_stmt()
              (SEMICOLON |
               LEFT_BRACE  stmtsep
                    (
                        when_stmt stmtsep
                      | if_feature_stmt stmtsep
                      | must_stmt stmtsep
                      | presence_stmt stmtsep
                      | config_stmt stmtsep
                      | status_stmt stmtsep
                      | description_stmt stmtsep
                      | reference_stmt stmtsep
                      | typedef_stmt stmtsep
                      | grouping_stmt stmtsep
                      | data_def_stmt stmtsep

                    )*
                RIGHT_BRACE
              );

leaf_stmt : LEAF_KEYWORD SEP string SEP?                                    // string validated in YangModelBasicValidationListener.enterLeaf_stmt()
                 LEFT_BRACE stmtsep
                 (   when_stmt stmtsep
                   | if_feature_stmt stmtsep
                   | type_stmt stmtsep
                   | units_stmt stmtsep
                   | must_stmt stmtsep
                   | default_stmt stmtsep
                   | config_stmt stmtsep
                   | mandatory_stmt stmtsep
                   | status_stmt stmtsep
                   | description_stmt stmtsep
                   | reference_stmt stmtsep
                 )*
                 RIGHT_BRACE;

leaf_list_stmt : LEAF_LIST_KEYWORD SEP string SEP?                          // string validated in YangModelBasicValidationListener.enterLeaf_list_stmt
                         LEFT_BRACE stmtsep
                         (    when_stmt stmtsep
                            | if_feature_stmt stmtsep
                            | type_stmt stmtsep
                            | units_stmt stmtsep
                            | must_stmt stmtsep
                            | config_stmt stmtsep
                            | min_elements_stmt stmtsep
                            | max_elements_stmt stmtsep
                            | ordered_by_stmt stmtsep
                            | status_stmt stmtsep
                            | description_stmt stmtsep
                            | reference_stmt stmtsep
                         )*
                        RIGHT_BRACE;

list_stmt : LIST_KEYWORD SEP string SEP?                                    // string validated in YangModelBasicValidationListener.enterList_stmt()
                 LEFT_BRACE stmtsep
                 (   when_stmt stmtsep
                   | if_feature_stmt stmtsep
                   | must_stmt stmtsep
                   | key_stmt stmtsep
                   | unique_stmt stmtsep
                   | config_stmt stmtsep
                   | min_elements_stmt stmtsep
                   | max_elements_stmt stmtsep
                   | ordered_by_stmt stmtsep
                   | status_stmt stmtsep
                   | description_stmt stmtsep
                   | reference_stmt stmtsep
                   | typedef_stmt stmtsep
                   | grouping_stmt stmtsep
                   | data_def_stmt stmtsep
                 )*
                 RIGHT_BRACE;

key_stmt : KEY_KEYWORD SEP string stmtend;                                  // see @EntryPoint Entry point for parsing key-arg-str (string validated in YangModelBasicValidationListener.enterKey_stmt())

/**
*
*
* @EntryPoint Entry point for parsing key-arg-str
*/
key_arg :  node_identifier (SEP node_identifier)*;

unique_stmt : UNIQUE_KEYWORD SEP string stmtend;                            // see @EntryPoint Entry point for parsing unique-arg-str (string validated in YangModelBasicValidationListener.enterUnique_stmt())

/**
*
*
* @EntryPoint Entry point for parsing unique-arg-str
*/
unique_arg : descendant_schema_nodeid (SEP descendant_schema_nodeid)*;

choice_stmt : CHOICE_KEYWORD SEP string SEP?                                // string validated in YangModelBasicValidationListener.enterChoice_stmt()
             (SEMICOLON |
                LEFT_BRACE stmtsep
                (    when_stmt stmtsep
                   | if_feature_stmt stmtsep
                   | default_stmt stmtsep
                   | config_stmt stmtsep
                   | mandatory_stmt stmtsep
                   | status_stmt stmtsep
                   | description_stmt stmtsep
                   | reference_stmt stmtsep
                   | short_case_stmt stmtsep
                   | case_stmt stmtsep
                )*
                RIGHT_BRACE
             );

short_case_stmt :   container_stmt
               | leaf_stmt
               | leaf_list_stmt
               | list_stmt
               | anyxml_stmt;

case_stmt : CASE_KEYWORD SEP string SEP?                                    // string validated in YangModelBasicValidationListener.enterCase_stmt()
                (SEMICOLON |
                    LEFT_BRACE stmtsep
                    (    when_stmt stmtsep
                       | if_feature_stmt stmtsep
                       | status_stmt stmtsep
                       | description_stmt stmtsep
                       | reference_stmt stmtsep
                       | data_def_stmt stmtsep
                    )*
                   RIGHT_BRACE
               );

anyxml_stmt : ANYXML_KEYWORD SEP string SEP?                                // string validated in YangModelBasicValidationListener.enterAnyxml_stmt()
                   (SEMICOLON |
                       LEFT_BRACE stmtsep
                           (   when_stmt stmtsep
                             | if_feature_stmt stmtsep
                             | must_stmt stmtsep
                             | config_stmt stmtsep
                             | mandatory_stmt stmtsep
                             | status_stmt stmtsep
                             | description_stmt stmtsep
                             | reference_stmt stmtsep
                           )*
                       RIGHT_BRACE
                   );

uses_stmt : USES_KEYWORD SEP identifier_ref SEP?
                   (SEMICOLON |
                        LEFT_BRACE stmtsep
                          (   when_stmt stmtsep
                            | if_feature_stmt stmtsep
                            | status_stmt stmtsep
                            | description_stmt stmtsep
                            | reference_stmt stmtsep
                            | refine_stmt stmtsep
                            | uses_augment_stmt stmtsep
                          )*
                       RIGHT_BRACE
                   );

refine_stmt : REFINE_KEYWORD SEP string SEP?                                // see @EntryPoint Entry point for parsing refine-arg-str (string validated in YangModelBasicValidationListener.enterRefine_stmt())
                (SEMICOLON |
                    LEFT_BRACE stmtsep
                        (     refine_container_stmts
                            | refine_leaf_stmts
                            | refine_leaf_list_stmts
                            | refine_list_stmts
                            | refine_choice_stmts
                            | refine_case_stmts
                            | refine_anyxml_stmts
                        )
                    RIGHT_BRACE
                );

/**
*
*
* @EntryPoint Entry point for parsing refine-arg-str
*/
refine_arg : descendant_schema_nodeid;

refine_container_stmts : (
                            must_stmt stmtsep
                          | presence_stmt stmtsep
                          | config_stmt stmtsep
                          | description_stmt stmtsep
                          | reference_stmt stmtsep
                         )*;

refine_leaf_stmts : (
                       must_stmt stmtsep
                     | default_stmt stmtsep
                     | config_stmt stmtsep
                     | mandatory_stmt stmtsep
                     | description_stmt stmtsep
                     | reference_stmt stmtsep
                     )*;

refine_leaf_list_stmts : (
                            must_stmt stmtsep
                          | config_stmt stmtsep
                          | min_elements_stmt stmtsep
                          | max_elements_stmt stmtsep
                          | description_stmt stmtsep
                          | reference_stmt stmtsep
                        )*;

refine_list_stmts : (
                       must_stmt stmtsep
                     | config_stmt stmtsep
                     | min_elements_stmt stmtsep
                     | max_elements_stmt stmtsep
                     | description_stmt stmtsep
                     | reference_stmt stmtsep
                     )*;

refine_choice_stmts : (
                        default_stmt stmtsep
                      | config_stmt stmtsep
                      | mandatory_stmt stmtsep
                      | description_stmt stmtsep
                      | reference_stmt stmtsep
                     )*;

refine_case_stmts : (
                      description_stmt stmtsep
                    | reference_stmt stmtsep
                    )*;

refine_anyxml_stmts : (
                        must_stmt stmtsep
                      | config_stmt stmtsep
                      | mandatory_stmt stmtsep
                      | description_stmt stmtsep
                      | reference_stmt stmtsep
                      )*;

uses_augment_stmt : AUGMENT_KEYWORD SEP string SEP?                         // see @EntryPoint Entry point for parsing uses-augment-arg-str (string not validated in source code)
                        LEFT_BRACE stmtsep
                            (
                                when_stmt stmtsep
                              | if_feature_stmt stmtsep
                              | status_stmt stmtsep
                              | description_stmt stmtsep
                              | reference_stmt stmtsep
                              | data_def_stmt stmtsep
                              | case_stmt stmtsep
                            )*
                        RIGHT_BRACE
                        ;
/**
*
*
* @EntryPoint Entry point for parsing uses-augment-arg-str
*/
uses_augment_arg : descendant_schema_nodeid;

augment_stmt : AUGMENT_KEYWORD SEP string SEP?                              // see @EntryPoint Entry point for parsing augment-arg-str (string validated in YangModelBasicValidationListener.enterAugment_stmt())
                LEFT_BRACE stmtsep
                    (
                         when_stmt stmtsep
                       | if_feature_stmt stmtsep
                       | status_stmt stmtsep
                       | description_stmt stmtsep
                       | reference_stmt stmtsep
                       | data_def_stmt stmtsep
                       | case_stmt stmtsep
                    )*
                RIGHT_BRACE
                ;

/**
*
*
* @EntryPoint Entry point for parsing augment-arg-str
*/
augment_arg : absolute_schema_nodeid;

unknown_statement : prefix COLON identifier (SEP string)? SEP?              // string validated in YangParserListenerImpl.handleUnknownNode()
                 (SEMICOLON | LEFT_BRACE SEP? (unknown_statement2 SEP?)* RIGHT_BRACE);

unknown_statement2 : (prefix COLON)? identifier (SEP string)? SEP?          // string validated in YangParserListenerImpl.handleUnknownNode()
                  (SEMICOLON | LEFT_BRACE SEP? (unknown_statement2 SEP?)* RIGHT_BRACE);

when_stmt : WHEN_KEYWORD SEP string SEP?                                    // string validated in ParserListenerUtils.stringFromStringContext()
                     (SEMICOLON |
                        LEFT_BRACE stmtsep
                        (   description_stmt stmtsep
                          | reference_stmt stmtsep
                        )*
                         RIGHT_BRACE
                     );

rpc_stmt : RPC_KEYWORD SEP string SEP?                                      // string valided in YangModelBasicValidationListener.enterRpc_stmt()
                    (SEMICOLON |
                           LEFT_BRACE stmtsep
                           (   if_feature_stmt stmtsep
                             | status_stmt stmtsep
                             | description_stmt stmtsep
                             | reference_stmt stmtsep
                             | typedef_stmt stmtsep
                             | grouping_stmt stmtsep
                             | input_stmt stmtsep
                             | output_stmt stmtsep
                           )*
                           RIGHT_BRACE
                    );

input_stmt : INPUT_KEYWORD SEP?
                 LEFT_BRACE stmtsep
                       (   typedef_stmt stmtsep
                          | grouping_stmt stmtsep
                          | data_def_stmt stmtsep
                       )*
                RIGHT_BRACE;

output_stmt : OUTPUT_KEYWORD SEP?
                LEFT_BRACE stmtsep
                      (    typedef_stmt stmtsep
                         | grouping_stmt stmtsep
                         | data_def_stmt stmtsep
                      )*
               RIGHT_BRACE;

notification_stmt : NOTIFICATION_KEYWORD SEP string SEP?                    // string validated in YangModelBasicValidationListener.enterNotification_stmt()
                           (SEMICOLON |
                                LEFT_BRACE stmtsep
                                    (   if_feature_stmt stmtsep
                                       | status_stmt stmtsep
                                       | description_stmt stmtsep
                                       | reference_stmt stmtsep
                                       | typedef_stmt stmtsep
                                       | grouping_stmt stmtsep
                                       | data_def_stmt stmtsep
                                    )*
                                RIGHT_BRACE
                           );

deviation_stmt : DEVIATION_KEYWORD SEP string SEP?                          // see @EntryPoint Entry point for parsing deviation-arg-str (string validated in YangModelBasicValidationListener.enterDeviation_stmt())
                         LEFT_BRACE stmtsep
                             (   description_stmt stmtsep
                               | reference_stmt stmtsep
                               | deviate_not_supported_stmt stmtsep
                               | deviate_add_stmt stmtsep
                               | deviate_replace_stmt stmtsep
                               | deviate_delete_stmt stmtsep
                             )*
                         RIGHT_BRACE;

/**
*
*
* @EntryPoint Entry point for parsing deviation-arg-str
*/
deviation_arg : absolute_schema_nodeid;

deviate_not_supported_stmt : DEVIATE_KEYWORD SEP string SEP?                // string validated as NOT_SUPPORTED_KEYWORD = 'not-supported' in DeviationBuilder.setDeviate()
                               (SEMICOLON |
                                    LEFT_BRACE stmtsep
                                    RIGHT_BRACE
                               );

deviate_add_stmt : DEVIATE_KEYWORD SEP string  SEP?                         // string validated as ADD_KEYWORD = 'add' in DeviationBuilder.setDeviate()
                              (SEMICOLON |
                                 LEFT_BRACE stmtsep
                                     (   units_stmt stmtsep
                                       | must_stmt stmtsep
                                       | unique_stmt stmtsep
                                       | default_stmt stmtsep
                                       | config_stmt stmtsep
                                       | mandatory_stmt stmtsep
                                       | min_elements_stmt stmtsep
                                       | max_elements_stmt stmtsep
                                     )*
                                 RIGHT_BRACE
                              );

deviate_delete_stmt : DEVIATE_KEYWORD SEP string SEP?                       // string validated as DELETE_KEYWORD = 'delete' in DeviationBuilder.setDeviate()
                              (SEMICOLON |
                                 LEFT_BRACE stmtsep
                                      (    units_stmt stmtsep
                                         | must_stmt stmtsep
                                         | unique_stmt stmtsep
                                         | default_stmt stmtsep
                                      )*
                                 RIGHT_BRACE
                              );

deviate_replace_stmt : DEVIATE_KEYWORD SEP string SEP?                      // string validated as REPLACE_KEYWORD = 'replace' in DeviationBuilder.setDeviate()
                             (SEMICOLON |
                                LEFT_BRACE stmtsep
                                    (    type_stmt
                                       | units_stmt
                                       | default_stmt
                                       | config_stmt
                                       | mandatory_stmt
                                       | min_elements_stmt
                                       | max_elements_stmt
                                    )*
                                RIGHT_BRACE
                             );

/**
 *
 *
 * @Entrypoint Entry point for parsing range-arg-str
 */
range_arg : range_part SEP? (BAR SEP? range_part SEP?)* ;

range_part : range_boundary SEP? (DOTS SEP? range_boundary SEP?)?;

range_boundary : MIN_KEYWORD | MAX_KEYWORD | integer_value | DECIMAL_VALUE;

integer_value : non_negative_integer_value | NEGATIVE_INTEGER_VALUE | UNQUOTED_NEGATIVE_INTEGER_VALUE;

/**
 *
 *
 * @Entrypoint Entry point for parsing length-arg-str
 */
length_arg : length_part SEP? (BAR SEP? length_part SEP?)* ;

length_part : length_boundary SEP? (DOTS SEP? length_boundary SEP?)?;

length_boundary : MIN_KEYWORD | MAX_KEYWORD | non_negative_integer_value;

/**
 *
 *
 * @Entrypoint Entry point for parsing date-arg-str
 */
date_arg : DATE_ARG;

/**
 *
 *
 * @Entrypoint Entry point for parsing path-arg-str
 */
path_arg : absolute_path | relative_path;

absolute_path : (SLASH node_identifier (path_predicate)*)+; 

relative_path : (DOTS SLASH)* descendant_path;

descendant_path : node_identifier ((path_predicate)* absolute_path)?;

path_predicate : LEFT_SQUARE_BRACKET SEP? path_equality_expr SEP? RIGHT_SQUARE_BRACKET;

path_equality_expr : node_identifier SEP? EQUAL SEP? path_key_expr;

path_key_expr : current_function_invocation SEP? SLASH SEP? rel_path_keyexpr;

rel_path_keyexpr : (DOTS SEP? SLASH SEP?)* (node_identifier SEP? SLASH SEP?)* node_identifier;

node_identifier : (prefix COLON)? identifier;

current_function_invocation : CURRENT_KEYWORD SEP? LEFT_PARENTHESIS SEP? RIGHT_PARENTHESIS;

descendant_schema_nodeid :  node_identifier
                            absolute_schema_nodeid;

absolute_schema_nodeid : (SLASH node_identifier)+;

prefix : identifier;

non_negative_integer_value : ZERO | POSITIVE_INTEGER_VALUE | UNQUOTED_ZERO | UNQUOTED_POSITIVE_INTEGER_VALUE;

/**
* Represents identifier statement in YANG ABNF grammar.
* Definition is not 100% exact as in YANG grammar, since tokenizer in ANTLR  prefers direct matching of keywords
* and emmiting them as tokens so any of YANG keywords never matches IDENTIFIER token.
*
* That is why idenfier is defined as IDENTIFIER or any keyword (see any_keyword)  since keywords in YANG are not
* reserved words and could be used as IDENTIFIER.
*
* Example:
*
* "description:description;" should match unknown_statement (and not description_stmt) with following
* substatements:
* prefix : "description"
* identifier : description;
*
* @Workaround Definition is changed to workaround not-direct mapping of ABNF rule procesing to ANTLR rule processing.
**/
identifier: IDENTIFIER | any_keyword;

/**
* Matches any YANG keyword. This statement does not exitst in RFC6020 grammar and exists only because of how ANTLR
* tokenizer work and we are using it - tokenizer emits keywords as concrete tokens and it is not possible to make
* this string be keyword and identifier at a same time.
*
* @Workaround : This is workaround for IDENTIFIER not matching any of keywords. See identifier for explanation.
* This list must contain all YANG keywords, with OR relationship.
*
*/
any_keyword:   YIN_ELEMENT_KEYWORD
          | YANG_VERSION_KEYWORD
          | WHEN_KEYWORD
          | VALUE_KEYWORD
          | USES_KEYWORD
          | UNITS_KEYWORD
          | UNIQUE_KEYWORD
          | TYPEDEF_KEYWORD
          | TYPE_KEYWORD
          | SUBMODULE_KEYWORD
          | STATUS_KEYWORD
          | RPC_KEYWORD
          | REVISION_DATE_KEYWORD
          | REVISION_KEYWORD
          | REQUIRE_INSTANCE_KEYWORD
          | REFINE_KEYWORD
          | REFERENCE_KEYWORD
          | RANGE_KEYWORD
          | PRESENCE_KEYWORD
          | PREFIX_KEYWORD
          | POSITION_KEYWORD
          | PATTERN_KEYWORD
          | PATH_KEYWORD
          | OUTPUT_KEYWORD
          | ORGANIZATION_KEYWORD
          | ORDERED_BY_KEYWORD
          | NOTIFICATION_KEYWORD
          | NAMESPACE_KEYWORD
          | MUST_KEYWORD
          | MODULE_KEYWORD
          | MIN_ELEMENTS_KEYWORD
          | MAX_ELEMENTS_KEYWORD
          | MANDATORY_KEYWORD
          | LIST_KEYWORD
          | LENGTH_KEYWORD
          | LEAF_LIST_KEYWORD
          | LEAF_KEYWORD
          | KEY_KEYWORD
          | INPUT_KEYWORD
          | INCLUDE_KEYWORD
          | IMPORT_KEYWORD
          | IF_FEATURE_KEYWORD
          | IDENTITY_KEYWORD
          | GROUPING_KEYWORD
          | FRACTION_DIGITS_KEYWORD
          | FEATURE_KEYWORD
          | DEVIATE_KEYWORD
          | DEVIATION_KEYWORD
          | EXTENSION_KEYWORD
          | ERROR_MESSAGE_KEYWORD
          | ERROR_APP_TAG_KEYWORD
          | ENUM_KEYWORD
          | DESCRIPTION_KEYWORD
          | DEFAULT_KEYWORD
          | CONTAINER_KEYWORD
          | CONTACT_KEYWORD
          | CONFIG_KEYWORD
          | CHOICE_KEYWORD
          | CASE_KEYWORD
          | BIT_KEYWORD
          | BELONGS_TO_KEYWORD
          | BASE_KEYWORD
          | AUGMENT_KEYWORD
          | ARGUMENT_KEYWORD
          | ANYXML_KEYWORD
          | CURRENT_KEYWORD
          | MIN_KEYWORD
          | MAX_KEYWORD
          | UNBOUNDED_KEYWORD;

stmtend : SEP? SEMICOLON | SEP? LEFT_BRACE SEP? (unknown_statement SEP?)* RIGHT_BRACE;

stmtsep : (unknown_statement | SEP)*;

identifier_ref: (prefix COLON)? identifier | string;

string : STRING (SEP? PLUS SEP? STRING)* | (SLASH)? (IDENTIFIER COLON)? IDENTIFIER (SLASH (IDENTIFIER COLON)? IDENTIFIER)*
       | DEF_MODE_STRING | DATE_ARG | ZERO | NEGATIVE_INTEGER_VALUE | POSITIVE_INTEGER_VALUE | DECIMAL_VALUE
       | UNQUOTED_ZERO | UNQUOTED_NEGATIVE_INTEGER_VALUE | UNQUOTED_POSITIVE_INTEGER_VALUE | UNQUOTED_DECIMAL_VALUE;