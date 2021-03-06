package org.iplantc.de.client.util;

import org.iplantc.de.client.models.search.DiskResourceQueryTemplate;
import org.iplantc.de.client.models.search.FileSizeRange.FileSizeUnit;
import org.iplantc.de.client.models.search.SearchAutoBeanFactory;
import org.iplantc.de.commons.client.info.ErrorAnnouncementConfig;
import org.iplantc.de.commons.client.info.IplantAnnouncer;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.shared.Splittable;
import com.google.web.bindery.autobean.shared.impl.StringQuoter;

import java.util.List;
import java.util.logging.Logger;

/**
 * A utility class for functions used in Data window searches
 */
public class SearchModelUtils {

    private final List<String> fileSizeUnits = Lists.newArrayList("KB", "MB", "GB", "TB");
    private final SearchAutoBeanFactory factory = GWT.create(SearchAutoBeanFactory.class);
    private static SearchModelUtils INSTANCE;
    Logger LOG = Logger.getLogger(SearchModelUtils.class.getName());

    SearchModelUtils(){

    }

    public static SearchModelUtils getInstance(){
        if(INSTANCE == null){
            INSTANCE = new SearchModelUtils();
        }
        return INSTANCE;
    }

    public DiskResourceQueryTemplate createDefaultFilter() {
        Splittable defFilter = StringQuoter.createSplittable();
        // Need to create full permissions by default in order to function as a "smart folder"
        Splittable permissions = StringQuoter.createSplittable();
        StringQuoter.create(true).assign(permissions, "own");
        StringQuoter.create(true).assign(permissions, "read");
        StringQuoter.create(true).assign(permissions, "write");
        permissions.assign(defFilter, "permissions");
        StringQuoter.create("/savedFilters/").assign(defFilter, "path");

        DiskResourceQueryTemplate dataSearchFilter = AutoBeanCodex.decode(factory, DiskResourceQueryTemplate.class, defFilter).as();
        dataSearchFilter.setCreatedWithin(factory.dateInterval().as());
        dataSearchFilter.setModifiedWithin(factory.dateInterval().as());
        dataSearchFilter.setFileSizeRange(factory.fileSizeRange().as());
        dataSearchFilter.getFileSizeRange().setMaxUnit(createDefaultFileSizeUnit());
        dataSearchFilter.getFileSizeRange().setMinUnit(createDefaultFileSizeUnit());

        return dataSearchFilter;
    }

    public Double convertFileSizeToBytes(Double size, FileSizeUnit unit) {
        if (size != null && unit != null && unit.getUnit() > 0) {
            return size * Math.pow(1024, unit.getUnit());
        }

        return size;
    }

    public List<FileSizeUnit> createFileSizeUnits() {
        List<FileSizeUnit> ret = Lists.newArrayList();

        int unit = 0;
        for (String fsLabel : fileSizeUnits) {
            unit++;
            ret.add(createFileSizeUnit(unit, fsLabel));
        }

        return ret;
    }

    private FileSizeUnit createDefaultFileSizeUnit() {
        return createFileSizeUnit(1, fileSizeUnits.get(0));
    }

    private FileSizeUnit createFileSizeUnit(int unit, String label) {
        FileSizeUnit fsUnit = factory.fileSizeUnit().as();
        fsUnit.setUnit(unit);
        fsUnit.setLabel(label);
        return fsUnit;
    }

    public DiskResourceQueryTemplate copyDiskResourceQueryTemplate(DiskResourceQueryTemplate src) {
        if (src == null) {
            return null;
        }

        AutoBean<DiskResourceQueryTemplate> queryBean = AutoBeanUtils.getAutoBean(src);
        if (queryBean == null) {
            return null;
        }

        DiskResourceQueryTemplate temp = AutoBeanCodex.decode(factory,
                                                              DiskResourceQueryTemplate.class,
                                                              AutoBeanCodex.encode(queryBean).getPayload()).as();

        LOG.fine(temp.getTagQuery().toString());
        return temp;
    }

    public boolean isEmptyQuery(DiskResourceQueryTemplate template) {
        boolean isEmpty = Strings.isNullOrEmpty(template.getOwnedBy())
                          && Strings.isNullOrEmpty(template.getFileQuery())
                          && Strings.isNullOrEmpty(template.getMetadataAttributeQuery())
                          && Strings.isNullOrEmpty(template.getMetadataValueQuery())
                          && Strings.isNullOrEmpty(template.getNegatedFileQuery())
                          && Strings.isNullOrEmpty(template.getSharedWith())
                          && (template.getDateCreated() == null)
                          && (template.getLastModified() == null)
                          && ((template.getCreatedWithin() == null) || (template.getCreatedWithin().getFrom() == null && template.getCreatedWithin()
                                                                                                                                 .getTo() == null))
                          && ((template.getModifiedWithin() == null) || (template.getModifiedWithin().getFrom() == null && template.getModifiedWithin()
                                                                                                                                   .getTo() == null))
                          && ((template.getFileSizeRange() == null) || (template.getFileSizeRange().getMax() == null && template.getFileSizeRange()
                                                                                                                                .getMin() == null))
                          && (template.getTagQuery() == null || template.getTagQuery().size() == 0);
        if (isEmpty) {
            LOG.fine("tags size==>" + template.getTagQuery());
            IplantAnnouncer.getInstance().schedule(new ErrorAnnouncementConfig("You must select at least one filter."));
        }
        return isEmpty;
    }
}
