package com.jetbrains.crucible.ui.toolWindow.details;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.actions.OpenRepositoryVersionAction;
import com.intellij.openapi.vcs.changes.actions.ShowDiffWithLocalAction;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowser;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.ui.*;
import com.intellij.ui.table.JBTable;
import com.jetbrains.crucible.actions.AddCommentAction;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.utils.CrucibleBundle;
import com.jetbrains.crucible.utils.CrucibleDataKeys;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * User: ktisha
 * <p/>
 * Show changes and general comments for review
 */
public class DetailsPanel extends SimpleToolWindowPanel {

  private final Project myProject;
  private final Review myReview;
  private ChangesBrowser myChangesBrowser;
  private JBTable myCommitsTable;
  private DefaultTableModel myCommitsModel;
  private CommentsTreeTable myGeneralComments;

  @SuppressWarnings("UseOfObsoleteCollectionType")
  public DetailsPanel(@NotNull final Project project, @NotNull final Review review) {
    super(false);
    myProject = project;
    myReview = review;

    final Splitter splitter = new Splitter(false, 0.7f);
    final JPanel mainTable = createMainTable();
    splitter.setFirstComponent(mainTable);

    final JComponent repoBrowser = createRepositoryBrowserDetails();
    splitter.setSecondComponent(repoBrowser);

    setContent(splitter);
  }

  public void updateCommitsList(final @NotNull List<CommittedChangeList> changeLists) {
    for (CommittedChangeList committedChangeList : changeLists) {
      myCommitsModel.addRow(new Object[]{committedChangeList, committedChangeList.getCommitterName(), committedChangeList.getCommitDate()});
    }
  }

  public void setBusy(boolean busy) {
    myCommitsTable.setPaintBusy(busy);
  }

  @NotNull
  private JPanel createMainTable() {
    final JBSplitter splitter = new JBSplitter(true, 0.65f);
    final JScrollPane commitsPane = createCommitsPane();

    final JPanel commentsPane = createCommentsPane();
    splitter.setFirstComponent(commitsPane);
    splitter.setSecondComponent(commentsPane);
    return splitter;
  }

  @NotNull
  private JPanel createCommentsPane() {
    myGeneralComments = new CommentsTreeTable();
    myGeneralComments.setExpandableItemsEnabled(false);
    myGeneralComments.updateModel(myReview);
    setUpColumnWidths(myGeneralComments);
    return installActions();
  }



  @NotNull
  private JPanel installActions() {
    final DefaultActionGroup actionGroup = new DefaultActionGroup();
    final AddCommentAction addCommentAction = new AddCommentAction(myReview, null, null, CrucibleBundle.message("crucible.add.comment"), false);
    addCommentAction.setContextComponent(myGeneralComments);
    actionGroup.add(addCommentAction);

    final AddCommentAction replyToCommentAction =
      new AddCommentAction(myReview, null, null, CrucibleBundle.message("crucible.reply"), true);

    replyToCommentAction.setContextComponent(myGeneralComments);
    actionGroup.add(replyToCommentAction);

    final ActionPopupMenu actionPopupMenu = ActionManager.getInstance()
      .createActionPopupMenu(CrucibleBundle.message("crucible.main.name"), actionGroup);
    final JPopupMenu popupMenu = actionPopupMenu.getComponent();
    myGeneralComments.setComponentPopupMenu(popupMenu);

    final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(myGeneralComments).
      setToolbarPosition(ActionToolbarPosition.LEFT);
    decorator.addExtraAction(addCommentAction);
    decorator.addExtraAction(replyToCommentAction);

    final Border border = IdeBorderFactory.createTitledBorder(CrucibleBundle.message("crucible.general.comments"),
                                                              false);
    final JPanel decoratedPanel = decorator.createPanel();
    decoratedPanel.setBorder(border);
    return decoratedPanel;
  }

  @NotNull
  private JScrollPane createCommitsPane() {
    @SuppressWarnings("UseOfObsoleteCollectionType")
    final Vector<String> commitColumnNames = new Vector<String>();
    commitColumnNames.add(CrucibleBundle.message("crucible.commit"));
    commitColumnNames.add(CrucibleBundle.message("crucible.author"));
    commitColumnNames.add(CrucibleBundle.message("crucible.date"));

    //noinspection UseOfObsoleteCollectionType
    myCommitsModel = new DefaultTableModel(new Vector(), commitColumnNames) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }

