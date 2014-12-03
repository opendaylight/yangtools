//
// Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
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
 *      E.g. ABNF: module-stmt, ANTLR module-stmt
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
package antlr4.generated.grammar;
} 

options{
    tokenVocab=RFC6020YangLexer;
    
}


/**
 *
 *
 * @EntryPoint Entry point for parsing YANG modules and submodules.
 */
yang : module_stmt | submodule_stmt ;

module_stmt : MODULE_KEYWORD SEP string 
              LEFT_BRACE stmtsep
                  module_header_stmts
                  linkage_stmts
                  meta_stmts
                  revision_stmts
                  body_stmts
              RIGHT_BRACE;

submodule_stmt : SUBMODULE_KEYWORD SEP string 
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
//				| augment_stmt 
//				| rpc_stmt 
//				| notification_stmt 
//				| deviation_stmt
                )
                stmtsep)*;

data_def_stmt :   container_stmt 
//                | leaf_stmt 
//                | leaf_list_stmt 
//                  | list_stmt
//                | choice_stmt 
//                | anyxml_stmt 
//                | uses_stmt
                ;

yang_version_stmt : YANG_VERSION_KEYWORD SEP string stmtend;

import_stmt : IMPORT_KEYWORD SEP string 
              LEFT_BRACE stmtsep
                  prefix_stmt stmtsep
                  (revision_date_stmt stmtsep)?
              RIGHT_BRACE;

include_stmt : INCLUDE_KEYWORD SEP string 
               (SEMICOLON | 
                LEFT_BRACE stmtsep
                    (revision_date_stmt stmtsep)?
                RIGHT_BRACE);

namespace_stmt : NAMESPACE_KEYWORD SEP string stmtend;

prefix_stmt : PREFIX_KEYWORD SEP string stmtend;

belongs_to_stmt : BELONGS_TO_KEYWORD SEP string 
                  LEFT_BRACE stmtsep
                      prefix_stmt stmtsep
                  RIGHT_BRACE;

organization_stmt : ORGANIZATION_KEYWORD SEP string stmtend;

contact_stmt : CONTACT_KEYWORD SEP string stmtend;

description_stmt : DESCRIPTION_KEYWORD SEP string stmtend;

reference_stmt : REFERENCE_KEYWORD SEP string stmtend;

units_stmt : UNITS_KEYWORD SEP string stmtend;

revision_stmt : REVISION_KEYWORD SEP string 
                (SEMICOLON |
                 LEFT_BRACE stmtsep
                     (description_stmt stmtsep)?
                     (reference_stmt stmtsep)?
                 RIGHT_BRACE);

revision_date_stmt : REVISION_DATE_KEYWORD SEP string stmtend;

extension_stmt : EXTENSION_KEYWORD SEP string 
                 (SEMICOLON |
                    LEFT_BRACE stmtsep
                        (   argument_stmt stmtsep
                          | status_stmt stmtsep
                          | description_stmt stmtsep
                          | reference_stmt stmtsep
                        )*
                    RIGHT_BRACE
                 );

argument_stmt : ARGUMENT_KEYWORD SEP string 
                (SEMICOLON |
                 LEFT_BRACE stmtsep
                     (yin_element_stmt stmtsep)?
                 RIGHT_BRACE
                );

yin_element_stmt : YIN_ELEMENT_KEYWORD SEP yin_element_arg stmtend;		

yin_element_arg : string; // TRUE_KEYWORD | FALSE_KEYWORD;		

identity_stmt : IDENTITY_KEYWORD SEP string 
                (SEMICOLON | 
                 LEFT_BRACE stmtsep 
                     (   base_stmt stmtsep 
                       | status_stmt stmtsep
                       | description_stmt stmtsep
                       | reference_stmt stmtsep
                     )* 
                 RIGHT_BRACE
                );

base_stmt : BASE_KEYWORD SEP string stmtend;

feature_stmt : FEATURE_KEYWORD SEP string 
                (SEMICOLON |
                 LEFT_BRACE stmtsep
                    (   if_feature_stmt stmtsep
                      | status_stmt stmtsep
                      | description_stmt stmtsep
                      | reference_stmt stmtsep
                    )*
                 RIGHT_BRACE
                );

if_feature_stmt : IF_FEATURE_KEYWORD SEP string stmtend;

typedef_stmt : TYPEDEF_KEYWORD SEP string 
               LEFT_BRACE stmtsep 
                  (   type_stmt stmtsep 
                    | units_stmt stmtsep
                    | default_stmt stmtsep
                    | status_stmt stmtsep
                    | description_stmt stmtsep
                    | reference_stmt stmtsep
                  )+ 
               RIGHT_BRACE;

type_stmt : TYPE_KEYWORD SEP string 
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

range_stmt : RANGE_KEYWORD SEP string 
             (SEMICOLON | 
              LEFT_BRACE stmtsep 
                  (  // identifier_stmt stmtsep 
                     error_message_stmt stmtsep
                    | error_app_tag_stmt stmtsep
                    | description_stmt stmtsep
                    | reference_stmt stmtsep
                  )* 
              RIGHT_BRACE
             );

decimal64_specification : fraction_digits_stmt;

fraction_digits_stmt : FRACTION_DIGITS_KEYWORD SEP string stmtend;

string_restrictions : (length_stmt | pattern_stmt )*;

