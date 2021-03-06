package org.iplantc.de.teams.client.views.dialogs;

import org.iplantc.de.client.models.groups.Group;
import org.iplantc.de.commons.client.views.dialogs.IPlantDialog;
import org.iplantc.de.teams.client.EditTeamView;
import org.iplantc.de.teams.client.TeamsView;
import org.iplantc.de.teams.client.events.DeleteTeamCompleted;
import org.iplantc.de.teams.client.events.JoinTeamCompleted;
import org.iplantc.de.teams.client.events.LeaveTeamCompleted;
import org.iplantc.de.teams.client.events.PrivilegeAndMembershipLoaded;
import org.iplantc.de.teams.client.events.TeamSaved;
import org.iplantc.de.teams.shared.Teams;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;

import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;

/**
 * The main dialog that presents the form to users for creating/editing a team
 */
public class EditTeamDialog extends IPlantDialog implements TeamSaved.HasTeamSavedHandlers,
                                                            LeaveTeamCompleted.HasLeaveTeamCompletedHandlers,
                                                            PrivilegeAndMembershipLoaded.PrivilegeAndMembershipLoadedHandler,
                                                            DeleteTeamCompleted.HasDeleteTeamCompletedHandlers,
                                                            JoinTeamCompleted.HasJoinTeamCompletedHandlers {
    private EditTeamView.Presenter presenter;
    private TeamsView.TeamsViewAppearance appearance;
    private TextButton leaveBtn;
    private TextButton deleteBtn;
    private TextButton requestToJoinBtn;
    private Group team;

    @Inject
    public EditTeamDialog(EditTeamView.Presenter presenter,
                          TeamsView.TeamsViewAppearance appearance) {
        super(true);
        this.presenter = presenter;
        this.appearance = appearance;

        setResizable(true);
        setPixelSize(appearance.editTeamWidth(), appearance.editTeamHeight());
        setMinWidth(appearance.editTeamWidth());
        setOnEsc(false);
        setHideOnButtonClick(false);
        setButtons();
        setHandlers();
        addHelp(new HTML(appearance.editTeamHelpText()));
    }

    public void show(Group team) {
        this.team = team;
        presenter.go(this, team);

        setHeading(appearance.editTeamHeading(team, false));
        super.show();

        ensureDebugId(Teams.Ids.EDIT_TEAM_DIALOG);
    }

    @Override
    public void show() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This method is not supported for this class. ");
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);

        presenter.setViewDebugId(Teams.Ids.EDIT_TEAM_DIALOG);
        leaveBtn.asWidget().ensureDebugId(baseID + Teams.Ids.LEAVE_TEAM_BTN);
        deleteBtn.asWidget().ensureDebugId(baseID + Teams.Ids.DELETE_TEAM_BTN);
        requestToJoinBtn.asWidget().ensureDebugId(baseID + Teams.Ids.JOIN_TEAM_BTN);
        getButton(PredefinedButton.OK).ensureDebugId(baseID + Teams.Ids.OK_BTN);
        getButton(PredefinedButton.CANCEL).ensureDebugId(baseID + Teams.Ids.CANCEL_BTN);
    }

    void setButtons() {
        leaveBtn = new TextButton(appearance.leaveTeam());
        deleteBtn = new TextButton(appearance.deleteTeam());
        requestToJoinBtn = new TextButton(appearance.requestToJoinTeam());

        leaveBtn.setVisible(false);
        deleteBtn.setVisible(false);
        requestToJoinBtn.setVisible(false);

        buttonBar.setPack(BoxLayoutContainer.BoxLayoutPack.START);
        addButton(leaveBtn);
        addButton(deleteBtn);
        addButton(requestToJoinBtn);
        addButton(new FillToolItem());
        addButton(getButton(PredefinedButton.OK));
        addButton(getButton(PredefinedButton.CANCEL));
        buttonBar.forceLayout();
    }

    void setHandlers() {
        presenter.addPrivilegeAndMembershipLoadedHandler(this);
        leaveBtn.addSelectHandler(event -> presenter.onLeaveButtonSelected(this));
        deleteBtn.addSelectHandler(event -> presenter.onDeleteButtonSelected(this));
        requestToJoinBtn.addSelectHandler(event -> presenter.onJoinButtonSelected(this));

        addOkButtonSelectHandler(selectEvent -> {
            if (presenter.isViewValid()) {
                presenter.saveTeamSelected(this);
            } else {
                AlertMessageBox alertMsgBox =
                        new AlertMessageBox(appearance.completeRequiredFieldsHeading(), appearance.completeRequiredFieldsError());
                alertMsgBox.show();
            }
        });
        addCancelButtonSelectHandler(selectEvent -> hide());
    }

    @Override
    public void onPrivilegeAndMembershipLoaded(PrivilegeAndMembershipLoaded event) {
        boolean isAdmin = event.isAdmin();
        boolean isMember = event.isMember();
        boolean hasVisibleMembers = event.hasVisibleMembers();
        setHeading(appearance.editTeamHeading(team, isAdmin));
        deleteBtn.setVisible(isAdmin);
        leaveBtn.setVisible(isMember);
        requestToJoinBtn.setVisible(!isAdmin && !isMember);
        setHeight(appearance.editTeamAdjustedHeight(isAdmin, hasVisibleMembers));
        if (!isAdmin) {
            buttonBar.remove(getButton(PredefinedButton.OK));
        }

        buttonBar.forceLayout();
    }

    @Override
    public HandlerRegistration addTeamSavedHandler(TeamSaved.TeamSavedHandler handler) {
        return presenter.addTeamSavedHandler(handler);
    }

    @Override
    public HandlerRegistration addLeaveTeamCompletedHandler(LeaveTeamCompleted.LeaveTeamCompletedHandler handler) {
        return presenter.addLeaveTeamCompletedHandler(handler);
    }

    @Override
    public HandlerRegistration addDeleteTeamCompletedHandler(DeleteTeamCompleted.DeleteTeamCompletedHandler handler) {
        return presenter.addDeleteTeamCompletedHandler(handler);
    }

    @Override
    public HandlerRegistration addJoinTeamCompletedHandler(JoinTeamCompleted.JoinTeamCompletedHandler handler) {
        return presenter.addJoinTeamCompletedHandler(handler);
    }
}
