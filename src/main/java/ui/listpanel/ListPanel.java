package ui.listpanel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import filter.expression.Qualifier;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import ui.TestController;
import ui.UI;
import ui.components.KeyboardShortcuts;
import ui.components.IssueListView;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import util.KeyPress;
import util.events.IssueSelectedEvent;
import util.events.ShowLabelPickerEvent;
import util.events.testevents.UIComponentFocusEvent;

import static ui.components.KeyboardShortcuts.JUMP_TO_FIRST_ISSUE;
import static ui.components.KeyboardShortcuts.JUMP_TO_FILTER_BOX;
import static ui.components.KeyboardShortcuts.MAXIMIZE_WINDOW;
import static ui.components.KeyboardShortcuts.MINIMIZE_WINDOW;
import static ui.components.KeyboardShortcuts.DEFAULT_SIZE_WINDOW;
import static ui.components.KeyboardShortcuts.SWITCH_DEFAULT_REPO;
import static ui.components.KeyboardShortcuts.SWITCH_BOARD;
import static ui.components.KeyboardShortcuts.UNDO_LABEL_CHANGES;
import static ui.components.KeyboardShortcuts.GOTO_MODIFIER;
import static ui.components.KeyboardShortcuts.SHOW_DOCS;
import static ui.components.KeyboardShortcuts.SHOW_CONTRIBUTORS;
import static ui.components.KeyboardShortcuts.SHOW_HELP;
import static ui.components.KeyboardShortcuts.SHOW_ISSUES;
import static ui.components.KeyboardShortcuts.SHOW_LABELS;
import static ui.components.KeyboardShortcuts.SHOW_MILESTONES;
import static ui.components.KeyboardShortcuts.SHOW_PULL_REQUESTS;
import static ui.components.KeyboardShortcuts.SHOW_KEYBOARD_SHORTCUTS;
import static ui.components.KeyboardShortcuts.JUMP_TO_NTH_ISSUE_KEYS;
import static ui.components.KeyboardShortcuts.NEW_COMMENT;
import static ui.components.KeyboardShortcuts.MANAGE_ASSIGNEES;

public class ListPanel extends FilterPanel {

    private final IModel model;
    private final UI ui;
    private int issueCount;

    private IssueListView listView;
    private HashMap<Integer, Integer> issueCommentCounts = new HashMap<>();
    private HashMap<Integer, Integer> issueNonSelfCommentCounts = new HashMap<>();

    // Context Menu
    private final ContextMenu contextMenu = new ContextMenu();

    private final MenuItem markAsReadUnreadMenuItem = new MenuItem();
    private static final String markAsReadMenuItemText = "Mark as read (E)";
    private static final String markAsUnreadMenuItemText = "Mark as unread (U)";

    private static final MenuItem changeLabelsMenuItem = new MenuItem();
    private static final String changeLabelsMenuItemText = "Change labels (L)";

    public ListPanel(UI ui, IModel model, PanelControl parentPanelControl, int panelIndex) {
        super(ui, model, parentPanelControl, panelIndex);
        this.model = model;
        this.ui = ui;

        listView = new IssueListView();
        setupListView();
        getChildren().add(listView);
    }

    /**
     * Determines if an issue has had new comments added (or removed) based on
     * its last-known comment count in {@link #issueCommentCounts}.
     * @param issue
     * @return true if the issue has changed, false otherwise
     */

    private boolean issueHasNewComments(TurboIssue issue, boolean hasMetadata) {
        if (hasMetadata && Qualifier.hasUpdatedQualifier(currentFilterExpression)) {
            return issueNonSelfCommentCounts.containsKey(issue.getId()) &&
                    Math.abs(
                            issueNonSelfCommentCounts.get(issue.getId()) - issue.getMetadata().getNonSelfCommentCount()
                    ) > 0;
        } else {
            return issueCommentCounts.containsKey(issue.getId()) &&
                    Math.abs(issueCommentCounts.get(issue.getId()) - issue.getCommentCount()) > 0;
        }
    }

    /**
     * Updates {@link #issueCommentCounts} with the latest counts.
     * Returns a list of issues which have new comments.
     * @return
     */
    private HashSet<Integer> updateIssueCommentCounts(boolean hasMetadata) {
        HashSet<Integer> result = new HashSet<>();
        for (TurboIssue issue : getIssueList()) {
            if (issueCommentCounts.containsKey(issue.getId())) {
                // We know about this issue; check if it's been updated
                if (issueHasNewComments(issue, hasMetadata)) {
                    result.add(issue.getId());
                }
            } else {
                // We don't know about this issue, just put the current comment count.
                issueNonSelfCommentCounts.put(issue.getId(), issue.getMetadata().getNonSelfCommentCount());
                issueCommentCounts.put(issue.getId(), issue.getCommentCount());
            }
        }
        return result;
    }

