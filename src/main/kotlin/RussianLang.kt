object RussianLang {
    const val ALPHABET = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
    val freq = HashMap<Char, Float>().apply {
        put('о', 0.1097f)
        put('е', 0.0845f)
        put('а', 0.0801f)
        put('и', 0.0735f)
        put('н', 0.0670f)

        put('т', 0.0626f)
        put('с', 0.0547f)
        put('р', 0.0473f)
        put('в', 0.0454f)
        put('л', 0.0440f)

        put('к', 0.0349f)
        put('м', 0.0321f)
        put('д', 0.0298f)
        put('п', 0.0281f)
        put('у', 0.0262f)

        put('я', 0.0201f)
        put('ы', 0.0190f)
        put('ь', 0.0174f)
        put('г', 0.0170f)
        put('з', 0.0165f)

        put('б', 0.0159f)
        put('ч', 0.0144f)
        put('й', 0.0121f)
        put('х', 0.0097f)
        put('ж', 0.0094f)

        put('ш', 0.0073f)
        put('ю', 0.0064f)
        put('ц', 0.0048f)
        put('щ', 0.0036f)
        put('э', 0.0032f)

        put('ф', 0.0026f)
        put('ъ', 0.0004f)
        put('ё', 0.0004f)
    }
}