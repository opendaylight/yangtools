/**
 * (C)2015 Brocade Communications Systems, Inc and others
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */



package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;
import com.google.common.base.Preconditions;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.io.Serializable;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Arrays;
import java.beans.ConstructorProperties;
import com.google.common.primitives.Ints;
import java.net.Inet4Address;
import java.net.InetAddress;
import com.google.common.net.InetAddresses;

/**
 * The ipv4-prefix type represents an IPv4 address prefix.
 *        The prefix length is given by the number following the
 *        slash character and must be less than or equal to 32.
 *
 *        A prefix length value of n corresponds to an IP address
 *        mask that has n contiguous 1-bits from the most
 *        significant bit (MSB) and all other bits set to 0.
 *
 *        The canonical format of an IPv4 prefix has all bits of
 *        the IPv4 address set to zero that are not part of the
 *        IPv4 prefix.
 *
 *        We are overriding the generation for this class because we 
 *        need binary form for efficient prefix matches
 *
 */
public class Ipv4Prefix
 implements Serializable {
    private static final long serialVersionUID = -8184825603273274556L;
    private static final List<Pattern> patterns;
    public static final List<String> PATTERN_CONSTANTS = ImmutableList.of("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])/(([0-9])|([1-2][0-9])|(3[0-2]))$");
    final private static int INADDR4SZ = 4;

    static {
        final List<Pattern> l = new ArrayList<Pattern>();
        for (String regEx : PATTERN_CONSTANTS) {
            l.add(Pattern.compile(regEx));
        }
    
        patterns = ImmutableList.copyOf(l);
    }

    final private byte [] _binary_form;

    final private int _netmask;

    public Ipv4Prefix(java.lang.String _value) {

        Preconditions.checkNotNull(_value, "Supplied value may not be null");

        boolean valid = false;
        for (Pattern p : patterns) {
            if (p.matcher(_value).matches()) {
                valid = true;
                break;
            }
        }

        String [] address =  _value.split("/");

        int mask = 32;

        try {
            mask = Integer.parseInt(address[1]);
            if (mask > 32) {
                valid = false;
            }
        } catch (NumberFormatException e) {
            valid = false;
        }


        Preconditions.checkArgument(valid, "Supplied value \"%s\" does not match any of the permitted patterns %s", _value, PATTERN_CONSTANTS);

        /*
        * our real IPv4 prefix presentation - we may consider replacing this with int at
        * a later date. Depends on how it is being (ab)used downstream.
        *
        * As we do not have canonicalization issues here we can use forString
        */


        InetAddress _inet_form = InetAddresses.forString(address[0]);

        Preconditions.checkArgument(_inet_form instanceof Inet4Address);

        /* 
         * RFC specifies that all non-zero bits in the prefix past netmask should be cleared for
         * canonical form
         */

        byte [] _non_canonical = _inet_form.getAddress();

        canonicalizeIpv4Prefix(_non_canonical, mask);

        this._binary_form =  _non_canonical;
        this._netmask = mask;
    }

    /**
     * Canonicalize a v4 prefix
     *
     * @param _prefix - prefix, in byte [] form
     * @param mask - mask - number of bits
     */

    static public void canonicalizeIpv4Prefix(byte [] _prefix, int mask) {

        /* We may convert this to a table lookup later, it is daft how two 
         * rep-prefixed instruction available in assembler on nearly any architecture 
         * (shr with 1 fill and logical and) needs 10 lines of code in a high level
         * language
         */

        int i;
        long slash1 = 0x80000000; /* 1 bit in leftmost address byte */
        long address = 0;
        for (i = 0; i <= mask; i++) {
            address = slash1 | (address >> 1);
        }

        byte [] _mask = Ints.toByteArray((int) address);

        for (i=0; i < INADDR4SZ; i++) {
            _prefix[i] = (byte) (_prefix[i] & _mask[i]);
        }
    }

    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */

    public Ipv4Prefix(Ipv4Prefix source) {
        this._binary_form = source._binary_form;
        this._netmask = source._netmask;
    }

    /**
     * Creates an Ipv4Prefix from binary (routing protocol) representation
     *
     * @param _binary_form ip address as byte array
     */

    public Ipv4Prefix(byte [] _binary_form, int _netmask ) {
        /* check onjava.lang.ly length */
        Preconditions.checkArgument(
            (_binary_form.length == 4),
            "Supplied binary value \"%d\" is not the correct length",
            _binary_form.length
        );
        Preconditions.checkArgument(
            ((_netmask > 32) || (_netmask < 0)),
            "Supplied binary value \"%d\" is not the correct length",
            _binary_form.length
        );
        canonicalizeIpv4Prefix(_binary_form, _netmask);
        this._binary_form = _binary_form;
        this._netmask = _netmask;
    }

    public static Ipv4Address getDefaultInstance(String defaultValue) {
        return new Ipv4Address(defaultValue);
    }

    /**
     * @return binary form as used for comparisons
     */

    public byte [] getBinaryForm() {
        return this._binary_form;
    }

    /**
     * @return netmask
     */

    public int getNetMask() {
        return this._netmask;
    }

    /* This warrants a comment on James Gosling opinion on why Java
     * should not have any signed types. Or maybe not
     */

    private int unsigned_byte(byte arg) {
        return (arg & 0xff);
    }

    public java.lang.String getValue() {
        /* fake the "expected" value by rebuilding string out of binary */
        StringBuilder sb = new java.lang.StringBuilder();
        sb.append(
            String.format(
                "%d.%d.%d.%d/%d",
                unsigned_byte(_binary_form[0]),
                unsigned_byte(_binary_form[1]),
                unsigned_byte(_binary_form[2]),
                unsigned_byte(_binary_form[3]),
                this._netmask
            )
        );
        return sb.toString();

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = this._netmask;
        result = prime * result + ((_binary_form == null) ? 0 : Arrays.hashCode(_binary_form));
        return result;
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Ipv4Prefix other = (Ipv4Prefix) obj;
        if(! Arrays.equals(this._binary_form , other._binary_form)) {
            return false;
        }
 
        /* address portions are equal, return equality of masks */

        return (this._netmask == other._netmask); 
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix.class.getSimpleName()).append(" [");

        builder.append(this.getValue());

        return builder.append(']').toString();
    }
}
