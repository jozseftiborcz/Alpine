/*
 * This file is part of Alpine.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (c) Steve Springett. All Rights Reserved.
 */
package alpine.filters;

import org.apache.commons.lang3.StringUtils;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 *     Implements W3C Content Security Policy (Level 1 and 2).
 * </p>
 *
 * <p>
 *     This filter is configured via the applications web.xml.
 * </p>
 *
 * An example implementation in web.xml:
 *
 * <pre>
 * &lt;filter&gt;
 *     &lt;filter-name&gt;CspFilter&lt;/filter-name&gt;
 *     &lt;filter-class&gt;alpine.filters.ContentSecurityPolicyFilter&lt;/filter-class&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;default-src&lt;/param-name&gt;
 *         &lt;param-value&gt;'self'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;script-src&lt;/param-name&gt;
 *         &lt;param-value&gt;'self' 'unsafe-inline'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;style-src&lt;/param-name&gt;
 *         &lt;param-value&gt;'self'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;img-src&lt;/param-name&gt;
 *         &lt;param-value&gt;'self'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;connect-src&lt;/param-name&gt;
 *         &lt;param-value&gt;'self'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;font-src&lt;/param-name&gt;
 *         &lt;param-value&gt;'self'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;object-src&lt;/param-name&gt;
 *         &lt;param-value&gt;'self'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;media-src&lt;/param-name&gt;
 *         &lt;param-value&gt;'self'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;frame-src&lt;/param-name&gt;
 *         &lt;param-value&gt;'self'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;sandbox&lt;/param-name&gt;
 *         &lt;param-value&gt;allow-forms&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;report-uri&lt;/param-name&gt;
 *         &lt;param-value&gt;/some-report-uri&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;child-src&lt;/param-name&gt;
 *         &lt;param-value&gt;'self'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;form-action-src&lt;/param-name&gt;
 *         &lt;param-value&gt;'self'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;frame-ancestors&lt;/param-name&gt;
 *         &lt;param-value&gt;'none'&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;plugin-types&lt;/param-name&gt;
 *         &lt;param-value&gt;application/pdf&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * &lt;/filter&gt;
 * &lt;filter-mapping&gt;
 *     &lt;filter-name&gt;CspFilter&lt;/filter-name&gt;
 *     &lt;url-pattern&gt;/&#42;&lt;/url-pattern&gt;
 * &lt;/filter-mapping>
 * </pre>
 *
 * <p>
 * The following parameters default to 'self' if not defined:
 * default-src, script-src, style-src, img-src, font-src, object-src, media-src, child-src and form-action.
 *</p>
 * <p>
 * The sandbox param defaults to null indicating that the default sandbox will be applied. The report-uri and
 * plugin-types also default to null. frame-ancestors defaults to 'none' if not specified.
 * </p>
 */
public final class ContentSecurityPolicyFilter implements Filter {

    private static final String SELF = "'self'";
    private static final String NONE = "'none'";

    private String policy = null;
    private String defaultSrc = SELF;
    private String scriptSrc = SELF;
    private String styleSrc = SELF;
    private String imgSrc = SELF;
    private String connectSrc = SELF;
    private String fontSrc = SELF;
    private String objectSrc = SELF;
    private String mediaSrc = SELF;
    private String frameSrc = SELF;
    private String sandbox = null;
    private String reportUri = null;
    private String childSrc = SELF;
    private String formAction = SELF;
    private String frameAncestors = NONE;
    private String pluginTypes = null;


    public void init(final FilterConfig filterConfig) {
        defaultSrc = getValue(filterConfig, "default-src", defaultSrc);
        scriptSrc = getValue(filterConfig, "script-src", scriptSrc);
        styleSrc = getValue(filterConfig, "style-src", styleSrc);
        imgSrc = getValue(filterConfig, "img-src", imgSrc);
        connectSrc = getValue(filterConfig, "connect-src", connectSrc);
        fontSrc = getValue(filterConfig, "font-src", fontSrc);
        objectSrc = getValue(filterConfig, "object-src", objectSrc);
        mediaSrc = getValue(filterConfig, "media-src", mediaSrc);
        frameSrc = getValue(filterConfig, "frame-src", frameSrc);
        sandbox = getValue(filterConfig, "sandbox", sandbox);
        reportUri = getValue(filterConfig, "report-uri", reportUri);
        childSrc = getValue(filterConfig, "child-src", childSrc);
        formAction = getValue(filterConfig, "form-action", formAction);
        frameAncestors = getValue(filterConfig, "frame-ancestors", frameAncestors);
        pluginTypes = getValue(filterConfig, "plugin-types", pluginTypes);

        policy = formatHeader();
    }

    private String getValue(FilterConfig filterConfig, String initParam, String variable) {
        String value = filterConfig.getInitParameter(initParam);
        if (StringUtils.isNotBlank(value)) {
            return value;
        } else {
            return variable;
        }
    }

    public String formatHeader() {
        final StringBuilder sb = new StringBuilder();
        getStringFromValue(sb, "default-src", defaultSrc);
        getStringFromValue(sb, "script-src", scriptSrc);
        getStringFromValue(sb, "style-src", styleSrc);
        getStringFromValue(sb, "img-src", imgSrc);
        getStringFromValue(sb, "connect-src", connectSrc);
        getStringFromValue(sb, "font-src", fontSrc);
        getStringFromValue(sb, "object-src", objectSrc);
        getStringFromValue(sb, "media-src", mediaSrc);
        getStringFromValue(sb, "frame-src", frameSrc);
        getStringFromValue(sb, "sandbox", sandbox);
        getStringFromValue(sb, "report-uri", reportUri);
        getStringFromValue(sb, "child-src", childSrc);
        getStringFromValue(sb, "form-action", formAction);
        getStringFromValue(sb, "frame-ancestors", frameAncestors);
        getStringFromValue(sb, "plugin-types", pluginTypes);
        return sb.toString().replaceAll("(\\[|\\])", "").trim();
    }

    private void getStringFromValue(final StringBuilder builder, final String directive, final String value) {
        if (value != null) {
            builder.append(directive).append(" ").append(value).append(";");
        }
    }

    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletResponse response = (HttpServletResponse)res;
        chain.doFilter(req, response);
        response.addHeader("Content-Security-Policy", policy);
    }

    public void destroy() {
    }

}