/**
 *
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
*
* References ABNF rule defined in RFC6020 - YANG Specification.
*
* <p>
* An interface / class annotated with this annotation
* is Java representation of data represented by ABNF rule
* provided as {@link #value()}. Java representation
* does not need to be direct,
* but must retain all information in some, publicly
* accessible form for consumers.
* </p>
* <p>
* Note that this annotation is used currently only for documentation
* and does not affect any runtime behaviour.
* </p>
*
*/
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface Rfc6020AbnfRule {

    String[] value();
}
