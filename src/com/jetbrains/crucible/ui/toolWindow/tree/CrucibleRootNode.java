
package com.jetbrains.crucible.ui.toolWindow.tree;

import com.intellij.ui.treeStructure.SimpleNode;
import com.jetbrains.crucible.ui.toolWindow.CrucibleReviewModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * User : ktisha
 */
public class CrucibleRootNode extends SimpleNode {
  private static final String NAME = "All My Reviews";
  private final CrucibleReviewModel myReviewModel;
  private final List<SimpleNode> myChildren = new ArrayList<SimpleNode>();

  public CrucibleRootNode(@NotNull final CrucibleReviewModel reviewModel) {
    myReviewModel = reviewModel;
    myChildren.add(new CrucibleToReviewNode(myReviewModel));
    myChildren.add(new CrucibleRequireApprovalNode(myReviewModel));
    myChildren.add(new CrucibleOutForReviewNode(myReviewModel));
    myChildren.add(new CrucibleClosedNode(myReviewModel));
  }

  @NotNull
  public String toString() {
    return NAME;
  }

  @Override
  public SimpleNode[] getChildren() {
    if (myChildren.isEmpty()) {
      myChildren.add(new CrucibleToReviewNode(myReviewModel));
      myChildren.add(new CrucibleRequireApprovalNode(myReviewModel));
      myChildren.add(new CrucibleOutForReviewNode(myReviewModel));
      myChildren.add(new CrucibleClosedNode(myReviewModel));
    }
    return myChildren.toArray(new SimpleNode[myChildren.size()]);
  }
}