    /**
     * Refreshes the list of issue cards shown to the user.
     *
     * @param hasMetadata Indicates the comment count hashmap to be used.
     */
    @Override
    public void refreshItems(boolean hasMetadata) {
        final HashSet<Integer> issuesWithNewComments = updateIssueCommentCounts(hasMetadata);

        // Set the cell factory every time - this forces the list view to update
        listView.setCellFactory(list ->
                new ListPanelCell(model, ListPanel.this, panelIndex, issuesWithNewComments));
        listView.saveSelection();

        // Supposedly this also causes the list view to update - not sure
        // if it actually does on platforms other than Linux...
        listView.setItems(null);
        listView.setItems(getIssueList());
        issueCount = getIssueList().size();

        listView.restoreSelection();
        this.setId(model.getDefaultRepo() + "_col" + panelIndex);
    }

    private void setupListView() {
        setVgrow(listView, Priority.ALWAYS);
        setupKeyboardShortcuts();
        setupContextMenu();

        listView.setOnItemSelected(i -> {
            updateContextMenu(contextMenu);

            TurboIssue issue = listView.getItems().get(i);
            ui.triggerEvent(
                    new IssueSelectedEvent(issue.getRepoId(), issue.getId(), panelIndex, issue.isPullRequest())
            );

            // Save the stored comment count as its own comment count.
            // The refreshItems(false) call that follows will remove the highlighted effect of the comment bubble.
            // (if it was there before)
            issueCommentCounts.put(issue.getId(), issue.getCommentCount());
            issueNonSelfCommentCounts.put(issue.getId(), issue.getMetadata().getNonSelfCommentCount());
            // We assume we already have metadata, so we pass true to avoid refreshItems from trying to get
            // metadata after clicking.
            refreshItems(true);
        });
    }

