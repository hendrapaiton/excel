package akal.nari.excel.controller

import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.FileInputStream
import java.io.IOException
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

data class Anggaran(
    var tahun: Int,
    var satker: String,
    var program: String,
    var kegiatan: String,
    var capaian: String,
    var sasaran: String,
    var waktu: String,
    var estimasi: Long
)

@RestController
class FileController {

  @PostMapping(value = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  @Throws(IOException::class)
  fun requestBodyFlux(@RequestPart("file") filePart: FilePart): Mono<String> {
    val tempFile: Path = Files.createTempFile("test", filePart.filename())
    val channel = AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE)
    DataBufferUtils.write(filePart.content(), channel, 0).subscribe()
    val inputStream = FileInputStream(tempFile.toString())
    val xlwb = WorkbookFactory.create(inputStream)
    val xlWs = xlwb.getSheetAt(1)
    val xlRows = xlWs.lastRowNum
    var i = 8;
    var data: MutableList<Anggaran> = mutableListOf<Anggaran>()
    while (i <= xlRows && !xlWs.getRow(i).getCell(0).toString().isNullOrBlank()) {
      data.add(Anggaran(
          xlWs.getRow(5).getCell(5).toString().removePrefix("TAHUN ANGGARAN : ").toInt(),
          "smknj",
          xlWs.getRow(i).getCell(1).toString(),
          xlWs.getRow(i).getCell(2).toString(),
          xlWs.getRow(i).getCell(3).toString(),
          xlWs.getRow(i).getCell(4).toString(),
          xlWs.getRow(i).getCell(5).toString(),
          xlWs.getRow(i).getCell(6).toString()
              .replace(".", "")
              .replace(",-", "")
              .toLong()
      ))
      i++
    }
    Flux.fromIterable(data.toList()).subscribe(::println)
    return Mono.just(filePart.filename())
  }

}