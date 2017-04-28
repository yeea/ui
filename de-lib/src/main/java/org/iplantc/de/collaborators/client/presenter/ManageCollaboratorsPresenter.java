/**
 * 
 */
package org.iplantc.de.collaborators.client.presenter;

import org.iplantc.de.client.events.EventBus;
import org.iplantc.de.client.models.UserInfo;
import org.iplantc.de.client.models.collaborators.Collaborator;
import org.iplantc.de.client.models.groups.Group;
import org.iplantc.de.client.services.CollaboratorsServiceFacade;
import org.iplantc.de.client.services.GroupServiceFacade;
import org.iplantc.de.collaborators.client.GroupView;
import org.iplantc.de.collaborators.client.ManageCollaboratorsView;
import org.iplantc.de.collaborators.client.events.AddGroupSelected;
import org.iplantc.de.collaborators.client.events.CollaboratorsLoadedEvent;
import org.iplantc.de.collaborators.client.events.DeleteGroupSelected;
import org.iplantc.de.collaborators.client.events.RemoveCollaboratorSelected;
import org.iplantc.de.collaborators.client.events.UserSearchResultSelected;
import org.iplantc.de.collaborators.client.gin.ManageCollaboratorsViewFactory;
import org.iplantc.de.collaborators.client.util.CollaboratorsUtil;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.commons.client.info.ErrorAnnouncementConfig;
import org.iplantc.de.commons.client.info.IplantAnnouncer;
import org.iplantc.de.commons.client.info.SuccessAnnouncementConfig;
import org.iplantc.de.resources.client.messages.I18N;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sriram
 * 
 */
