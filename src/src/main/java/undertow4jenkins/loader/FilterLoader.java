package undertow4jenkins.loader;

import static io.undertow.servlet.Servlets.filter;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.DispatcherType;

import undertow4jenkins.parser.WebXmlContent.Filter;
import undertow4jenkins.parser.WebXmlContent.FilterMapping;

/**
 * Utility class to create filters from web.xml data
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class FilterLoader {

    /**
     * Creates filters entity from web.xml data
     * @param filtersDataCol Data from web.xml
     * @param classLoader Application classloader 
     * @return Created entity
     * @throws ClassNotFoundException Thrown if some class 
     *      could not be loaded by current class loader
     */
    public static List<FilterInfo> createFilters(List<Filter> filtersDataCol,
            ClassLoader classLoader) throws ClassNotFoundException {
        List<FilterInfo> filters = new ArrayList<FilterInfo>();

        for (Filter filterData : filtersDataCol) {
            filters.add(
                    createFilterInfo(filterData.filterName, filterData.filterClass, classLoader));
        }

        return filters;
    }

    /**
     * Creates undertow entity with filter information
     * @param filterName Name of filter
     * @param filterClassName Name of filter class
     * @param classLoader Application classloader 
     * @return Created entity
     * @throws ClassNotFoundException Thrown if some class 
     *      could not be loaded by current class loader
     */
    private static FilterInfo createFilterInfo(String filterName, String filterClassName,
            ClassLoader classLoader) throws ClassNotFoundException {
        Class<? extends javax.servlet.Filter> clazz = Class.forName(filterClassName, true,
                classLoader).asSubclass(javax.servlet.Filter.class);

        return filter(filterName, clazz);
    }

    /**
     * Add filter mappings to servlet container configuration in DeploymentInfo
     * 
     * @param filtersDataCol Data from web.xml
     * @param servletBuilder Builder instance of servlet container
     */
    public static void addFilterMappings(List<FilterMapping> mappingsDataCol, DeploymentInfo servletBuilder) {
        for (FilterMapping mappingData : mappingsDataCol) {
            servletBuilder.addFilterUrlMapping(mappingData.filterName, mappingData.urlPattern,
                    DispatcherType.REQUEST);
        }
    }

}
