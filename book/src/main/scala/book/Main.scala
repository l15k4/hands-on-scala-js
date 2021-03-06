package book
import acyclic.file
import java.io.InputStream
import java.nio.file.{Paths, Files}



object Main {
  def write(txt: String, dest: String) = {
    Paths.get(dest).toFile.getParentFile.mkdirs()
    Files.deleteIfExists(Paths.get(dest))
    Files.write(Paths.get(dest), txt.getBytes)
  }
  def copy(src: InputStream, dest: String) = {
    Paths.get(dest).toFile.getParentFile.mkdirs()
    Files.deleteIfExists(Paths.get(dest))
    Files.copy(src, Paths.get(dest))
  }

  def main(args: Array[String]): Unit = {
    println("Writing Book")
    val outputRoot = System.getProperty("output.root") + "/"
    write(Book.site, s"$outputRoot/index.html")

    val jsFiles = Book.autoResources.filter(_.endsWith(".js")).toSet
    val cssFiles = Book.autoResources.filter(_.endsWith(".css")).toSet
    val miscFiles = Book.autoResources -- cssFiles -- jsFiles

    for(res <- Book.manualResources ++ miscFiles) {
      copy(getClass.getResourceAsStream("/" + res), outputRoot + res)
    }

    for((resources, dest) <- Seq(jsFiles -> "scripts.js", cssFiles -> "styles.css")) {
      val blobs = for(res <- resources.iterator) yield {
        io.Source.fromInputStream(getClass.getResourceAsStream("/"+res)).mkString
      }

      write(blobs.mkString("\n"), outputRoot + dest)
    }

    val allNames = {
      def rec(n: Tree[String]): Seq[String] = {
        n.value +: n.children.flatMap(rec)
      }
      rec(sect.structure).toSet
    }
    val dupes = allNames.groupBy(x => x)
                        .values
                        .filter(_.size > 1)
                        .map(_.head)
                        .toSet

    assert(dupes.size == 0, s"Duplicate names: $dupes")

    val dangling = sect.usedRefs -- allNames

    assert(dangling.size == 0, s"Dangling Refs: $dangling")

    println("Writing Done")

    // can be used to verify that no links are broken
    // lnk.usedLinks
  }


}
