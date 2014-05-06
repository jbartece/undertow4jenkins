package undertow4jenkins.loader;

import static io.undertow.servlet.Servlets.filter;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.DispatcherType;

import undertow4jenkins.parser.WebXmlContent.Filter;
import undertow4jenkins.parser.WebXmlContent.FilterMapping;

public class FilterLoader {

    public static List<FilterInfo> createFilters(List<Filter> filtersDataCol,
            ClassLoader classLoader) throws ClassNotFoundException {
        List<FilterInfo> filters = new ArrayList<FilterInfo>();

        for (Filter filterData : filtersDataCol) {
            filters.add(
                    createFilterInfo(filterData.filterName, filterData.filterClass, classLoader));
        }

        return filters;
    }

    private static FilterInfo createFilterInfo(String filterName, String filterClassName,
            ClassLoader classLoader) throws ClassNotFoundException {
        Class<? extends javax.servlet.Filter> clazz = Class.forName(filterClassName, true,
                classLoader).asSubclass(javax.servlet.Filter.class);

        return filter(filterName, clazz);
    }

    public static void addFilterMappings(List<FilterMapping> mappingsDataCol, DeploymentInfo servletBuilder) {
        for (FilterMapping mappingData : mappingsDataCol) {
            servletBuilder.addFilterUrlMapping(mappingData.filterName, mappingData.urlPattern,
                    DispatcherType.REQUEST);
        }
    }

}
