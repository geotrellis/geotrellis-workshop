package workshop

import java.nio.file.{Files, Paths}


object Util {
  def copyToClipboard(s: String): Unit = {
    import java.awt.Toolkit
    import java.awt.datatransfer.StringSelection;
    Toolkit.getDefaultToolkit.getSystemClipboard.setContents(new StringSelection(s), null);
    println(s"Copied: ${s.take(64)}...")
  }

  def writeToFile(s: String, fileName: String): Unit = {
    new java.io.PrintWriter(fileName) {
      write(s)
      close()
    }
    println(s"Wrote: $fileName")
  }

  def readFile(path: String): String = {
    val fileName = Paths.get(path)
    val bytes = Files.readAllBytes(fileName)
    new String(bytes, "UTF-8")
  }

  def time[R](block: => R): R = {
    val t0 = System.currentTimeMillis()
    val result = block    // call-by-name
    val t1 = System.currentTimeMillis()
    println(s"Elapsed time: ${t1-t0} ms")

    result
  }

  def fileUri(prefix: String) = s"s3://geotrellis-workshop/$prefix"
}
