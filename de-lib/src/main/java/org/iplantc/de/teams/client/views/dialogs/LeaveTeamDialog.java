package org.iplantc.de.teams.client.views.dialogs;

import org.iplantc.de.client.models.groups.Group;
import org.iplantc.de.teams.client.TeamsView;
import org.iplantc.de.teams.shared.Teams;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;

/**
 * A dialog that warns users trying to leave a team of the consequences of the operation
 * before executing the operation
 */
public class LeaveTeamDialog extends ConfirmMessageBox {

    private TeamsView.TeamsViewAppearance appearance;

    @Inject
    public LeaveTeamDialog(TeamsView.TeamsViewAppearance appearance) {
        super(SafeHtmlUtils.EMPTY_SAFE_HTML, appearance.leaveTeamWarning());
        this.appearance = appearance;

        setWidth(appearance.leaveTeamWidth());
        setPredefinedButtons(PredefinedButton.YES, PredefinedButton.CANCEL);
        getButton(PredefinedButton.YES).setText(appearance.leaveTeam());

        ensureDebugId(Teams.Ids.LEAVE_TEAM_DLG);
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);

        getButton(PredefinedButton.YES).asWidget().ensureDebugId(baseID + Teams.Ids.LEAVE_TEAM_BTN);
        getButton(PredefinedButton.CANCEL).asWidget().ensureDebugId(baseID + Teams.Ids.CANCEL_BTN);
    }

    public void show(Group group) {
        setHeading(appearance.leaveTeamHeader(group));

        super.show();
    }
}
