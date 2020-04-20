import java.io.File
import java.io.IOException
import kotlin.math.pow
import kotlin.random.Random

class HackCipher(private val text: String) {
    companion object {
        private const val MIN_FITNESS_CHAR = 0.0009
    }
    private var getDecoded = HashMap<Char, Char>()
    private var currentPermutation = RussianLang.ALPHABET.toList().shuffled()
    private var swapIdx1 = 0
    private var swapIdx2 = 0
    private val dictionary = loadDictionary()
    private val badWords = ArrayList<String>()
    private var needAddThisWordToBad = false
    private var successChangePermutationByWords = false
    private var endOfPermutations = false

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
        // пороговое значение, до которого сбрасываем с помощью похожести частот одного символа
        while (fitness > MIN_FITNESS_CHAR) {
//        while (it < 5000) {
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
        // пороговое значение совпадения слов, при 0 - все слова из словаря
        while (fitnessWords > text.length.toFloat() / 1000 && !endOfPermutations) {
            successChangePermutationByWords = false
            nextPermutationByWords(applyPermutationToText())
            val newFitnessWords = fitnessWord(applyPermutationToText())
            if (newFitnessWords >= fitnessWords) {
                if (successChangePermutationByWords) {
                    rollbackPermutation()
                }
                needAddThisWordToBad = true
            } else {
                // если вдруг смогли спуститься вниз, то почистим слова, чтобы обрабатывать их потом, так как они изменились
                badWords.clear()
                fitnessWords = newFitnessWords
            }
            println("Шаг $it, newFitnessWords $fitnessWords")
            it++
        }
        println(badWords)
        println(currentPermutation.joinToString(""))
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

    private fun nextPermutationByWords(testText: String) {
        val testWords = testText.split(Regex("[^а-яА-Я]")).filter { it.isNotEmpty() }.sortedByDescending { it.length }

        nextPermutationByWords(testWords, 1)
    }

    private fun nextPermutationByWords(testWords: List<String>, currentMinDiff: Int) {
        println("NEXT WORDS PERMUTATION min: $currentMinDiff, badWordsSize: ${badWords.size}")
        val someWord = testWords.find { findMinDiffWord(it).first == currentMinDiff && !badWords.contains(it) }
        if (someWord != null) {
            if (needAddThisWordToBad) {
                badWords.add(someWord)
                needAddThisWordToBad = false
            }
            val needWord = findMinDiffWord(someWord).second
            var iter = 0
            while (someWord[iter] == needWord[iter]) {
                iter++
            }
            val oldChar = someWord[iter]
            val needChar = needWord[iter]
            swapIdx1 = RussianLang.ALPHABET.indexOf(oldChar)
            swapIdx2 = RussianLang.ALPHABET.indexOf(needChar)
            if (swapIdx1 != -1 && swapIdx2 != -1) {
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
                successChangePermutationByWords = true
            } else {
                badWords.add(someWord)
                println("ERROR find char $oldChar or $needChar")
            }
        } else {
            if (currentMinDiff < 49) {
                nextPermutationByWords(testWords, currentMinDiff + 1)
            } else {
                endOfPermutations = true
                println("Error find word for correct")
            }
        }
    }

    private fun fitnessWord(testText: String): Double {
        val testWords = testText.split(Regex("[^а-яА-Я]")).filter { it.isNotEmpty() }

        val startTime = System.currentTimeMillis()

        val minWordDiff = testWords.stream()
            .parallel()
            .mapToDouble { word ->
                findMinDiffWord(word).first.toDouble() / word.length
            }
        println("Fitness word time: ${(System.currentTimeMillis() - startTime) / 1000} s")
        return minWordDiff.sum()
    }

    private fun findMinDiffWord(word: String): Pair<Int, String> {
        var min = 50 // максимальная разница в слове
        var rightWord = ""
        dictionary[word.length]?.forEach {
            val wordDiff = wordDiff(word, it)
            if (wordDiff < min) {
                min = wordDiff
                rightWord = it
            }
        }
        return Pair(min, rightWord)
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