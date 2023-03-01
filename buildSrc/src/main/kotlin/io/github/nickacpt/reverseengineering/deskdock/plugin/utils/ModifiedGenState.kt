/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.nickacpt.reverseengineering.deskdock.plugin.utils

import net.fabricmc.mappings.EntryTriple
import net.fabricmc.mappings.MappingsProvider
import net.fabricmc.stitch.commands.GenMap
import net.fabricmc.stitch.representation.*
import net.fabricmc.stitch.util.MatcherUtil
import net.fabricmc.stitch.util.StitchUtil
import org.objectweb.asm.Opcodes
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class ModifiedGenState {
    private val counters: MutableMap<String, Int> = HashMap()
    private val values: MutableMap<AbstractJarEntry, Int> = IdentityHashMap()
    private var oldToIntermediary: GenMap? = null
    private var newToOld: GenMap? = null
    private var newToIntermediary: GenMap? = null
    private var interactive = true
    private var writeAll = false
    private val scanner = Scanner(System.`in`)
    private var targetNamespace = "net/minecraft/"
    private val obfuscatedPatterns: MutableList<Pattern> = ArrayList()
    fun setWriteAll(writeAll: Boolean) {
        this.writeAll = writeAll
    }

    fun disableInteractive() {
        interactive = false
    }

    fun next(entry: AbstractJarEntry, name: String): String {
        return name + "_" + values.computeIfAbsent(entry) { e: AbstractJarEntry? ->
            val v = counters.getOrDefault(name, 1)
            counters[name] = v + 1
            v
        }
    }

    fun setTargetNamespace(namespace: String) {
        if (namespace.lastIndexOf("/") != namespace.length - 1) targetNamespace = "$namespace/" else targetNamespace =
            namespace
    }

    fun clearObfuscatedPatterns() {
        obfuscatedPatterns.clear()
    }

    @Throws(PatternSyntaxException::class)
    fun addObfuscatedPattern(regex: String?) {
        obfuscatedPatterns.add(Pattern.compile(regex))
    }

    fun setCounter(key: String, value: Int) {
        counters[key] = value
    }

    fun getCounters(): Map<String, Int> {
        return Collections.unmodifiableMap(counters)
    }

    @Throws(IOException::class)
    fun generate(file: File, jarEntry: JarRootEntry, jarOld: JarRootEntry?) {
        if (file.exists()) {
            System.err.println("Target file exists - loading...")
            newToIntermediary = GenMap()
            FileInputStream(file).use { inputStream ->
                newToIntermediary!!.load(
                    MappingsProvider.readTinyMappings(inputStream),
                    "official",
                    "intermediary"
                )
            }
        }
        FileWriter(file).use { fileWriter ->
            BufferedWriter(fileWriter).use { writer ->
                writer.write("v1\tofficial\tintermediary\n")
                for (c in jarEntry.classes) {
                    addClass(writer, c, jarOld, jarEntry, targetNamespace)
                }
                writeCounters(writer)
            }
        }
    }

    private fun getFieldName(storage: ClassStorage, c: JarClassEntry, f: JarFieldEntry): String? {
        if (!isMappedField(storage, c, f)) {
            return null
        }
        if (newToIntermediary != null) {
            val findEntry = newToIntermediary!!.getField(c.fullyQualifiedName, f.name, f.descriptor)
            if (findEntry != null) {
                return if (findEntry.name.contains("field_")) {
                    findEntry.name
                } else {
                    val newName = next(f, "field")
                    println(findEntry.name + " is now " + newName)
                    newName
                }
            }
        }
        if (newToOld != null) {
            var findEntry = newToOld!!.getField(c.fullyQualifiedName, f.name, f.descriptor)
            if (findEntry != null) {
                findEntry = oldToIntermediary!!.getField(findEntry)
                if (findEntry != null) {
                    return if (findEntry.name.contains("field_")) {
                        findEntry.name
                    } else {
                        val newName = next(f, "field")
                        println(findEntry.name + " is now " + newName)
                        newName
                    }
                }
            }
        }
        return next(f, "field")
    }

    private val methodNames: MutableMap<JarMethodEntry, String> = IdentityHashMap()

    init {
        obfuscatedPatterns.add(Pattern.compile("^[^/]*$")) // Default ofbfuscation. Minecraft classes without a package are obfuscated.
    }

    private fun getPropagation(storage: ClassStorage, classEntry: JarClassEntry?): String {
        if (classEntry == null) {
            return ""
        }
        val builder = StringBuilder(classEntry.fullyQualifiedName)
        val strings: MutableList<String> = ArrayList()
        var scs = getPropagation(storage, classEntry.getSuperClass(storage))
        if (!scs.isEmpty()) {
            strings.add(scs)
        }
        for (ce in classEntry.getInterfaces(storage)) {
            scs = getPropagation(storage, ce)
            if (!scs.isEmpty()) {
                strings.add(scs)
            }
        }
        if (!strings.isEmpty()) {
            builder.append("<-")
            if (strings.size == 1) {
                builder.append(strings[0])
            } else {
                builder.append("[")
                builder.append(StitchUtil.join(",", strings))
                builder.append("]")
            }
        }
        return builder.toString()
    }

    private fun getNamesListEntry(storage: ClassStorage, classEntry: JarClassEntry): String {
        val builder = StringBuilder(getPropagation(storage, classEntry))
        if (classEntry.isInterface) {
            builder.append("(itf)")
        }
        return builder.toString()
    }

    private fun findNames(
        storageOld: JarRootEntry?,
        storageNew: ClassStorage,
        c: JarClassEntry,
        m: JarMethodEntry,
        names: MutableMap<String, MutableSet<String>>
    ): Set<JarMethodEntry> {
        val allEntries: MutableSet<JarMethodEntry> = HashSet()
        findNames(storageOld, storageNew, c, m, names, allEntries)
        return allEntries
    }

    private fun findNames(
        storageOld: JarRootEntry?,
        storageNew: ClassStorage,
        c: JarClassEntry,
        m: JarMethodEntry,
        names: MutableMap<String, MutableSet<String>>,
        usedMethods: MutableSet<JarMethodEntry>
    ) {
        if (!usedMethods.add(m)) {
            return
        }
        var suffix = "." + m.name + m.descriptor
        if (m.access and Opcodes.ACC_BRIDGE != 0) {
            suffix += "(bridge)"
        }
        val ccList = m.getMatchingEntries(storageNew, c)
        for (cc in ccList) {
            var findEntry: EntryTriple? = null
            if (newToIntermediary != null) {
                findEntry = newToIntermediary!!.getMethod(cc.fullyQualifiedName, m.name, m.descriptor)
                if (findEntry != null) {
                    names.computeIfAbsent(findEntry.name) { s: String? -> TreeSet() }
                        .add(getNamesListEntry(storageNew, cc) + suffix)
                }
            }
            if (findEntry == null && newToOld != null) {
                findEntry = newToOld!!.getMethod(cc.fullyQualifiedName, m.name, m.descriptor)
                if (findEntry != null) {
                    val newToOldEntry: EntryTriple = findEntry
                    findEntry = oldToIntermediary!!.getMethod(newToOldEntry)
                    if (findEntry != null) {
                        names.computeIfAbsent(findEntry.name) { s: String? -> TreeSet() }
                            .add(getNamesListEntry(storageNew, cc) + suffix)
                    } else {
                        // more involved...
                        val oldBase = storageOld?.getClass(newToOldEntry.owner, false)
                        if (oldBase != null) {
                            val oldM = oldBase.getMethod(newToOldEntry.name + newToOldEntry.desc)
                            val cccList = oldM.getMatchingEntries(storageOld, oldBase)
                            for (ccc in cccList) {
                                findEntry =
                                    oldToIntermediary!!.getMethod(ccc.fullyQualifiedName, oldM.name, oldM.descriptor)
                                if (findEntry != null) {
                                    names.computeIfAbsent(findEntry.name) { s: String? -> TreeSet() }
                                        .add(getNamesListEntry(storageOld, ccc) + suffix)
                                }
                            }
                        }
                    }
                }
            }
        }
        for (mc in ccList) {
            for (pair in mc.getRelatedMethods(m)) {
                findNames(storageOld, storageNew, pair.left, pair.left.getMethod(pair.right), names, usedMethods)
            }
        }
    }

    private fun getMethodName(
        storageOld: JarRootEntry?,
        storageNew: ClassStorage,
        c: JarClassEntry,
        m: JarMethodEntry
    ): String? {
        if (!isMappedMethod(storageNew, c, m)) {
            return null
        }
        if (methodNames.containsKey(m)) {
            return methodNames[m]
        }
        if (newToOld != null || newToIntermediary != null) {
            val names: MutableMap<String, MutableSet<String>> = HashMap()
            val allEntries = findNames(storageOld, storageNew, c, m, names)
            for (mm in allEntries) {
                if (methodNames.containsKey(mm)) {
                    return methodNames[mm]
                }
            }
            if (names.size > 1) {
                println("Conflict detected - matched same target name!")
                val nameList = ArrayList(names.keys)
                nameList.sort()
                for (i in nameList.indices) {
                    val s = nameList[i]
                    println((i + 1).toString() + ") " + s + " <- " + StitchUtil.join(", ", names[s]))
                }
                if (!interactive) {
                    throw RuntimeException("Conflict detected!")
                }
                while (true) {
                    val cmd = scanner.nextLine()
                    var i: Int
                    i = try {
                        cmd.toInt()
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                        continue
                    }
                    if (i >= 1 && i <= nameList.size) {
                        for (mm in allEntries) {
                            methodNames[mm] = nameList[i - 1]
                        }
                        println("OK!")
                        return nameList[i - 1]
                    }
                }
            } else if (names.size == 1) {
                val s = names.keys.iterator().next()
                for (mm in allEntries) {
                    methodNames[mm] = s
                }
                return if (s.contains("method_")) {
                    s
                } else {
                    val newName = next(m, "method")
                    println("$s is now $newName")
                    newName
                }
            }
        }
        return next(m, "method")
    }

    @Throws(IOException::class)
    private fun addClass(
        writer: BufferedWriter,
        c: JarClassEntry,
        storageOld: JarRootEntry?,
        storage: ClassStorage,
        translatedPrefix: String
    ) {
        var translatedPrefix = translatedPrefix
        val className = c.name
        var cname: String? = ""
        val prefixSaved = translatedPrefix
        if (!obfuscatedPatterns.stream().anyMatch { p: Pattern -> p.matcher(className).matches() }) {
            translatedPrefix = c.fullyQualifiedName
        } else {
            if (!isMappedClass(storage, c)) {
                cname = c.name
            } else {
                cname = null
                if (newToIntermediary != null) {
                    val findName = newToIntermediary!!.getClass(c.fullyQualifiedName)
                    if (findName != null) {
                        val r = findName.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        cname = r[r.size - 1]
                        if (r.size == 1) {
                            translatedPrefix = ""
                        }
                    }
                }
                if (cname == null && newToOld != null) {
                    var findName = newToOld!!.getClass(c.fullyQualifiedName)
                    if (findName != null) {
                        findName = oldToIntermediary!!.getClass(findName)
                        if (findName != null) {
                            val r = findName.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            cname = r[r.size - 1]
                            if (r.size == 1) {
                                translatedPrefix = ""
                            }
                        }
                    }
                }
                if (cname != null && !cname.contains("class_")) {
                    val newName = next(c, "class")
                    println("$cname is now $newName")
                    cname = newName
                    translatedPrefix = prefixSaved
                }
                if (cname == null) {
                    cname = next(c, "class")
                }
            }
        }
        writer.write("CLASS\t" + c.fullyQualifiedName + "\t" + translatedPrefix + cname + "\n")
        for (f in c.fields) {
            var fName = getFieldName(storage, c, f)
            if (fName == null) {
                fName = f.name
            }
            if (fName != null) {
                writer.write(
                    "FIELD\t" + c.fullyQualifiedName
                            + "\t" + f.descriptor
                            + "\t" + f.name
                            + "\t" + fName + "\n"
                )
            }
        }
        for (m in c.methods) {
            var mName = getMethodName(storageOld, storage, c, m)
            if (mName == null) {
                if (!m.name.startsWith("<") && m.isSource(storage, c)) {
                    mName = m.name
                }
            }
            if (mName != null) {
                writer.write(
                    "METHOD\t" + c.fullyQualifiedName
                            + "\t" + m.descriptor
                            + "\t" + m.name
                            + "\t" + mName + "\n"
                )
            }
        }
        for (cc in c.innerClasses) {
            addClass(writer, cc, storageOld, storage, "$translatedPrefix$cname$")
        }
    }

    @Throws(IOException::class)
    fun prepareRewrite(oldMappings: File) {
        oldToIntermediary = GenMap()
        newToOld = GenMap.Dummy()

        // TODO: only read once
        readCounters(oldMappings)
        FileInputStream(oldMappings).use { inputStream ->
            oldToIntermediary!!.load(
                MappingsProvider.readTinyMappings(inputStream),
                "official",
                "intermediary"
            )
        }
    }

    @Throws(IOException::class)
    fun prepareUpdate(oldMappings: File, matches: File?) {
        oldToIntermediary = GenMap()
        newToOld = GenMap()

        // TODO: only read once
        readCounters(oldMappings)
        FileInputStream(oldMappings).use { inputStream ->
            oldToIntermediary!!.load(
                MappingsProvider.readTinyMappings(inputStream),
                "official",
                "intermediary"
            )
        }
        FileReader(matches).use { fileReader ->
            BufferedReader(fileReader).use { reader ->
                MatcherUtil.read(
                    reader,
                    true,
                    { from: String?, to: String? -> newToOld!!.addClass(from, to) },
                    { from: EntryTriple?, to: EntryTriple? ->
                        newToOld!!.addField(
                            from,
                            to
                        )
                    }) { from: EntryTriple?, to: EntryTriple? -> newToOld!!.addMethod(from, to) }
            }
        }
    }

    @Throws(IOException::class)
    private fun readCounters(counterFile: File) {
        var counterFile: File? = counterFile
        val counterPath = externalCounterFile
        if (counterPath != null && Files.exists(counterPath)) {
            counterFile = counterPath.toFile()
        }
        FileReader(counterFile).use { fileReader ->
            BufferedReader(fileReader).use { reader ->
                var line: String
                while (reader.readLine().also { line = it } != null) {
                    if (line.startsWith("# INTERMEDIARY-COUNTER")) {
                        val parts = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        counters[parts[2]] = parts[3].toInt()
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun writeCounters(writer: BufferedWriter) {
        val counterLines = StringJoiner("\n")
        for ((key, value) in counters) {
            counterLines.add("# INTERMEDIARY-COUNTER $key $value")
        }
        writer.write(counterLines.toString())
        val counterPath = externalCounterFile
        if (counterPath != null) {
            Files.write(
                counterPath,
                counterLines.toString().toByteArray(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
    }

    private val externalCounterFile: Path?
        private get() = if (System.getProperty("stitch.counter") != null) {
            Paths.get(System.getProperty("stitch.counter"))
        } else null

    companion object {
        fun isMappedClass(storage: ClassStorage?, c: JarClassEntry?): Boolean {
            return !(c?.isAnonymous ?: false)
        }

        fun isMappedField(storage: ClassStorage?, c: JarClassEntry?, f: JarFieldEntry): Boolean {
            return isUnmappedFieldName(f.name)
        }

        fun isUnmappedFieldName(name: String): Boolean {
            return true
        }

        fun isMappedMethod(storage: ClassStorage?, c: JarClassEntry?, m: JarMethodEntry): Boolean {
            return isUnmappedMethodName(m.name) && m.isSource(storage, c)
        }

        fun isUnmappedMethodName(name: String): Boolean {
            return name[0] != '<'
        }
    }
}
