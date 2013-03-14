/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource.query;

import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.dsl.AntLRUtils;
import com.kloudtek.systyrant.dsl.LogOp;
import com.kloudtek.systyrant.dsl.SystyrantLangParser;
import com.kloudtek.systyrant.exception.InvalidQueryException;
import com.kloudtek.systyrant.resource.Resource;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import static com.kloudtek.systyrant.dsl.LogOp.*;
import static com.kloudtek.util.StringUtils.isEmpty;

/**
 * <p>Matches resource attribute.</p>
 */
public class AttrMatchExpression extends Expression {
    private LogOp logOp;
    private String val;
    private Pattern valPattern;
    private final String attr;
    private boolean not;

    public AttrMatchExpression(SystyrantLangParser.QueryAttrMatchContext attrMatch, String query, STContext context) throws InvalidQueryException {
        attr = attrMatch.attr.getText();
        if (isEmpty(attr)) {
            throw new InvalidQueryException(attrMatch.attr.getStart(), query);
        }
        if (attrMatch.nul != null) {
            not = attrMatch.nul.n != null;
            if (attrMatch.nul.nul != null) {
                logOp = ISNULL;
            } else if (attrMatch.nul.empty != null) {
                logOp = EMPTY;
            } else {
                throw new RuntimeException("BUG! couldn't match " + attrMatch.nul.getText());
            }
        } else {
            not = attrMatch.nnul.n != null;
            logOp = LogOp.valueOf(attrMatch.nnul.op, query);
            val = AntLRUtils.toString(attrMatch.nnul.val);
            if (logOp == REGEX) {
                valPattern = Pattern.compile(val);
            }
        }
    }

    @Override
    public boolean matches(STContext context, Resource resource) {
        String attrVal = resource.get(attr);
        boolean result = eval(attrVal);
        return not ? !result : result;
    }

    private boolean eval(String attrVal) {
        switch (logOp) {
            case EMPTY:
                return isEmpty(attrVal);
            case ISNULL:
                return attrVal == null;
            case EQ:
                return val.equals(attrVal);
            case LIKE:
                return val.equalsIgnoreCase(attrVal);
            case REGEX:
                return valPattern.matcher(attrVal).find();
            case GT:
                return compareStr(attrVal, val);
            case LT:
                return compareStr(val, attrVal);
            default:
                throw new RuntimeException("BUG! invalid attr operator: " + logOp);
        }
    }

    private static boolean compareStr(String attrVal, String val) {
        if (val == null && attrVal != null) {
            return true;
        } else if (val != null && attrVal == null) {
            return false;
        } else {
            try {
                BigDecimal v = new BigDecimal(val);
                BigDecimal av = new BigDecimal(attrVal);
                return v.compareTo(av) < 0;
            } catch (NumberFormatException e) {
                return val.compareTo(attrVal) < 0;
            }
        }
    }
}
