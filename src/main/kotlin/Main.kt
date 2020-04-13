import java.io.File
import java.io.IOException

fun main(args: Array<String>) {
    tryToHack()
}

private fun encodeToEncoded() {
    val path = "E:\\univer8\\HackCipherEasyReplacement\\input.txt"
    var text: String = ""
    try {
        text = File(path).readLines().joinToString("\n")
    } catch (ioException: IOException) {
        println(ioException.message)
        return
    }
    val encodedText = Encode(text).getProcessText()
    val file = File("E:\\univer8\\HackCipherEasyReplacement\\encoded.txt")
    file.writeText(encodedText)
}

private fun tryToHack() {
    val path = "E:\\univer8\\HackCipherEasyReplacement\\encoded.txt"
    var text: String = ""
    try {
        text = File(path).readLines().joinToString("\n")
    } catch (ioException: IOException) {
        println(ioException.message)
        return
    }
    val hackedText = HackCipher(text).hacked()
    val file = File("E:\\univer8\\HackCipherEasyReplacement\\hacked.txt")
    file.writeText(hackedText)
}