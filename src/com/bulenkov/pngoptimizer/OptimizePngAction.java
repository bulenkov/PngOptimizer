package com.bulenkov.pngoptimizer;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Konstantin Bulenkov
 */
public class OptimizePngAction extends DumbAwareAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {

  }

  public static long optimize(Project project, final Collection<VirtualFile> files) throws IOException {
    return ProgressManager.getInstance().runProcessWithProgressSynchronously(new ThrowableComputable<Long, IOException>() {
      @Override
      public Long compute() throws IOException {
        long optimized = 0;
        for (VirtualFile f : files) {
          File file = new File(f.getPath());
          long size = file.length();
          BufferedImage image = ImageIO.read(file);
          ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
          ImageIO.write(image, "png", byteArrayOutputStream);

          byte[] bytes = byteArrayOutputStream.toByteArray();

          if (size > bytes.length) {
            optimized += size - bytes.length;
            FileOutputStream fos = null;
            try {
              fos = new FileOutputStream(file);
              fos.write(bytes);
            } finally {
              if (fos != null) {
                fos.close();
              }
            }
          }

        }
        return optimized;

      }
    }, "Optimizing PNG Files", true, project);
  }
}
