package com.example.travel.helpers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.travel.models.PlacesModelClass
import com.example.travel.models.TagsModelClass

//CREATE TABLE tags (id INTEGER PRIMARY KEY, name TEXT, is_active INTEGER)
//CREATE TABLE regions (id INTEGER PRIMARY KEY, name TEXT);
//CREATE TABLE cities (id INTEGER PRIMARY KEY, region_id INTEGER, name TEXT, description TEXT, image TEXT, tags TEXT, likes FLOAT, lat TEXT, lon, TEXT, is_car INTEGER, is_train INTEGER, is_bus, is_minibus);
//CREATE TABLE places (id INTEGER PRIMARY KEY, name TEXT, description TEXT, image TEXT, city_id INTEGER, lat TEXT, lon, TEXT, price FLOAT);

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "Travel"
    }

    val all: Cursor
        get() = this.writableDatabase.query(DATABASE_NAME, null, null, null, null, null, null)


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

    fun price(): Array<Int> {
        val data: Array<Int> = arrayOf(0, 0)
        val selectQuery = "SELECT MIN(price) as min, MAX(price) as max FROM places"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return arrayOf()
        }
        if (cursor.moveToFirst()) {
            do {
                data.set(0, cursor.getInt(cursor.getColumnIndex("min")))
                data.set(1, cursor.getInt(cursor.getColumnIndex("max")))
            } while (cursor.moveToNext())
        }

        return data
    }

    @SuppressLint("Recycle")
    fun viewTag(sort: Boolean = false): List<TagsModelClass> {
        val empList: ArrayList<TagsModelClass> = ArrayList()
        val selectQuery = if (sort) {
            "SELECT  * FROM tags WHERE is_active = 1"
        } else {
            "SELECT  * FROM tags"
        }
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
        var tagNameEn: String
        var tagActive: Int
        if (cursor.moveToFirst()) {
            do {
                tagId = cursor.getInt(cursor.getColumnIndex("id"))
                tagName = cursor.getString(cursor.getColumnIndex("name"))
                tagNameEn = cursor.getString(cursor.getColumnIndex("name_en"))
                tagActive = cursor.getInt(cursor.getColumnIndex("is_active"))
                val emp = TagsModelClass(tagId = tagId, tagName = tagName, tagActive = tagActive)
                empList.add(emp)
            } while (cursor.moveToNext())
        }

//        addCity()
//        addPlace()
//        addEnglish()

        return empList
    }

    fun addEnglish() {
        val db = this.readableDatabase

        db?.execSQL("ALTER TABLE cities ADD COLUMN minibus_text_en TEXT DEFAULT ''")
    }

    //method to insert data
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

    fun updateTagById(id: Int, name: String, is_active: Int): Int {
        val cv = ContentValues()
        cv.put("name", name)
        cv.put("is_active", is_active)

        val whereclause = "id=?"
        val whereargs = arrayOf(id.toString())
        return this.writableDatabase.update("tags", cv, whereclause, whereargs)
    }

    fun updateTag(id: Int, isActive: Int) {
        val values = ContentValues()

        values.put("is_active", isActive)

        val db = this.writableDatabase
        db.update("tags", values, "id=?", arrayOf(id.toString()))
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
        /*contentValues.put("region_id", 5)
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
        db.insert("cities", null, contentValues)*/

        contentValues.put("region_id", 6)
        contentValues.put("name", "Могилёв")
        contentValues.put("description", "")
        contentValues.put("image", "doroga.jpg")
        contentValues.put("likes", 0)
        contentValues.put("lat", "53.90516563333846")
        contentValues.put("lon", "30.337704710084417")
        contentValues.put("is_car", 1)
        contentValues.put("is_train", 1)
        contentValues.put("is_bus", 1)
        contentValues.put("is_minibus", 1)
        db.insert("cities", null, contentValues)
        contentValues.clear()

        db.close()
        return 1
    }

    fun addPlace(): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        /*contentValues.put("tag_id", "[2,3,5]")
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
        db.insert("places", null, contentValues)*/

        contentValues.put("tag_id", "[2,6]")
        contentValues.put("city_id", 8)
        contentValues.put("name", "Драматический театр")
        contentValues.put(
            "description",
            "Из губернаторской ложи этого театра игрой актеров любовался сам император Николай II во время пребывания ставки царя в Могилеве. Сегодня Могилевский драматический театр – одна из самых известных достопримечательностей города. Здание, построенное в 1886-1888 годах в неорусском стиле из красного кирпича, расположено в историческом центре города, на Театральной Площади.\n" +
                    "\n" +
                    "Зал драмтеатра – уменьшенная копия Варшавского малого театра, отделанная  резным деревом, лепниной и бархатом. Обратите внимание на главную люстру театра – она весит больше двух тонн и горит тремя сотнями лампочек. На входе в театр вас встретит скульптура «Дама с собачкой» – сестра-близнец минской скульптуры авторства Владимира Жбанова, установленной рядом с Комаровским рынком."
        )
        contentValues.put("image", "doroga.jpg")
        contentValues.put("lat", "53.89773")
        contentValues.put("lon", "30.33282")
        contentValues.put("price", 10)
        db.insert("places", null, contentValues)
        contentValues.clear()

        contentValues.put("tag_id", "[2,3,5]")
        contentValues.put("city_id", 8)
        contentValues.put("name", "Свято-Никольский монастырь")
        contentValues.put(
            "description",
            "Свято-Никольский монастырь – один из главных православных центров Могилева и Могилевской области. Одно из зданий монастырского комплекса – Свято-Никольский собор 1669-1672 годов постройки – включено ЮНЕСКО в реестр наиболее ценных сооружений Европы в стиле барокко. В комплекс также входят Онуфриевский храм 1798 года, колокольня и церковный дом для паломников. В советский период высокохудожественные фресковые росписи стерли цементным раствором. Чудом остался только центральный иконостас Свято-Никольского собора – его стоит увидеть своими глазами."
        )
        contentValues.put("image", "doroga.jpg")
        contentValues.put("lat", "53.89379")
        contentValues.put("lon", "30.34585")
        contentValues.put("price", 0)
        db.insert("places", null, contentValues)
        contentValues.clear()

        contentValues.put("tag_id", "[2,3,5]")
        contentValues.put("city_id", 8)
        contentValues.put("name", "Городская ратуша")
        contentValues.put(
            "description",
            "С обзорной площадки этой башни городским пейзажем любовались Екатерина II и австрийский император Иосиф II. И сегодня отсюда открывается панорамный вид на Заднепровье и живописную излучину главной водной артерии города – Днепра. А через панорамный бинокль – и на весь Могилев. Восьмигранное здание каменной ратуши  высотой 46 метров на Торговой площади (нынешней Площади Славы) было построено в 1679 году. С тех пор оно разрушалось и восстанавливалось много раз." +
                    "В последний раз ратуша была восстановлена  в 2008 году, став местом притяжения для местных и туристов. Могилевчане устраивают здесь свадебные фотосессии, а туристы едут в музей истории города, экспозиционные залы которого занимают два этажа. В ратуше есть две уникальные вещи: башенные часы, механизм которых приводится в действие тяжелыми гирями, и механическая фигурка мальчика-горниста Могислава, как его зовут местные. Трижды в день Могислав трубит фанфару на балконе ратуши: в 12.00, 20.00 и в момент, когда в городе садится солнце. И это – must see в Могилеве."
        )
        contentValues.put("image", "doroga.jpg")
        contentValues.put("lat", "53.89455")
        contentValues.put("lon", "30.33195")
        contentValues.put("price", 0)
        db.insert("places", null, contentValues)
        contentValues.clear()

        db.close()
        return 1
    }

    @SuppressLint("Recycle")
    fun viewPlace(sort: Int, priceMin: Int, priceMax: Int): List<PlacesModelClass> {
        val empList: ArrayList<PlacesModelClass> = ArrayList()
        val sortStr = if (sort == 0) "cities.name" else "places.price"
        val selectQuery =
            "SELECT places.id, tag_id, cities.name AS city_name, cities.name_en AS city_name_en, cities.is_car, cities.is_train, cities.is_bus, cities.is_minibus, cities.minibus_text, cities.minibus_text_en, places.name, places.name_en, places.description, places.description_en, places.image, places.lat, places.lon, places.price, places.visited FROM places LEFT JOIN cities ON places.city_id = cities.id WHERE places.price >= $priceMin AND places.price <= $priceMax ORDER BY $sortStr"

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
        var placeCityNameEn: String
        var placeIsCar: Int
        var placeIsTrain: Int
        var placeIsBus: Int
        var placeIsMinibus: Int
        var placeMinibusText: String
        var placeMinibusTextEn: String
        var placeName: String
        var placeNameEn: String
        var placeDescription: String
        var placeDescriptionEn: String
        var placeImage: String
        var placeLat: String
        var placeLon: String
        var placePrice: Float
        var placeVisited: Int

        if (cursor.moveToFirst()) {
            do {
                val tagIds =
                    cursor.getString(cursor.getColumnIndex("tag_id")).removeSurrounding("[", "]")
                        .split(",").map { it.toInt() }

                var check = false
                for (tagId in tagIds)
                    for (e in viewTag(true))
                        if (tagId == e.tagId) check = true


                if (!check) continue

                placeId = cursor.getInt(cursor.getColumnIndex("id"))
                placeTagId = cursor.getString(cursor.getColumnIndex("tag_id"))
                placeCityName = cursor.getString(cursor.getColumnIndex("city_name"))
                placeCityNameEn = cursor.getString(cursor.getColumnIndex("city_name_en"))
                placeIsCar = cursor.getInt(cursor.getColumnIndex("is_car"))
                placeIsTrain = cursor.getInt(cursor.getColumnIndex("is_train"))
                placeIsBus = cursor.getInt(cursor.getColumnIndex("is_bus"))
                placeIsMinibus = cursor.getInt(cursor.getColumnIndex("is_minibus"))
                placeMinibusText = cursor.getString(cursor.getColumnIndex("minibus_text"))
                placeMinibusTextEn = cursor.getString(cursor.getColumnIndex("minibus_text_en"))
                placeName = cursor.getString(cursor.getColumnIndex("name"))
                placeNameEn = cursor.getString(cursor.getColumnIndex("name_en"))
                placeDescription = cursor.getString(cursor.getColumnIndex("description"))
                placeDescriptionEn = cursor.getString(cursor.getColumnIndex("description_en"))
                placeImage = cursor.getString(cursor.getColumnIndex("image"))
                placeLat = cursor.getString(cursor.getColumnIndex("lat"))
                placeLon = cursor.getString(cursor.getColumnIndex("lon"))
                placePrice = cursor.getFloat(cursor.getColumnIndex("price"))
                placeVisited = cursor.getInt(cursor.getColumnIndex("visited"))
                val emp = PlacesModelClass(
                    id = placeId,
                    tag_id = placeTagId,
                    city_name = placeCityName,
                    city_name_en = placeCityNameEn,
                    is_car = placeIsCar,
                    is_train = placeIsTrain,
                    is_bus = placeIsBus,
                    is_minibus = placeIsMinibus,
                    minibus_text = placeMinibusText,
                    minibus_text_en = placeMinibusTextEn,
                    name = placeName,
                    name_en = placeNameEn,
                    description = placeDescription,
                    description_en = placeDescriptionEn,
                    image = placeImage,
                    lat = placeLat,
                    lon = placeLon,
                    price = placePrice,
                    visited = placeVisited
                )
                empList.add(emp)
            } while (cursor.moveToNext())
        }
        return empList
    }

    @SuppressLint("Recycle")
    fun viewPlaceById(id: Int): PlacesModelClass {
        val selectQuery =
            "SELECT places.id, tag_id, cities.name as city_name, cities.name_en as city_name_en, cities.is_car, cities.is_train, cities.is_bus, cities.is_minibus, cities.minibus_text, cities.minibus_text_en, places.name, places.name_en, places.description, places.description_en, places.image, places.lat, places.lon, places.price, places.visited FROM places left join cities on places.city_id = cities.id where places.id = $id order by cities.name"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return PlacesModelClass(0, "", "", "", 0, 0, 0, 0, "", "","", "", "", "", "", "", "", 0.0F, 0)
        }

        cursor?.moveToFirst();

        val empList = PlacesModelClass(
            cursor.getInt(cursor.getColumnIndex("id")),
            cursor.getString(cursor.getColumnIndex("tag_id")),
            cursor.getString(cursor.getColumnIndex("city_name")),
            cursor.getString(cursor.getColumnIndex("city_name_en")),
            cursor.getInt(cursor.getColumnIndex("is_car")),
            cursor.getInt(cursor.getColumnIndex("is_train")),
            cursor.getInt(cursor.getColumnIndex("is_bus")),
            cursor.getInt(cursor.getColumnIndex("is_minibus")),
            cursor.getString(cursor.getColumnIndex("minibus_text")),
            cursor.getString(cursor.getColumnIndex("minibus_text_en")),
            cursor.getString(cursor.getColumnIndex("name")),
            cursor.getString(cursor.getColumnIndex("name_en")),
            cursor.getString(cursor.getColumnIndex("description")),
            cursor.getString(cursor.getColumnIndex("description_en")),
            cursor.getString(cursor.getColumnIndex("image")),
            cursor.getString(cursor.getColumnIndex("lat")),
            cursor.getString(cursor.getColumnIndex("lon")),
            cursor.getFloat(cursor.getColumnIndex("price")),
            cursor.getInt(cursor.getColumnIndex("visited"))
        )
        return empList
    }

    fun updateVisited(id: Int, status: Boolean): Int {
        val cv = ContentValues()
        var visited = 0
        if (status) {
            visited = 1;
        }
        cv.put("visited", visited)

        val whereclause = "id=?"
        val whereargs = arrayOf(id.toString())
        val query = this.writableDatabase.update("places", cv, whereclause, whereargs)
        return query
    }
}
