package com.example.travel.helpers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.travel.fragments.WindowFragment
import com.example.travel.models.PlacesModelClass
import com.example.travel.models.TagsModelClass

//CREATE TABLE tags (id INTEGER PRIMARY KEY, name TEXT, is_active INTEGER)
//CREATE TABLE regions (id INTEGER PRIMARY KEY, name TEXT);
//CREATE TABLE cities (id INTEGER PRIMARY KEY, region_id INTEGER, name TEXT, description TEXT, image TEXT, tags TEXT, likes FLOAT, lat TEXT, lon, TEXT, is_car INTEGER, is_train INTEGER, is_bus, is_minibus);
//CREATE TABLE places (id INTEGER PRIMARY KEY, name TEXT, description TEXT, image TEXT, city_id INTEGER, lat TEXT, lon, TEXT, price FLOAT);


/*
//TODO
// Группировка по городам
// Сортировка по цене и расстоянию, алфавиту
//
// ******************************************
//TODO
// Цена: берётся средняя по местам в городе
*/


class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "Travel"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE tags (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, is_active INTEGER)")
        db?.execSQL("CREATE TABLE regions (id INTEGER PRIMARY KEY AUTOINCREMENT, name)")
        db?.execSQL("CREATE TABLE cities (id INTEGER PRIMARY KEY AUTOINCREMENT, region_id INTEGER, name TEXT, description TEXT, image TEXT/*, tags TEXT*/, likes FLOAT, lat TEXT, lon TEXT, is_car INTEGER, is_train INTEGER, is_bus INTEGER, is_minibus INTEGER)")
        db?.execSQL("CREATE TABLE places (id INTEGER PRIMARY KEY AUTOINCREMENT, tag_id TEXT, city_id INTEGER, name TEXT, description TEXT, image TEXT, lat TEXT, lon TEXT, price FLOAT)")

        Log.d("TAG", "TABLES CREATE")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS tags")
        db.execSQL("DROP TABLE IF EXISTS regions")
        db.execSQL("DROP TABLE IF EXISTS cities")
        db.execSQL("DROP TABLE IF EXISTS places")
        onCreate(db)
    }

    @SuppressLint("Recycle")
    fun viewTag(): List<TagsModelClass> {
        val empList: ArrayList<TagsModelClass> = ArrayList()
        val selectQuery = "SELECT  * FROM tags"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var tagId: Int
        var tagName: String
        var tagActive: Int
        if (cursor.moveToFirst()) {
            do {
                tagId = cursor.getInt(cursor.getColumnIndex("id"))
                tagName = cursor.getString(cursor.getColumnIndex("name"))
                tagActive = cursor.getInt(cursor.getColumnIndex("is_active"))
                val emp = TagsModelClass(tagId = tagId, tagName = tagName, tagActive = tagActive)
                empList.add(emp)
            } while (cursor.moveToNext())
        }
        return empList
    }

    //method to insert data