      @Override
      public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) return CommittedChangeList.class;
        if (columnIndex == 2) return Date.class;
        return String.class;
      }
    };

    myCommitsTable = new JBTable(myCommitsModel) {
      @Override
      public TableCellRenderer getCellRenderer(int row, int column) {
        if (column == myCommitsTable.getColumnModel().getColumnIndex(CrucibleBundle.message("crucible.commit")))
          return new MyCommitsCellRenderer();
        return super.getCellRenderer(row, column);
      }
    };
    myCommitsTable.setStriped(true);
    myCommitsTable.setAutoCreateRowSorter(true);
    myCommitsTable.setExpandableItemsEnabled(false);
    setUpColumnWidths(myCommitsTable);

    return ScrollPaneFactory.createScrollPane(myCommitsTable);
  }

  public static void setUpColumnWidths(@NotNull final JBTable table) {
    final TableColumnModel columnModel = table.getColumnModel();
    columnModel.getColumn(0).setMinWidth(400);          //message
    columnModel.getColumn(0).setPreferredWidth(400);    //message
    columnModel.getColumn(1).setMinWidth(200);     //Author
    columnModel.getColumn(1).setMaxWidth(200);     //Author
    columnModel.getColumn(2).setMinWidth(130);     //Date
    columnModel.getColumn(2).setMaxWidth(130);     //Date
  }

  @NotNull
  private JComponent createRepositoryBrowserDetails() {
    myChangesBrowser = new MyChangesBrowser(myProject);

    myChangesBrowser.getDiffAction().registerCustomShortcutSet(CommonShortcuts.getDiff(), myCommitsTable);
    myChangesBrowser.getViewer().setScrollPaneBorder(IdeBorderFactory.createBorder(SideBorder.LEFT | SideBorder.TOP));

    myCommitsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        final int[] indices = myCommitsTable.getSelectedRows();
        List<Change> changes = new ArrayList<Change>();
        for (int i : indices) {
          changes.addAll(((CommittedChangeList)myCommitsModel.getValueAt(i, 0)).getChanges());
        }
        myChangesBrowser.setChangesToDisplay(changes);
      }
    });
    return myChangesBrowser;
  }


  static class MyCommitsCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      final Component orig = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      final Color bg = orig.getBackground();
      if (value instanceof CommittedChangeList) {
        final String text = ((CommittedChangeList)value).getName();

        setText("<html>" + text + "</html>");
        setToolTipText(text);
      }
      setBackground(bg);
      setBorder(BorderFactory.createLineBorder(bg));
      return this;
    }
  }

  class MyChangesBrowser extends ChangesBrowser {
    public MyChangesBrowser(Project project) {
      super(project, Collections.<CommittedChangeList>emptyList(),
            Collections.<Change>emptyList(), null, false, false, null,
            ChangesBrowser.MyUseCase.COMMITTED_CHANGES, null);
    }

    protected void buildToolBar(final DefaultActionGroup toolBarGroup) {
      super.buildToolBar(toolBarGroup);
      toolBarGroup.add(new ShowDiffWithLocalAction());
      final OpenRepositoryVersionAction action = new OpenRepositoryVersionAction();
      toolBarGroup.add(action);

      final ActionGroup group = (ActionGroup) ActionManager.getInstance().getAction("RepositoryChangesBrowserToolbar");
      final AnAction[] actions = group.getChildren(null);
      for (AnAction anAction : actions) {
        toolBarGroup.add(anAction);
      }
    }


    @Override
    public void calcData(DataKey key, DataSink sink) {
      if (key == CrucibleDataKeys.REVIEW)
        sink.put(CrucibleDataKeys.REVIEW, myReview);
      if (key == CrucibleDataKeys.SELECTED_COMMENT)
        sink.put(CrucibleDataKeys.SELECTED_COMMENT, (Comment)myGeneralComments.getValueAt(myGeneralComments.getSelectedRow(), 0));
      if (key == VcsDataKeys.SELECTED_CHANGES) {
        final List<Change> list = myViewer.getSelectedChanges();
        sink.put(VcsDataKeys.SELECTED_CHANGES, list.toArray(new Change [list.size()]));
      }
      super.calcData(key, sink);
    }
  }


}
