package com.jetbrains.crucible.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.crucible.model.Comment;
import com.jetbrains.crucible.model.Review;
import com.jetbrains.crucible.ui.toolWindow.details.CommentBalloonBuilder;
import com.jetbrains.crucible.ui.toolWindow.details.CommentsTree;
import org.jetbrains.annotations.NotNull;

/**
 * User: ktisha
 * <p/>
 * Show comments for file
 */
@SuppressWarnings("ComponentNotRegistered")
public class ShowFileCommentsAction extends AnAction implements DumbAware {
  private final Comment myComment;
  private final VirtualFile myVFile;
  private final Review myReview;

  public ShowFileCommentsAction(@NotNull final Comment comment, @NotNull final VirtualFile VFile,
                                @NotNull final Review review) {
    myComment = comment;
    myVFile = VFile;
    myReview = review;
  }

  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) return;

    final Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
    if (editor == null) return;

    final CommentsTree commentsTree = new CommentsTree(myReview, myComment, editor, myVFile);
    final CommentBalloonBuilder commentBalloonBuilder = new CommentBalloonBuilder();
    commentBalloonBuilder.showBalloon(myComment, editor, commentsTree, myComment.getAuthor().getUserName());
  }
}
