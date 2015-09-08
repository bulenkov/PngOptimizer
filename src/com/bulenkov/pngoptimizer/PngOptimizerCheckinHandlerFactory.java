package com.bulenkov.pngoptimizer;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairConsumer;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Konstantin Bulenkov
 */
public class PngOptimizerCheckinHandlerFactory extends CheckinHandlerFactory {
  private static final String PNG_OPTIMIZER_STATE_KEY = "PNG_OPTIMIZER_STATE_KEY";

  @NotNull
  @Override
  public CheckinHandler createHandler(@NotNull final CheckinProjectPanel panel, @NotNull CommitContext context) {
    return new CheckinHandler() {
      @Override
      public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        final JCheckBox checkBox = new JCheckBox("Optimize PNG files");
        return new RefreshableOnComponent() {
          public JComponent getComponent() {
            if (getPngFiles(panel).isEmpty()) return JBUI.Panels.simplePanel();
            return JBUI.Panels.simplePanel().addToLeft(checkBox);
          }

          public void refresh() {
          }

          public void saveState() {
            PropertiesComponent.getInstance().setValue(PNG_OPTIMIZER_STATE_KEY, checkBox.isSelected());
          }

          public void restoreState() {
            checkBox.setSelected(isOptimizeEnabled());
          }
        };
      }

      @Override
      public ReturnResult beforeCheckin(@Nullable CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
        if (isOptimizeEnabled()) {
          ArrayList<VirtualFile> pngFiles = getPngFiles(panel);

          if (!pngFiles.isEmpty()) {
            try {
              OptimizePngAction.optimize(panel.getProject(), pngFiles);
            } catch (IOException e) {
//              e.printStackTrace();
            }
            FileDocumentManager.getInstance().saveAllDocuments();
          }
        }
        return super.beforeCheckin();
      }
    };
  }

  @NotNull
  public static ArrayList<VirtualFile> getPngFiles(@NotNull CheckinProjectPanel panel) {
    ArrayList<VirtualFile> pngFiles = new ArrayList<VirtualFile>();
    for (VirtualFile file : panel.getVirtualFiles()) {
      if ("png".equalsIgnoreCase(file.getExtension())) {
        pngFiles.add(file);
      }
    }
    return pngFiles;
  }

  public static boolean isOptimizeEnabled() {
    return PropertiesComponent.getInstance().getBoolean(PNG_OPTIMIZER_STATE_KEY, true);
  }
}
