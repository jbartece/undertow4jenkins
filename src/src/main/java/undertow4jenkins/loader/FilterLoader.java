package undertow4jenkins.loader;

import static io.undertow.servlet.Servlets.filter;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterLoader {

    // <filter>
    // <filter-name>encoding-filter</filter-name>
    // <filter-class>hudson.util.CharacterEncodingFilter</filter-class>
    // </filter>
    // <filter>
    // <filter-name>compression-filter</filter-name>
    // <filter-class>org.kohsuke.stapler.compression.CompressionFilter</filter-class>
    // </filter>
    // <filter>
    // <filter-name>authentication-filter</filter-name>
    // <filter-class>hudson.security.HudsonFilter</filter-class>
    // </filter>
    // <filter>
    // <filter-name>csrf-filter</filter-name>
    // <filter-class>hudson.security.csrf.CrumbFilter</filter-class>
    // </filter>
    // <filter>
    // <filter-name>plugins-filter</filter-name>
    // <filter-class>hudson.util.PluginServletFilter</filter-class>
    // </filter>
    private static final String[] filterNames = { "encoding-filter", "compression-filter",
            "authentication-filter", "csrf-filter", "plugins-filter" };

    private static final String[] filterClasses = { "hudson.util.CharacterEncodingFilter",
            "org.kohsuke.stapler.compression.CompressionFilter", "hudson.security.HudsonFilter",
            "hudson.security.csrf.CrumbFilter", "hudson.util.PluginServletFilter" };

    // <filter-mapping>
    // <filter-name>encoding-filter</filter-name>
    // <url-pattern>/*</url-pattern>
    // </filter-mapping>
    // <filter-mapping>
    // <filter-name>compression-filter</filter-name>
    // <url-pattern>/*</url-pattern>
    // </filter-mapping>
    // <filter-mapping>
    // <filter-name>authentication-filter</filter-name>
    // <url-pattern>/*</url-pattern>
    // </filter-mapping>
    // <filter-mapping>
    // <filter-name>csrf-filter</filter-name>
    // <url-pattern>/*</url-pattern>
    // </filter-mapping>
    // <filter-mapping>
    // <filter-name>plugins-filter</filter-name>
    // <url-pattern>/*</url-pattern>
    // </filter-mapping>

    private static final String[] filterMappingNames = { "encoding-filter", "compression-filter",
            "authentication-filter", "csrf-filter", "plugins-filter" };

    private static final String[] filterMappingUrls = { "/*", "/*", "/*", "/*", "/*" };

    private static final Logger log = LoggerFactory
            .getLogger("undertow4jenkins.loader.FilterLoader");

    public static List<FilterInfo> createFilters(ClassLoader classLoader)
            throws ClassNotFoundException {
        if (filterNames.length != filterClasses.length) {
            log.error("Different count of filter names and classes!");
            return null;
        }

        List<FilterInfo> filters = new ArrayList<FilterInfo>();
        for (int i = 0; i < filterNames.length; i++) {
            filters.add(createFilterInfo(filterNames[i], filterClasses[i], classLoader));
        }

        return filters;
    }

    public static void addFilterMappings(DeploymentInfo servletBuilder) {
        if (filterMappingNames.length != filterMappingUrls.length) {
            log.error("Different count of filter names and urls!");
            return;
        }

        for (int i = 0; i < filterMappingNames.length; i++) {
            servletBuilder.addFilterUrlMapping(filterMappingNames[i], filterMappingUrls[i],
                    DispatcherType.REQUEST); // TODO check the default dispatcher type
        }
    }

    private static FilterInfo createFilterInfo(String filterName, String filterClassName,
            ClassLoader classLoader) throws ClassNotFoundException {
        Class<? extends Filter> clazz = Class.forName(filterClassName, true,
                classLoader).asSubclass(Filter.class);

        return filter(filterName, clazz);
    }

}