length_stmt : LENGTH_KEYWORD SEP string 
              (SEMICOLON | 
               LEFT_BRACE stmtsep 
                  (   error_message_stmt stmtsep
                    | error_app_tag_stmt stmtsep
                    | description_stmt stmtsep
                    | reference_stmt stmtsep
                  )* 
                RIGHT_BRACE 
              );

pattern_stmt : PATTERN_KEYWORD SEP string 
               (SEMICOLON | 
                LEFT_BRACE stmtsep
                    (   error_message_stmt stmtsep
                      | error_app_tag_stmt stmtsep
                      | description_stmt stmtsep
                      | reference_stmt stmtsep
                    )*
                 RIGHT_BRACE
               );
               
default_stmt : DEFAULT_KEYWORD SEP string stmtend;

enum_specification : enum_stmt stmtsep;       

enum_stmt : ENUM_KEYWORD SEP string 
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

path_stmt : PATH_KEYWORD SEP string stmtend;

require_instance_stmt : REQUIRE_INSTANCE_KEYWORD SEP require_instance_arg stmtend;

require_instance_arg :string; // TRUE_KEYWORD | FALSE_KEYWORD;

instance_identifier_specification : (require_instance_stmt stmtsep)?;

identityref_specification : base_stmt stmtsep;

union_specification : type_stmt stmtsep;

bits_specification : bit_stmt stmtsep;

bit_stmt : BIT_KEYWORD SEP string 
           (SEMICOLON | 
            LEFT_BRACE  stmtsep 
                 (   position_stmt stmtsep 
                   | status_stmt stmtsep
                   | description_stmt stmtsep
                   | reference_stmt stmtsep
                 )* 
            RIGHT_BRACE
           );

position_stmt : POSITION_KEYWORD SEP string stmtend;

status_stmt : STATUS_KEYWORD SEP status_arg stmtend;

status_arg : string; /*CURRENT_KEYWORD | OBSOLETE_KEYWORD | DEPRECATED_KEYWORD; */

config_stmt : CONFIG_KEYWORD SEP config_arg stmtend;

config_arg : string; //  TRUE_KEYWORD | FALSE_KEYWORD;

mandatory_stmt : MANDATORY_KEYWORD SEP mandatory_arg stmtend;

mandatory_arg :string; // TRUE_KEYWORD | FALSE_KEYWORD;

presence_stmt : PRESENCE_KEYWORD SEP string stmtend;

ordered_by_stmt : ORDERED_BY_KEYWORD SEP ordered_by_arg stmtend;

ordered_by_arg : string; /*USER_KEYWORD | SYSTEM_KEYWORD;*/

must_stmt : MUST_KEYWORD SEP string 
            (SEMICOLON | 
             LEFT_BRACE stmtsep  
                (  //  identifier_stmt stmtsep 
                    error_message_stmt stmtsep 
                   | error_app_tag_stmt stmtsep
                   | description_stmt stmtsep
                   | reference_stmt stmtsep
                )* 
             RIGHT_BRACE
            );

error_message_stmt : ERROR_MESSAGE_KEYWORD SEP string stmtend;

error_app_tag_stmt : ERROR_APP_TAG_KEYWORD SEP string stmtend;

min_elements_stmt : MIN_ELEMENTS_KEYWORD SEP min_value_arg stmtend;

min_value_arg : /*UNBOUNDED_KEYWORD |*/ string;

max_elements_stmt : MAX_ELEMENTS_KEYWORD SEP max_value_arg stmtend;

max_value_arg : /*UNBOUNDED_KEYWORD |*/ string;

value_stmt : VALUE_KEYWORD SEP string stmtend;

grouping_stmt : GROUPING_KEYWORD SEP string 
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

container_stmt : CONTAINER_KEYWORD SEP string 
                 (SEMICOLON |
                  LEFT_BRACE  stmtsep
                       (
                         //when_stmt stmtsep
                          if_feature_stmt stmtsep
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


unknown_statement : prefix COLON identifier (SEP string)?
                    (SEMICOLON | LEFT_BRACE unknown_statement2* RIGHT_BRACE);
                    
unknown_statement2 : (prefix COLON)? identifier (SEP string)? 
                     (SEMICOLON | LEFT_BRACE unknown_statement2* RIGHT_BRACE);

prefix : identifier;

/**
 * Represents identifier statement in YANG ABNF grammar.
 * Definition is not 100% exact as in YANG grammar, since tokenizer in ANTLR  prefers direct matching of keywords
 * and emmiting them as token so any of YANG keywords never matches IDENTIFIER token.
 *
 * That is why idenfier is defined as IDENTIFIER or any keyword (see any_keyword)  since keywords in YANG are not
 * reserved words and could be used as IDENTIFIER.
 *
 * Example:
 *
 * "description:description;" should match unknown_statement (and not description_stmtm) with following
 * substatements:
 * prefix : "description"
 * identifier : description;
 *
 * @Workaround Definition is changed to workaround not-direct mapping of ABNF rule procesing to ANTLR rule processing.
 **/
identifier: IDENTIFIER | any_keyword;

/**
 * Matches any YANG keyword. This statement does not exitst in RFC6020 grammar and exists only because of how ANTLR
 * tokenizer work and we are using it - tokenizers emits keywords as concrete tokens and it is not possible to make
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
             | ANYXML_KEYWORD;

stmtend : SEMICOLON | LEFT_BRACE unknown_statement* RIGHT_BRACE;

stmtsep : unknown_statement*;

string : STRING (PLUS STRING)*;





