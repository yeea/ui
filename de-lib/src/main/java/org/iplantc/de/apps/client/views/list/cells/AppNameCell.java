package org.iplantc.de.apps.client.views.list.cells;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;

import org.iplantc.de.apps.client.events.selection.AppNameSelectedEvent;
import org.iplantc.de.apps.shared.AppsModule;
import org.iplantc.de.client.models.apps.App;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;

/**
 * This is a custom cell which combines the functionality of the {@link AppFavoriteCell} with a selectable
 * hyper-link of an app name.
 *
 * @author jstroot
 * 
 */
public class AppNameCell extends AbstractCell<App> implements HasCell<App, App> {

    @Override
    public Cell<App> getCell() {
        return this;
    }

    @Override
    public FieldUpdater<App, App> getFieldUpdater() {
        return null;
    }

    @Override
    public App getValue(App object) {
        return object;
    }

    public interface AppNameCellAppearance {
        String ELEMENT_NAME = "appName";

        String appDisabledClass();

        String appHyperlinkNameClass();

        String appUnavailable();

        void render(SafeHtmlBuilder sb, App value, String textClassName, String searchPattern,
                    String textToolTip, boolean separateFavoriteCell, String debugId);

        String run();
    }

    protected final AppFavoriteCell favoriteCell = new AppFavoriteCell();
    private final AppNameCellAppearance appearance;
    private String baseID;
    private HasHandlers hasHandlers;
    protected String pattern;
    private boolean separateFavoriteCell = false;

    public AppNameCell() {
        this(GWT.<AppNameCellAppearance> create(AppNameCellAppearance.class));
    }

    public AppNameCell(final AppNameCellAppearance appearance) {
        super(CLICK);
        this.appearance = appearance;
    }

    @Override
    public void render(Cell.Context context, App value, SafeHtmlBuilder sb) {
        if (value == null) {
            return;
        }

        if (!separateFavoriteCell) {
            favoriteCell.render(context, value, sb);
        }
        String textClassName, textToolTip;
        textToolTip = appearance.run();

        if (value.isDisabled()) {
            textClassName = appearance.appDisabledClass();
        } else {
            textClassName = appearance.appHyperlinkNameClass();
        }

        String debugId = baseID + "." + value.getId() + AppsModule.Ids.APP_NAME_CELL;
        appearance.render(sb, value, textClassName, pattern, textToolTip, separateFavoriteCell, debugId);
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element parent, App value, NativeEvent event,
            ValueUpdater<App> valueUpdater) {
        Element eventTarget = Element.as(event.getEventTarget());
        if ((value == null) || !parent.isOrHasChild(eventTarget)) {
            return;
        }

        if (!separateFavoriteCell) {
            favoriteCell.onBrowserEvent(context, parent, value, event, valueUpdater);
        }

        Element child = findAppNameElement(parent);
        if (child != null && child.isOrHasChild(eventTarget)) {

            switch (Event.as(event).getTypeInt()) {
                case Event.ONCLICK:
                    if(hasHandlers != null){
                        hasHandlers.fireEvent(new AppNameSelectedEvent(value));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void setBaseDebugId(String baseID) {
        this.baseID = baseID;
        favoriteCell.setBaseDebugId(baseID);
    }

    public void setHasHandlers(HasHandlers hasHandlers) {
        this.hasHandlers = hasHandlers;
        favoriteCell.setHasHandlers(hasHandlers);
    }

    public void setSearchRegexPattern(String pattern) {
        this.pattern = pattern;
    }

    private Element findAppNameElement(Element parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            Node childNode = parent.getChild(i);

            if (Element.is(childNode)) {
                Element child = Element.as(childNode);
                if (child.getAttribute("name").equalsIgnoreCase(AppNameCellAppearance.ELEMENT_NAME)) { //$NON-NLS-1$
                    return child;
                }
            }
        }
        return null;
    }

    public void setSeparateFavoriteCell(boolean separateFavoriteCell) {
        this.separateFavoriteCell = separateFavoriteCell;
    }
}
