package com.loony.bodokeyboard

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TransliterationDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "transliteration.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "learned_rules"
        private const val COLUMN_ID = "id"
        private const val COLUMN_LATIN = "latin"
        private const val COLUMN_BODO = "bodo"
        private const val COLUMN_USAGE_COUNT = "usage_count"
    }

    data class LearnedRule(val id: Int, val latin: String, val bodo: String, val count: Int)

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_LATIN + " TEXT, "
                + COLUMN_BODO + " TEXT, "
                + COLUMN_USAGE_COUNT + " INTEGER, "
                + "UNIQUE(" + COLUMN_LATIN + ", " + COLUMN_BODO + "))")
        db.execSQL(createTable)

        // val initialRules = arrayOf(
        //     arrayOf("khalamdong", "खालामदों", "10"),
        //     arrayOf("bodo", "बर'", "10"),
        //     arrayOf("mwina", "मैना", "10")
        // )

        val expandedRules = arrayOf(
            // --- Pronouns & Demonstratives ---
            arrayOf("aang", "आं", "15"),
            arrayOf("nwng", "नों", "18"),
            arrayOf("bwi", "बै", "14"),
            arrayOf("biyw", "बियो", "16"),
            arrayOf("jwng", "जों", "12"),
            arrayOf("nwngswr", "नोंसोर", "15"),
            arrayOf("biswr", "बिसोर", "15"),
            arrayOf("be", "बे", "20"),

            // --- High-Frequency Verbs ---
            arrayOf("za", "जा", "20"),
            arrayOf("thang", "थां", "15"),
            arrayOf("phai", "फाय", "15"),
            arrayOf("mwn", "मोन", "18"),
            arrayOf("hwn", "हुन", "14"),
            arrayOf("gwdan", "गुदान", "10"),
            arrayOf("rao", "राव", "13"),
            arrayOf("labw", "लाबों", "16"),
            arrayOf("lang", "लां", "14"),
            arrayOf("din", "दिन", "12"),
            arrayOf("gono", "गनो", "10"),

            // --- Interrogatives & Adverbs ---
            arrayOf("swr", "सोर", "10"),
            arrayOf("ma", "मा", "22"),
            arrayOf("bwbha", "बब्हा", "12"),
            arrayOf("mabwla", "माब्ला", "15"),
            arrayOf("borai", "बराय", "11"),
            arrayOf("bidi", "बिदि", "13"),
            arrayOf("danw", "दानो", "14"),
            arrayOf("swng", "सों", "12"),

            // --- Nouns & Environment ---
            arrayOf("bodo", "बर'", "10"),
            arrayOf("dwisri", "दुइस्रि", "10"),
            arrayOf("mansi", "मानसि", "19"),
            arrayOf("gami", "गामि", "14"),
            arrayOf("phang", "फां", "13"),
            arrayOf("hazw", "हाजो", "15"),
            arrayOf("dwi", "दै", "20"),
            arrayOf("or", "अर'", "12"),
            arrayOf("bar", "बार", "15"),
            arrayOf("okhrang", "अख्रां", "14"),
            arrayOf("san", "सान", "16"),
            arrayOf("okhafwr", "अखाफोर", "13"),

            // // --- Adjectives & Descriptors ---
            // arrayOf("gaham", "गाहाम", "18"),
            // arrayOf("gaza", "गाजा", "11"),
            // arrayOf("gwdar", "गुदार", "12"),
            // arrayOf("gezer", "गेजेर", "15"),
            // arrayOf("gwdan", "गोदान", "10"),
            // arrayOf("gwza", "गोजा", "11"),
            // arrayOf("gwdwng", "गोदों", "12"),
            // arrayOf("gusu", "गुसु", "13"),
            arrayOf("zau", "जाउ", "10"),

            // --- Conjunctions, Postpositions & Particles ---
            arrayOf("arw", "आरो", "22"),
            arrayOf("bla", "ब्ला", "17"),
            arrayOf("khow", "खौ", "25"), // Accusative case marker
            arrayOf("ni", "नि", "24"),  // Genitive case marker
            arrayOf("on", "अन", "12"),
            arrayOf("nw", "नो", "20"),   // Emphatic particle
            arrayOf("ao", "आव", "22"),  // Locative case marker
            arrayOf("zwng", "जों", "18")   // Instrumental/Comitative marker
        )
        for (rule in expandedRules) {
            db.execSQL("INSERT INTO $TABLE_NAME ($COLUMN_LATIN, $COLUMN_BODO, $COLUMN_USAGE_COUNT) VALUES (?, ?, ?)", rule)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun learn(latin: String, bodo: String) {
        // Skip storing if the engine already produces this output by default
        val engine = TransliterationEngine()
        if (engine.flush(latin) == bodo) return

        val db = this.writableDatabase
        val query = """
            INSERT INTO $TABLE_NAME ($COLUMN_LATIN, $COLUMN_BODO, $COLUMN_USAGE_COUNT) 
            VALUES (?, ?, 1)
            ON CONFLICT($COLUMN_LATIN, $COLUMN_BODO) 
            DO UPDATE SET $COLUMN_USAGE_COUNT = $COLUMN_USAGE_COUNT + 1
        """.trimIndent()
        db.execSQL(query, arrayOf(latin, bodo))
    }

    fun getLearnedRules(prefix: String): List<Pair<String, String>> {
        val rules = mutableListOf<Pair<String, String>>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_LATIN, COLUMN_BODO),
            "$COLUMN_LATIN LIKE ?",
            arrayOf("$prefix%"),
            null,
            null,
            "$COLUMN_USAGE_COUNT DESC",
            "5"
        )

        if (cursor.moveToFirst()) {
            do {
                val latin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LATIN))
                val bodo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODO))
                rules.add(latin to bodo)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return rules
    }

    fun getAllRules(): List<LearnedRule> {
        val rules = mutableListOf<LearnedRule>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COLUMN_USAGE_COUNT DESC")
        
        if (cursor.moveToFirst()) {
            do {
                rules.add(LearnedRule(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    latin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LATIN)),
                    bodo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODO)),
                    count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USAGE_COUNT))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return rules
    }

    fun deleteRule(id: Int) {
        this.writableDatabase.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun updateRule(id: Int, latin: String, bodo: String) {
        val values = ContentValues().apply {
            put(COLUMN_LATIN, latin)
            put(COLUMN_BODO, bodo)
        }
        this.writableDatabase.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun getStableRules(): Map<String, String> {
        val rules = mutableMapOf<String, String>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_LATIN, COLUMN_BODO),
            "$COLUMN_USAGE_COUNT >= ?",
            arrayOf("3"),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val latin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LATIN))
                val bodo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODO))
                rules[latin] = bodo
            } while (cursor.moveToNext())
        }
        cursor.close()
        return rules
    }
}