    private void setupKeyboardShortcuts() {
        filterTextField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (JUMP_TO_FIRST_ISSUE.match(event)) {
                event.consume();
                listView.selectNthItem(1);
            }
            if (MAXIMIZE_WINDOW.match(event)) {
                ui.maximizeWindow();
            }
            if (MINIMIZE_WINDOW.match(event)) {
                ui.minimizeWindow();
            }
            if (DEFAULT_SIZE_WINDOW.match(event)) {
                ui.setDefaultWidth();
            }
            if (SWITCH_DEFAULT_REPO.match(event)) {
                ui.switchDefaultRepo();
            }
            if (SWITCH_BOARD.match(event)) {
                ui.getMenuControl().switchBoard();
            }
        });

        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (KeyboardShortcuts.markAsRead.match(event)) {
                markAsRead();
            }
            if (KeyboardShortcuts.markAsUnread.match(event)) {
                markAsUnread();
            }
            if (SHOW_DOCS.match(event)) {
                ui.getBrowserComponent().showDocs();
            }
            if (JUMP_TO_FILTER_BOX.match(event)) {
                setFocusToFilterBox();
            }
            if (JUMP_TO_FIRST_ISSUE.match(event)) {
                listView.selectNthItem(1);
            }
            if (SHOW_ISSUES.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().showIssues();
                }
            }
            if (SHOW_PULL_REQUESTS.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().showPullRequests();
                }
            }
            if (SHOW_HELP.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().showDocs();
                }
            }
            if (SHOW_KEYBOARD_SHORTCUTS.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().showKeyboardShortcuts();
                }
            }
            if (SHOW_CONTRIBUTORS.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().showContributors();
                    event.consume();
                }
            }
            if (KeyboardShortcuts.scrollToTop.match(event)) {
                ui.getBrowserComponent().scrollToTop();
            }
            if (KeyboardShortcuts.scrollToBottom.match(event)) {
                if (!MINIMIZE_WINDOW.match(event)) {
                    ui.getBrowserComponent().scrollToBottom();
                }
            }
            if (KeyboardShortcuts.scrollUp.match(event) || KeyboardShortcuts.scrollDown.match(event)) {
                ui.getBrowserComponent().scrollPage(KeyboardShortcuts.scrollDown.match(event));
            }
            if (GOTO_MODIFIER.match(event)) {
                KeyPress.setLastKeyPressedCodeAndTime(event.getCode());
            }
            if (NEW_COMMENT.match(event) && 
                    ui.getBrowserComponent().isCurrentUrlIssue()) {
                ui.getBrowserComponent().switchToConversationTab();
                ui.getBrowserComponent().jumpToComment();
            }
            if (SHOW_LABELS.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().newLabel();
                } else {
                    changeLabels();
                }
            }
            if (MANAGE_ASSIGNEES.match(event) && ui.getBrowserComponent().isCurrentUrlIssue()) {
                ui.getBrowserComponent().switchToConversationTab();
                ui.getBrowserComponent().manageAssignees(event.getCode().toString());
            }
            if (SHOW_MILESTONES.match(event)) {
                if (KeyPress.isValidKeyCombination(GOTO_MODIFIER.getCode(), event.getCode())) {
                    ui.getBrowserComponent().showMilestones();
                } else if (ui.getBrowserComponent().isCurrentUrlIssue()) {
                    ui.getBrowserComponent().switchToConversationTab();
                    ui.getBrowserComponent().manageMilestones(event.getCode().toString());
                }
            }
            if (MAXIMIZE_WINDOW.match(event)) {
                ui.maximizeWindow();
            }
            if (MINIMIZE_WINDOW.match(event)) {
                ui.minimizeWindow();
            }
            if (DEFAULT_SIZE_WINDOW.match(event)) {
                ui.setDefaultWidth();
            }
            if (SWITCH_BOARD.match(event)) {
                ui.getMenuControl().switchBoard();
            }
            if (SWITCH_DEFAULT_REPO.match(event)) {
                ui.switchDefaultRepo();
            }
            if (UNDO_LABEL_CHANGES.match(event)) {
                ui.triggerNotificationAction();
            }
            for (Map.Entry<Integer, KeyCodeCombination> entry : JUMP_TO_NTH_ISSUE_KEYS.entrySet()) {
                if (entry.getValue().match(event)){
                    event.consume();
                    listView.selectNthItem(entry.getKey());
                    break;
                }
            }
        });
    }

    private ContextMenu setupContextMenu() {
        markAsReadUnreadMenuItem.setOnAction(e -> {
            String menuItemText = markAsReadUnreadMenuItem.getText();

            if (menuItemText.equals(markAsReadMenuItemText)) {
                markAsRead();
            } else if (menuItemText.equals(markAsUnreadMenuItemText)) {
                markAsUnread();
            }
        });

        changeLabelsMenuItem.setText(changeLabelsMenuItemText);
        changeLabelsMenuItem.setOnAction(e -> {
            changeLabels();
        });

        contextMenu.getItems().addAll(markAsReadUnreadMenuItem, changeLabelsMenuItem);
        contextMenu.setOnShowing(e -> updateContextMenu(contextMenu));
        listView.setContextMenu(contextMenu);

        return contextMenu;
    }

    private ContextMenu updateContextMenu(ContextMenu contextMenu) {
        updateMarkAsReadUnreadMenuItem();
        updateChangeLabelsMenuItem();

        return contextMenu;
    }

    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    private MenuItem updateChangeLabelsMenuItem() {
        Optional<TurboIssue> item = listView.getSelectedItem();
        if (item.isPresent()) {
            changeLabelsMenuItem.setDisable(false);
        } else {
            changeLabelsMenuItem.setDisable(true);
        }

        return changeLabelsMenuItem;
    }

    private MenuItem updateMarkAsReadUnreadMenuItem() {
        Optional<TurboIssue> item = listView.getSelectedItem();
        if (item.isPresent()) {
            markAsReadUnreadMenuItem.setDisable(false);
            TurboIssue selectedIssue = item.get();

            if (selectedIssue.isCurrentlyRead()) {
                markAsReadUnreadMenuItem.setText(markAsUnreadMenuItemText);
            } else {
                markAsReadUnreadMenuItem.setText(markAsReadMenuItemText);
            }
        } else {
            markAsReadUnreadMenuItem.setDisable(true);
        }

        return markAsReadUnreadMenuItem;
    }

    private void setFocusToFilterBox() {
        if (TestController.isTestMode()) {
            ui.triggerEvent(new UIComponentFocusEvent(UIComponentFocusEvent.EventType.FILTER_BOX));
        }
        filterTextField.requestFocus();
        filterTextField.setText(filterTextField.getText().trim());
        filterTextField.positionCaret(filterTextField.getLength());

        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (KeyboardShortcuts.downIssue.match(event) || KeyboardShortcuts.upIssue.match(event)) {
                listView.selectNthItem(1);
            }
        });
    }

    public int getIssueCount() {
        return issueCount;
    }

    public TurboIssue getSelectedIssue() {
        return listView.getSelectedItem().get();
    }

    /* Methods that perform user's actions under the context of this ListPanel */

    private void markAsRead() {
        Optional<TurboIssue> item = listView.getSelectedItem();
        if (item.isPresent()) {
            TurboIssue issue = item.get();
            issue.markAsRead(ui.prefs);

            parentPanelControl.refresh();
            listView.selectNextItem();
        }
    }

    private void markAsUnread() {
        Optional<TurboIssue> item = listView.getSelectedItem();
        if (item.isPresent()) {
            TurboIssue issue = item.get();
            issue.markAsUnread(ui.prefs);

            parentPanelControl.refresh();
        }
    }

    private void changeLabels() {
        ui.triggerEvent(new ShowLabelPickerEvent(getSelectedIssue()));
    }
}