//    fun addTag(tag: TagsModelClass):Long {
    fun addTag(): Long {
        val arr = arrayOf(
            "Парк",
            "Исторические сооружения",
            "Архитектура",
            "Граффити",
            "Достопримечательность города",
            "Театр",
            "Музей",
            "Отдых",
            "Площадь",
            "Зоосад",
            "Зоопарк",
            "Батанический сад",
            "Аквапарк",
            "Контактный зоопарк",
            "Водоёмы"
        )
        val db = this.writableDatabase

        for (item in arr) {
            val contentValues = ContentValues()
            contentValues.put("name", item)
            contentValues.put("is_active", 0)
            db.insert("tags", null, contentValues)
        }
        db.close()
        return 1
    }

    fun updateTag(tag: TagsModelClass): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("id", tag.tagId)
        contentValues.put("name", tag.tagName)
        contentValues.put("is_active", tag.tagActive)

        val success = db.update("tags", contentValues, "id=" + tag.tagId, null)
        db.close()
        return success
    }

    fun addRegion(): Long {
        val arr = arrayOf(
            "Брестская область",
            "Витебская область",
            "Гомельская область",
            "Гродненская область",
            "Минская область",
            "Могилёвская область"
        )
        val db = this.writableDatabase

        for (item in arr) {
            val contentValues = ContentValues()
            contentValues.put("name", item)
            db.insert("regions", null, contentValues)
        }
        db.close()
        return 1
    }

    fun addCity(): Long {
        val db = this.writableDatabase
//        insert into cities(region_id, name, description, image, likes, lat, lon, is_car, is_train, is_bus, is_minibus) values (5, "", "", "", 0, "", "", 1, 1, 1, 1 )

        val contentValues = ContentValues()
        contentValues.put("region_id", 5)
        contentValues.put("name", "Минск")
        contentValues.put(
            "description",
            "В Минске каждый путешественник найдет для себя что-то интересное. Здесь можно погулять по чистым уютным улочкам и зеленым паркам, посетить различные музеи или выставки. Любители архитектуры оценят памятники конструктивизма и сталинского ампира, а также старинные храмы и костелы. Туристы с детьми смогут отдохнуть в зоопарке или аквапарке. Мест, достойных внимания, в белорусской столице много"
        )
        contentValues.put("image", "doroga.jpg")
        contentValues.put("likes", 0)
        contentValues.put("lat", "53.90080185451262")
        contentValues.put("lon", "27.55919009885386")
        contentValues.put("is_car", 1)
        contentValues.put("is_train", 1)
        contentValues.put("is_bus", 1)
        contentValues.put("is_minibus", 1)
        db.insert("cities", null, contentValues)

        db.close()
        return 1
    }

    fun addPlace(): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("tag_id", "[2,3,5]")
        contentValues.put("city_id", 1)
        contentValues.put("name", "Верхний город")
        contentValues.put(
            "description",
            "Верхний город, или Высокий рынок, — исторический центр Минска и одно из любимых мест туристов и жителей города. Так называется район площади Свободы и соседние улицы. Верхний город начали застраивать в 16 веке как новый центр Минска, после того как город пострадал от пожаров и набегов крымских татар. Здесь жили богатые горожане, а сам район был окружен земляным валом.\n" +
                    "\n" +
                    "Сегодня в Верхнем городе можно увидеть хорошо сохранившиеся и отреставрированные здания 16-18 веков: Свято-Духов собор, бывший костел Святого Иосифа и базилианский монастырь, драматический театр им. М. Горького, Собор Пресвятой Девы Марии, гостиный двор. Ходят слухи, что под Верхним городом существуют подземные ходы, которые соединяют главные храмы.\n" +
                    "\n" +
                    "В Верхнем городе лучше всего ощущается европейский дух белорусской столицы. Сюда приходят, чтобы полюбоваться на старинные здания, посидеть в уютных кафе, послушать уличных музыкантов и купить сувениров. Также отсюда открывается отличная панорама на окрестности и реку Немигу."
        )
        contentValues.put("image", "doroga.jpg")
        contentValues.put("lat", "53.902976971817786")
        contentValues.put("lon", "27.553953868508213")
        contentValues.put("price", 0)

        db.insert("places", null, contentValues)


        db.close()
        return 1
    }

    @SuppressLint("Recycle")
    fun viewPlace(): List<PlacesModelClass> {
        val empList: ArrayList<PlacesModelClass> = ArrayList()
        val selectQuery =
            "SELECT places.id, tag_id, cities.name as city_name, places.name, places.description, places.image, places.lat, places.lon, places.price FROM places left join cities on places.city_id = cities.id order by cities.name"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var placeId: Int
        var placeTagId: String
        var placeCityName: String
        var placeName: String
        var placeDescription: String
        var placeImage: String
        var placeLat: String
        var placeLon: String
        var placePrice: Float

        if (cursor.moveToFirst()) {
            do {
                placeId = cursor.getInt(cursor.getColumnIndex("id"))
                placeTagId = cursor.getString(cursor.getColumnIndex("tag_id"))
                placeCityName = cursor.getString(cursor.getColumnIndex("city_name"))
                placeName = cursor.getString(cursor.getColumnIndex("name"))
                placeDescription = cursor.getString(cursor.getColumnIndex("description"))
                placeImage = cursor.getString(cursor.getColumnIndex("image"))
                placeLat = cursor.getString(cursor.getColumnIndex("lat"))
                placeLon = cursor.getString(cursor.getColumnIndex("lon"))
                placePrice = cursor.getFloat(cursor.getColumnIndex("price"))
                val emp = PlacesModelClass(
                    id = placeId,
                    tag_id = placeTagId,
                    city_name = placeCityName,
                    name = placeName,
                    description = placeDescription,
                    image = placeImage,
                    lat = placeLat,
                    lon = placeLon,
                    price = placePrice
                )
                empList.add(emp)
            } while (cursor.moveToNext())
        }
        return empList
    }

    @SuppressLint("Recycle")
    fun viewPlaceById(id: Int): PlacesModelClass {
        val selectQuery = "SELECT places.id, tag_id, cities.name as city_name, places.name, places.description, places.image, places.lat, places.lon, places.price FROM places left join cities on places.city_id = cities.id where places.id = $id order by cities.name"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return PlacesModelClass(0, "", "", "", "", "", "", "", 0.0F)
        }

        cursor?.moveToFirst();

        val empList = PlacesModelClass(
            cursor.getInt(cursor.getColumnIndex("id")),
            cursor.getString(cursor.getColumnIndex("tag_id")),
            cursor.getString(cursor.getColumnIndex("city_name")),
            cursor.getString(cursor.getColumnIndex("name")),
            cursor.getString(cursor.getColumnIndex("description")),
            cursor.getString(cursor.getColumnIndex("image")),
            cursor.getString(cursor.getColumnIndex("lat")),
            cursor.getString(cursor.getColumnIndex("lon")),
            cursor.getFloat(cursor.getColumnIndex("price"))
        )
        return empList
    }

    fun openConnect(): SQLiteDatabase? {
        val db = this.writableDatabase
        return db;
    }
}
