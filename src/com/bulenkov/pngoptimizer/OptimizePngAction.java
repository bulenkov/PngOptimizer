package com.bulenkov.pngoptimizer;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Konstantin Bulenkov
 */
public class OptimizePngAction extends DumbAwareAction {
  @Override
  public void actionPerformed(AnActionEvent e) {
    VirtualFile[] files = e.getRequiredData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
    try {
      optimize(e.getProject(), Arrays.asList(files), true);
    } catch (IOException e1) {
//      e1.printStackTrace();
    }
  }

  private void optimizeAll(Project project, @NotNull VirtualFile[] files) {
  }

  @Override
  public void update(AnActionEvent e) {
    VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
    if (files != null && e.getProject() != null) {
      for (VirtualFile file : files) {
        if (file.isDirectory() || isPngFile(file)) {
          e.getPresentation().setEnabledAndVisible(true);
          return;
        }
      }
    }

    e.getPresentation().setEnabledAndVisible(false);
  }

  public static long optimize(Project project, final Collection<VirtualFile> files) throws IOException {
    return optimize(project, files, false);
  }
  public static long optimize(final Project project, final Collection<VirtualFile> files, final boolean showBalloon) throws IOException {
    return ProgressManager.getInstance().runProcessWithProgressSynchronously(new ThrowableComputable<Long, IOException>() {
      @Override
      public Long compute() throws IOException {
        long optimized = 0;
        int optimizedFiles = 0;
        LinkedList<VirtualFile> images = new LinkedList<VirtualFile>(files);

        while (!images.isEmpty()) {
          VirtualFile file = images.pop();
          if (file.isDirectory()) {
            for (VirtualFile f : file.getChildren()) {
              images.push(f);
            }
          } else if (isPngFile(file)) {
            long profit = optimize(file);
            if (profit > 0) {
              optimized += profit;
              optimizedFiles++;
            }
          }
        }
        if (showBalloon) {
          new NotificationGroup("PNG Optimizer", NotificationDisplayType.BALLOON, true)
              .createNotification(optimizedFiles + " files were optimized<br/>" + optimized + " bytes saved", NotificationType.INFORMATION).notify(project);
        }
        return optimized;
      }
    }, "Optimizing PNG Files", true, project);
  }

  public static boolean isPngFile(VirtualFile file) {
    return file != null && !file.isDirectory() && "png".equalsIgnoreCase(file.getExtension());
  }

  private static long optimize(VirtualFile f) throws IOException {
    File file = new File(f.getPath());
    long size = file.length();
    BufferedImage image = ImageIO.read(file);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", byteArrayOutputStream);

    byte[] bytes = byteArrayOutputStream.toByteArray();

    if (size > bytes.length) {
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(file);
        fos.write(bytes);
        return size - bytes.length;
      } finally {
        if (fos != null) {
          fos.close();
        }
      }
    }
    return 0;
  }
}
