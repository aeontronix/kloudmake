/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.FQName;
import com.kloudtek.systyrant.STContext;
import com.kloudtek.systyrant.exception.InvalidAttributeException;
import com.kloudtek.systyrant.exception.InvalidRefException;
import com.kloudtek.systyrant.exception.STException;
import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represent a reference to one of more elements, in a text format.
 * <p>That string has the format of <i>TYPE</i>:<i>VALUE</i>. Multiple matchers can be used, by using the '|' symbol.
 * <p>The following characters will require to be escape with a '\' : '|' , '\' so to have the string 'a|b\c', you'll need to write 'a\|b\\c'</p>
 * <p>The following reference types are supported:</p>
 * <ul>
 * <li><b>uid</b>: This will match any resource with the uid specified in reference value</li>
 * <li><b>id</b>: This will match any resource with the id specified in reference value.</li>
 * <li><b>value</b>: This will match any resource with the resource value (see {@link com.kloudtek.systyrant.FQName} for it's format)
 * specified in reference value. If the namespace if not specified, it will first try to use the namespace from the
 * resolvingElement (see {@link #resolveMultiple(com.kloudtek.systyrant.STContext, Resource)}), or if not set it will try to use resource
 * scope {@link com.kloudtek.systyrant.STContext#currentResource()}.</li>
 * <li><b>@value</b>: The same as 'value', with the exception that should no resource match the specified value, it will
 * automatically create one.
 * <li><b>attr</b>: This will attempt to match again the value of an attribute. The format of the resource ref must
 * be in the format of <i>ATTRNAME</i> <i>OPERATOR</i> <i>ATTRVALUE</i>.
 * <li><b>expr</b>: This allows to use a <a href="http://commons.apache.org/jexl/">JEXL</a> expression to evaluate. The
 * resource will be set as the 'el' variable. Also the function attr() and var() allow to retrieve attributes and variables.
 * </ul>
 * The supported operators for 'attr' and 'var' references types are:
 * <dl>
 * <dt>'=' or 'eq'</dt>
 * <dd>Case insensitive Equals</dd>
 * <dt>'==' or 'seq'</dt>
 * <dd>Case sensitive equals</dd>
 * <dt>'!=' or 'neq'</dt>
 * <dd>Not equals</dd>
 * <dt>'<' or 'lt'</dt>
 * <dd>Less than</dd>
 * <dt>'>' or 'gt'</dt>
 * <dd>Greater than</dd>
 * <dt>'<=' or 'lte'</dt>
 * <dd>Less than or equals</dd>
 * <dt>'>=' or 'gte'</dt>
 * <dd>Greater than or equals</dd>
 * <dt>'sw'</dt>
 * <dd>Starts With</dd>
 * <dt>'ew'</dt>
 * <dd>End With</dd>
 * <dt>'regex'</dt>
 * <dd>Regex pattern find</dd>
 * <dt>'regexm'</dt>
 * <dd>Regex pattern match (must match the whole value)</dd>
 * <dt>'isnull'</dt>
 * <dd>Used to check if the value is specifically null or not-null. The value must be either 'true' if null is expected, or 'false' for non-null values</dd>
 * </dl>
 * <p>Examples:</p>
 * <p>
 * <b>uid:somefile</b> Matches the resource with the uid 'somefile'</br>
 * <b>uid : somefile</b> Also matches the resource with the uid 'somefile'</br>
 * <b>id:somefile</b> Matches the resource with the id</br>
 * <b>value:core:file</b> Matches all elements of value 'file' from the core namespace<br/>
 * <b>attr:path=/hello</b> Matches all elements with an attribute 'path' which has a value equals
 * </p>
 */
public class ResourceRef {
    private static LookupTranslator escapeTranslator = new LookupTranslator(new String[][]{{"\\\\", "\\"}, {"\\|", "|"}});
    private static final Pattern REGEX_REF = Pattern.compile("^(uid|id|type|@type|attr|expr)\\s*?:\\s*?(.*)");
    private static final Pattern REGEX_MATCH = Pattern.compile("^\\s*?([a-z0-9\\-_]*)\\s*?(>=|<=|eq|=|==|neq|!=|lt|<|lte|gt|>|gte|sw|ew|regex|regexm|isnull)\\s*(.*)");
    private static final Logger logger = LoggerFactory.getLogger(ResourceRef.class);
    private String ref;
    private Matcher[] matchers;

    public ResourceRef() {
        matchers = new Matcher[0];
    }

    public ResourceRef(String ref) throws InvalidRefException {
        setRef(ref);
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) throws InvalidRefException {
        this.ref = ref;
        parse(ref);
    }

    private void parse(String ref) throws InvalidRefException {
        ArrayList<Matcher> list = new ArrayList<>();
        List<String> matchList = split(ref);
        for (String statement : matchList) {
            Matcher m;
            try {
                java.util.regex.Matcher regexMatch = REGEX_REF.matcher(statement.trim());
                if (!regexMatch.find()) {
                    throw new IllegalArgumentException();
                }
                String value = regexMatch.group(2);
                String matchType = regexMatch.group(1);
                // TODO Cache matchers
                switch (matchType.toLowerCase()) {
                    case "uid":
                        m = new ComplexMatcher("uid", value);
                        break;
                    case "id":
                        m = new ComplexMatcher("id", value);
                        break;
                    case "type":
                        m = new TypeMatcher(value, false);
                        break;
                    case "+type":
                        m = new TypeMatcher(value, true);
                        break;
                    case "attr":
                        m = new ComplexMatcher(value);
                        break;
                    case "expr":
                        m = new ExprMatcher(value);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid match type " + matchType);
                }
                list.add(m);
            } catch (IllegalArgumentException e) {
                throw new InvalidRefException(logger, "Invalid reference '" + ref + "': " + e.getMessage());
            }
        }
        matchers = list.toArray(new Matcher[list.size()]);
    }

    private List<String> split(String ref) {
        List<String> matchList = new ArrayList<>();
        StringBuilder tmp = new StringBuilder();
        boolean escaping = false;
        for (char c : ref.toCharArray()) {
            if (c == '|' && !escaping) {
                if (tmp.length() > 0) {
                    matchList.add(tmp.toString());
                    tmp = new StringBuilder();
                }
            } else {
                tmp.append(c);
            }
            if ((c == '\\' && !escaping) || escaping) {
                escaping = !escaping;
            }
        }
        if (tmp.length() > 0) {
            matchList.add(tmp.toString());
        }
        return matchList;
    }

    @Override
    public String toString() {
        return ref;
    }

    public List<Resource> resolveMultiple(@NotNull STContext ctx, @Nullable Resource resolvingResource) throws InvalidRefException {
        logger.debug("Resolving ref {}", ref);
        ArrayList<Resource> list = new ArrayList<>();
        for (Resource resource : ctx.getResources()) {
            for (Matcher matcher : matchers) {
                if (matcher.match(ctx, resource, resolvingResource)) {
                    list.add(resource);
                    break;
                }
            }
        }
        if (list.isEmpty()) {
            Resource autocreated = null;
            for (Matcher matcher : matchers) {
                autocreated = matcher.autocreate(ctx);
                if (autocreated != null) {
                    break;
                }
            }
            if (autocreated != null) {
                list.add(autocreated);
            }
        }
        return list;
    }

    public Resource resolveFirst(STContext ctx, @Nullable Resource resolvingResource) throws InvalidRefException {
        Iterator<Resource> iterator = resolveMultiple(ctx, resolvingResource).iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    public Resource resolveSingle(STContext ctx, @Nullable Resource resolvingResource) throws InvalidRefException {
        List<Resource> list = null;
        list = resolveMultiple(ctx, resolvingResource);
        if (list.size() > 1) {
            throw new InvalidRefException(logger, "Ref " + ref + " matches more than one resource");
        }
        return list.isEmpty() ? null : list.get(0);
    }

    private enum MatchOp {
        EQ("="), SEQ("=="), ISNULL(null), NEQ("!="), LT("<"), GT(">"), LTE("<="), GTE(">="), SW(null), EW(null), REGEX(null), REGEXM(null);
        private String alt;

        private MatchOp(String alt) {
            this.alt = alt;
        }

        static MatchOp toOp(String txt) {
            for (MatchOp op : values()) {
                if (op.toString().equalsIgnoreCase(txt) ||
                        (op.alt != null && op.alt.equalsIgnoreCase(txt))) {
                    return op;
                }
            }
            throw new IllegalArgumentException(txt + " is not a valid comparison operator");
        }
    }

    private interface Matcher {
        boolean match(STContext ctx, Resource resource, @Nullable Resource resolvingResource) throws InvalidRefException;

        Resource autocreate(STContext ctx) throws InvalidRefException;
    }

    private class TypeMatcher implements Matcher {
        private FQName type;
        private final boolean autocreate;

        private TypeMatcher(String type, boolean autocreate) {
            this.autocreate = autocreate;
            this.type = new FQName(type);
        }

        @Override
        public boolean match(STContext ctx, Resource resource, @Nullable Resource resolvingResource) throws InvalidRefException {

            return type.equals(resource.getFactory().getFQName());
        }

        @Override
        public Resource autocreate(STContext ctx) throws InvalidRefException {
            if (autocreate) {
                logger.debug("Autocreating resource for ref {}", ref);
                try {
                    return ctx.getResourceManager().createResource(type, null, null);
                } catch (STException e) {
                    throw new InvalidRefException("Failed to auto-create " + type.toString() + ": " + e.getMessage());
                }
            } else {
                return null;
            }
        }
    }

    private class ComplexMatcher implements Matcher {
        private String name;
        private MatchOp op;
        private String value;
        private Pattern regexPattern;

        private ComplexMatcher(String complexMatchString) throws InvalidRefException {
            java.util.regex.Matcher m = REGEX_MATCH.matcher(complexMatchString);
            if (!m.find()) {
                throw new IllegalArgumentException();
            }
            name = m.group(1);
            op = MatchOp.toOp(m.group(2));
            value = escapeTranslator.translate(m.group(3));
            if (op == MatchOp.REGEX) {
                try {
                    regexPattern = Pattern.compile(value);
                } catch (PatternSyntaxException e) {
                    throw new InvalidRefException(value + " is not a valid regex string");
                }
            }
        }

        public ComplexMatcher(String key, String value) {
            name = key;
            op = MatchOp.EQ;
            this.value = value;
        }

        @Override
        public Resource autocreate(STContext ctx) throws InvalidRefException {
            return null;
        }

        @Override
        public boolean match(STContext ctx, Resource resource, @Nullable Resource resolvingResource) throws InvalidRefException {
            String v = resource.get(name);
            switch (op) {
                case EQ:
                    return value.equalsIgnoreCase(v);
                case NEQ:
                    return !value.equalsIgnoreCase(v);
                case SEQ:
                    return value.equals(v);
                case REGEX:
                    return regexPattern.matcher(v).find();
                case REGEXM:
                    return regexPattern.matcher(v).matches();
                case EW:
                    return value.toLowerCase().endsWith(v);
                case GT:
                    return compareString(v) < 0;
                case GTE:
                    return compareString(v) <= 0;
                case LT:
                    return compareString(v) > 0;
                case LTE:
                    return compareString(v) >= 0;
                case ISNULL:
                    if (value.toLowerCase().equals("true") || value.equals("1")) {
                        return v == null;
                    } else {
                        return v != null;
                    }
                default:
                    throw new InvalidRefException(logger, "Unsupported operator " + op.toString());
            }
        }

        private int compareString(String v) {
            try {
                BigDecimal valueNumber = new BigDecimal(value);
                BigDecimal vNumber = new BigDecimal(v);
                return valueNumber.compareTo(vNumber);
            } catch (NumberFormatException e) {
                return value.compareTo(v);
            }
        }
    }


    private static class ExprMatcher implements Matcher {
        private final ThreadLocal<Resource> expressionResource = new ThreadLocal<>();
        private final Expression expr;

        public ExprMatcher(String value) {
            JexlEngine eng = new JexlEngine();
            HashMap<String, Object> functions = new HashMap<>();
            functions.put(null, new ExprFunctions(expressionResource));
            eng.setFunctions(functions);
            expr = eng.createExpression(value);
        }

        @Override
        public boolean match(STContext ctx, Resource resource, @Nullable Resource resolvingResource) throws InvalidRefException {
            expressionResource.set(resource);
            MapContext exprCtx = new MapContext();
            exprCtx.set("el", resource);
            Boolean result = (Boolean) expr.evaluate(exprCtx);
            expressionResource.remove();
            return result;
        }

        @Override
        public Resource autocreate(STContext ctx) throws InvalidRefException {
            return null;
        }
    }

    public static class ExprFunctions {
        private final ThreadLocal<Resource> resource;

        public ExprFunctions(ThreadLocal<Resource> resource) {
            this.resource = resource;
        }

        public String attr(String value) throws InvalidAttributeException {
            return resource.get().get(value);
        }

        public String var(String value) throws InvalidAttributeException {
            return resource.get().get(value);
        }
    }
}
