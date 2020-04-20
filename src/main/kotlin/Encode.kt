class Encode(private val text: String, permutation: List<Char> = RussianLang.ALPHABET.toList().shuffled()) {
    private val getEncoded: HashMap<Char, Char>
    private val getDecoded: HashMap<Char, Char>

    init {
        getEncoded = HashMap<Char, Char>().apply {
            RussianLang.ALPHABET.forEachIndexed { index, c ->
                put(c, permutation[index])
            }
        }
        getDecoded = HashMap<Char, Char>().apply {
            permutation.forEachIndexed { index, c ->
                put(c, RussianLang.ALPHABET[index])
            }
        }
    }

    fun getProcessText(encode: Boolean = true): String {
        val newText = StringBuilder()
        text.forEach {
            val origIsUpperCase = it.isUpperCase()
            val oldChar = it.toLowerCase()
            if (oldChar in RussianLang.ALPHABET) {
                val newChar = if (encode) {
                    getEncoded[oldChar]
                } else {
                    getDecoded[oldChar]
                }
                if (newChar != null) {
                    if (origIsUpperCase) {
                        newText.append(newChar.toUpperCase())
                    } else {
                        newText.append(newChar)
                    }
                }

            } else {
                newText.append(it)
            }
        }
        return String(newText)
    }
}