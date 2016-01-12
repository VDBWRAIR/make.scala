


//Path operators
implicit class Iff(val fs: Seq[Path]) extends AnyVal { 
  
  def fileReg (s:String) = ("(" ++ s.replaceAll("\\.", "\\.").replaceAll("\\*", ".*") ++ ")").r
  
  val noExt =  ((x:String) => x.split('.').init.mkString(".")) 
  
  val swapExt = ( (s:String, ext:String) => (noExt(s)) ++ "." ++ ext)
  
  def dirname (p:Path) : Path = p.segments.init match { 
    case Vector() => wd; 
    case xs       => Path("/" ++ xs.mkString("/")) }

  def %- = a => fs.map(dirname(a)/noExt(a.last))
  def %= (b: String) : Seq[Path]  = fs.map( a => dirname(a)/swapExt(a.last, b))
  def %+ (b: String) : Seq[Path]  = fs.map( a => dirname(a)/(a.last ++ "." ++ b))
  def ~= (b: String) : Seq[Path] = fs.filter(a => fileReg(b).unapplySeq(a.last).isDefined)
  def ~/ (b: String) : Seq[Path] = fs.filterNot(a => fileReg(b).unapplySeq(a.last).isDefined)
        }


object OS { 
  def FS(fs:String*): List[RelPath] = fs.map(RelPath(_)).toList
  def makeTmpFile = java.nio.file.Files.createTempFile(      java.nio.file.Paths.get(System.getProperty                           ("java.io.tmpdir")), "ammonite", ".py")

 // make a version that save stdout 
  def py(s :String) :Unit = {
    for { 
      file <- Try(Path(makeTmpFile))
      _ <- Try(write(file, s))
      result <- Try(%%('python, file))
      _ <- Try(rm! file) } yield result }
 
//    val file = Path(makeTmpFile)
//    write(file, s)
//    val ret = Try(%%('python, file))
//    rm! file 
//    ret
  
  val runPython = (List("python","-c",_:String)) andThen ( x => Try(%%(x)))
}  

//Map(NoSuchFileException -> (x) => PathNotExist(Path(x.getFile)) )

//@example 
//val sffs = (ls!) ~= "*.sff")
//val fastqs = ( (ls!) ~= "*.fastq" ) ++ (sffs %= "fastq")
//val unpaired = (ls!) ~/ "*_R[12]_*.fastq" ++ (sffs %= "fastq")
//val paired = (ls!) ~= "*_R[12]_*.fastq"



abstract class ProjectError
abstract class OSError extends ProjectError
case class PathNotExist(p: Path) extends OSError
case class PathAlreadyExist(p: Path) extends OSError
case class ExecNotExist(s: String) extends OSError
case class NonZeroReturnCode(s: Stirng, code: Int) extends OSError
