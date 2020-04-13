import java.io.File
import java.io.IOException
import kotlin.math.pow
import kotlin.random.Random

class HackCipher(private val text: String) {
    private var getDecoded = HashMap<Char, Char>()
    private var currentPermutation = RussianLang.ALPHABET.toList().shuffled()
    private var swapIdx1 = 0
    private var swapIdx2 = 0


    init {
        getDecoded.clear()
        getDecoded.apply {
            currentPermutation.forEachIndexed { index, c ->
                put(c, RussianLang.ALPHABET[index])
            }
        }
    }

    private fun applyPermutationToText(): String {
        return Encode(text, currentPermutation).getProcessText(false)
    }

    private fun nextPermutation() {
        swapIdx1 = Random(System.currentTimeMillis()).nextInt(RussianLang.ALPHABET.length)
        swapIdx2 = Random(System.currentTimeMillis()).nextInt(RussianLang.ALPHABET.length)
        while (swapIdx2 == swapIdx1) {
            swapIdx2 = Random(System.currentTimeMillis()).nextInt(RussianLang.ALPHABET.length)
        }
        // меняем в перестановке
        val newPermutation = currentPermutation.toMutableList()
        val char1 = newPermutation[swapIdx1]
        newPermutation[swapIdx1] = newPermutation[swapIdx2]
        newPermutation[swapIdx2] = char1
        currentPermutation = newPermutation
        // меняем в мапе
        val value1 = getDecoded[char1]!!
        getDecoded[char1] = getDecoded[newPermutation[swapIdx1]]!!
        getDecoded[newPermutation[swapIdx1]] = value1
    }

    private fun rollbackPermutation() {
        // меняем в перестановке
        val newPermutation = currentPermutation.toMutableList()
        val char1 = newPermutation[swapIdx1]
        newPermutation[swapIdx1] = newPermutation[swapIdx2]
        newPermutation[swapIdx2] = char1
        currentPermutation = newPermutation
        // меняем в мапе
        val value1 = getDecoded[char1]!!
        getDecoded[char1] = getDecoded[newPermutation[swapIdx1]]!!
        getDecoded[newPermutation[swapIdx1]] = value1
    }

    fun hacked(): String {
        var fitness = fitness(applyPermutationToText())

        var it = 0
        while (fitness > 0.0005) {
            nextPermutation()
            val newFitness = fitness(applyPermutationToText())
            if (newFitness > fitness) {
                rollbackPermutation()
            } else {
                fitness = newFitness
            }
            println("Шаг $it, newFitness $fitness")
            it++
        }

        var fitnessWords = fitnessWord(applyPermutationToText())
        while (fitnessWords > 100 ) {
            nextPermutation()
            val newFitnessWords = fitnessWord(applyPermutationToText())
            if (newFitnessWords > fitnessWords) {
                rollbackPermutation()
            } else {
                fitnessWords = newFitnessWords
            }
            println("Шаг $it, newFitnessWords $fitnessWords")
            it++
        }
        return applyPermutationToText()
    }

    private fun fitness(testText: String): Double {
        val calculatedFreq = calculateFreq(testText)
        var quadError = mutableListOf<Pair<Char, Double>>()
        RussianLang.ALPHABET.forEach {
            quadError.add(Pair(it, (RussianLang.freq[it]!! - calculatedFreq[it]!!).toDouble().pow(2)))
        }
        quadError = quadError.sortedBy { it.second }.toMutableList()
        val fitness = quadError.sumByDouble { it.second }
        if (fitness <= 0.0005) {
            quadError.forEach {
                println("${it.first} : (${calculatedFreq[it.first]} vs ${RussianLang.freq[it.first]}) : ${it.second  / RussianLang.freq[it.first]!!}")
            }
        }
        return fitness
    }

    private fun fitnessWord(testText: String): Double {
        val testWords = testText.split(Regex("[^а-яА-Я]")).filter { it.isNotEmpty() }

        val startTime = System.currentTimeMillis()
        val dictionary = loadDictionary()
        val minWordDiff = testWords.stream()
            .parallel()
            .mapToDouble { word ->
                var min = 50 // максимальная разница в слове
                dictionary[word.length]?.forEach {
                    val wordDiff = wordDiff(word, it)
                    if (wordDiff < min) {
                        min = wordDiff
                    }
                }
                min.toDouble() / word.length
            }
        println("Fitness word time: ${(System.currentTimeMillis() - startTime) / 1000} s")
        return minWordDiff.sum()
    }

    /**
     * Разница между двумя словами, только для слов одинаковой длины
     */
    private fun wordDiff(word1: String, word2: String): Int {
        var ans = 0
        repeat(word1.length) {
            if (word1[it] != word2[it]) {
                ans++
            }
        }
        return ans
    }

    private fun loadDictionary(): HashMap<Int, ArrayList<String>> {
        val ans = HashMap<Int, ArrayList<String>>()
        try {
            val freqWithWords = File("E:\\univer8\\HackCipherEasyReplacement\\1grams-3.txt").readLines()
            freqWithWords.forEach {
                val word = it.split("\t")[1]
                if (ans[word.length] != null) {
                    ans[word.length]!!.add(word)
                } else {
                    ans[word.length] = arrayListOf(word)
                }
            }
        } catch (ioException: IOException) {
            println(ioException.message)
            return ans
        }
        return ans
    }

    private fun calculateFreq(testText: String): HashMap<Char, Float> {
        val ans = HashMap<Char, Float>()
        var count = 0
        testText.toLowerCase().forEach {
            if (it in RussianLang.ALPHABET) {
                count++
                if (ans[it] != null) {
                    ans[it] = ans[it]!! + 1
                } else {
                    ans[it] = 1f
                }
            }
        }
        ans.forEach { (c, f) ->
            ans[c] = f / count
        }
        // если вдруг какие-то не нашли
        RussianLang.ALPHABET.forEach {
            if (ans[it] == null) {
                ans[it] = 0f
            }
        }
        return ans
    }
}