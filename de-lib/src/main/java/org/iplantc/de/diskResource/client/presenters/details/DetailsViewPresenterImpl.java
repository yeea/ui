package org.iplantc.de.diskResource.client.presenters.details;

import org.iplantc.de.client.models.diskResources.DiskResource;
import org.iplantc.de.client.models.tags.Tag;
import org.iplantc.de.client.services.FileSystemMetadataServiceFacade;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.commons.client.info.IplantAnnouncer;
import org.iplantc.de.commons.client.info.SuccessAnnouncementConfig;
import org.iplantc.de.diskResource.client.DetailsView;
import org.iplantc.de.diskResource.client.events.selection.RemoveResourceTagSelected;
import org.iplantc.de.diskResource.client.events.selection.UpdateResourceTagSelected;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import java.util.List;

/**
 * @author jstroot
 */
public class DetailsViewPresenterImpl implements DetailsView.Presenter,
                                                 RemoveResourceTagSelected.RemoveResourceTagSelectedHandler,
                                                 UpdateResourceTagSelected.UpdateResourceTagSelectedHandler {

    @Inject IplantAnnouncer announcer;
    @Inject FileSystemMetadataServiceFacade metadataService;
    @Inject DetailsView.Presenter.Appearance appearance;

    private final DetailsView view;

    @Inject
    DetailsViewPresenterImpl(final DetailsView view) {
        this.view = view;

        view.addRemoveResourceTagSelectedHandler(this);
        view.addUpdateResourceTagSelectedHandler(this);
    }

    @Override
    public void onUpdateResourceTagSelected(UpdateResourceTagSelected event) {
        DiskResource resource = event.getDiskResource();
        Tag tag = event.getTag();
        List<Tag> tagList = wrapInList(tag);
        metadataService.attachTags(tagList, resource, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(appearance.tagAttachError(), caught);
            }

            @Override
            public void onSuccess(Void result) {
                announcer.schedule(new SuccessAnnouncementConfig(appearance.tagAttached(resource.getName(), tag.getValue())));
            }
        });
    }

    @Override
    public void onRemoveResourceTagSelected(RemoveResourceTagSelected event) {
        Tag tag = event.getTag();
        DiskResource resource = event.getResource();
        List<Tag> tagList = wrapInList(tag);
        metadataService.detachTags(tagList, resource, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(appearance.tagDetachError(), caught);
            }

            @Override
            public void onSuccess(Void result) {
                announcer.schedule(new SuccessAnnouncementConfig(appearance.tagDetached(tag.getValue(), resource.getName())));
            }
        });
    }

    @Override
    public DetailsView getView() {
        return view;
    }

    List<Tag> wrapInList(Tag tag) {
        return Lists.newArrayList(tag);
    }
}