public class ManageCollaboratorsPresenter implements ManageCollaboratorsView.Presenter,
                                                     RemoveCollaboratorSelected.RemoveCollaboratorSelectedHandler,
                                                     AddGroupSelected.AddGroupSelectedHandler,
                                                     DeleteGroupSelected.DeleteGroupSelectedHandler {

    final class UserSearchResultSelectedEventHandlerImpl implements
                                                                 UserSearchResultSelected.UserSearchResultSelectedEventHandler {
        @Override
           public void
                   onUserSearchResultSelected(UserSearchResultSelected userSearchResultSelected) {
               if (userSearchResultSelected.getTag()
                                           .equalsIgnoreCase(UserSearchResultSelected.USER_SEARCH_EVENT_TAG.MANAGE.toString())) {
                   Collaborator collaborator = userSearchResultSelected.getCollaborator();
                   if (!UserInfo.getInstance()
                                .getUsername()
                                .equals(collaborator.getUserName())) {
                       if (!collaboratorsUtil.isCurrentCollaborator(collaborator, view.getCollaborators())) {
                           addAsCollaborators(Arrays.asList(collaborator));
                       }
                   } else {
                       announcer.schedule(new ErrorAnnouncementConfig(I18N.DISPLAY.collaboratorSelfAdd()));
                   }
               }
           }
    }

    @Inject CollaboratorsUtil collaboratorsUtil;
    @Inject EventBus eventBus;
    @Inject IplantAnnouncer announcer;
    private ManageCollaboratorsViewFactory factory;
    private GroupServiceFacade groupServiceFacade;
    private CollaboratorsServiceFacade collabServiceFacade;
    private GroupView.GroupViewAppearance groupAppearance;
    ManageCollaboratorsView view;
    HandlerRegistration addCollabHandlerRegistration;

    @Inject
    public ManageCollaboratorsPresenter(ManageCollaboratorsViewFactory factory,
                                        GroupServiceFacade groupServiceFacade,
                                        CollaboratorsServiceFacade collabServiceFacade,
                                        GroupView.GroupViewAppearance groupAppearance) {
        this.factory = factory;
        this.groupServiceFacade = groupServiceFacade;
        this.collabServiceFacade = collabServiceFacade;
        this.groupAppearance = groupAppearance;
    }

    void addEventHandlers() {
        addCollabHandlerRegistration = eventBus.addHandler(UserSearchResultSelected.TYPE,
                                                           new UserSearchResultSelectedEventHandlerImpl());
        view.addAddGroupSelectedHandler(this);
        view.addDeleteGroupSelectedHandler(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.iplantc.de.commons.client.presenter.Presenter#go(com.google.gwt.user.client.ui.HasOneWidget )
     */
    @Override
    public void go(HasOneWidget container, ManageCollaboratorsView.MODE mode) {
        this.view = factory.create(mode);
        view.addRemoveCollaboratorSelectedHandler(this);
        loadCurrentCollaborators();
//        updateListView();
        addEventHandlers();
        container.setWidget(view.asWidget());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.iplantc.de.client.collaborators.views.ManageCollaboratorsView.Presenter#addAsCollaborators
     * (java.util.List)
     */
    @Override
    public void addAsCollaborators(final List<Collaborator> models) {
        collabServiceFacade.addCollaborators(models, new AsyncCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                // remove added models from search results
                view.addCollaborators(models);
                String names = getCollaboratorNames(models);

                announcer.schedule(new SuccessAnnouncementConfig(
                                       I18N.DISPLAY.collaboratorAddConfirm(names)));
            }

            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(I18N.ERROR.addCollabErrorMsg(), caught);
            }
        });

    }

    String getCollaboratorNames(List<Collaborator> collaborators) {
        Stream<Collaborator> stream = collaborators.stream();

        Stream<String> stringStream = stream.map(Collaborator::getUserName);
        List<String> names = stringStream.collect(Collectors.toList());
        return Joiner.on(",").join(names);
    }

    void updateListView() {
        String searchTerm = "*";
        updateListView(searchTerm);
    }

    @Override
    public void updateListView(String searchTerm) {
        groupServiceFacade.getGroups(searchTerm, new AsyncCallback<List<Group>>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
            }

            @Override
            public void onSuccess(List<Group> result) {
                view.addCollabLists(result);
            }
        });
    }

    @Override
    public void onRemoveCollaboratorSelected(RemoveCollaboratorSelected event) {
        List<Collaborator> models = event.getCollaborators();
        collabServiceFacade.removeCollaborators(models, new AsyncCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                view.removeCollaborators(models);
                String names = getCollaboratorNames(models);
                announcer.schedule(new SuccessAnnouncementConfig(I18N.DISPLAY.collaboratorRemoveConfirm(names)));

            }

            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
            }
        });

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.iplantc.de.client.collaborators.views.ManageCollaboratorsView.Presenter#loadCurrentCollaborators
     * ()
     */
    @Override
    public void loadCurrentCollaborators() {
        view.mask(null);
        collabServiceFacade.getCollaborators(new AsyncCallback<List<Collaborator>>() {

            @Override
            public void onFailure(Throwable caught) {
                view.unmask();
            }

            @Override
            public void onSuccess(List<Collaborator> result) {
                view.unmask();
                view.loadData(result);
                eventBus.fireEvent(new CollaboratorsLoadedEvent());
            }

        });

    }

    @Override
    public void setCurrentMode(ManageCollaboratorsView.MODE m) {
        view.setMode(m);
    }

    @Override
    public ManageCollaboratorsView.MODE getCurrentMode() {
        return view.getMode();
    }

    @Override
    public List<Collaborator> getSelectedCollaborators() {
        return view.getSelectedCollaborators();
    }

    @Override
    public void cleanup() {
        if (addCollabHandlerRegistration != null) {
            addCollabHandlerRegistration.removeHandler();
        }
    }

    @Override
    public void onAddGroupSelected(AddGroupSelected event) {
        Group group = event.getGroup();
        if (group == null) {
            return;
        }
        groupServiceFacade.addGroup(group, new AsyncCallback<Group>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
            }

            @Override
            public void onSuccess(Group result) {
                view.addCollabLists(getGroupList(result));
            }
        });
    }

    List<Group> getGroupList(Group result) {
        return Lists.newArrayList(result);
    }

    @Override
    public void onDeleteGroupSelected(DeleteGroupSelected event) {
        Group group = event.getGroup();
        groupServiceFacade.deleteGroup(group.getName(), new AsyncCallback<Group>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
            }

            @Override
            public void onSuccess(Group result) {
                view.removeCollabList(result);
                announcer.schedule(new SuccessAnnouncementConfig(groupAppearance.groupDeleteSuccess(result)));
            }
        });
    }
}
