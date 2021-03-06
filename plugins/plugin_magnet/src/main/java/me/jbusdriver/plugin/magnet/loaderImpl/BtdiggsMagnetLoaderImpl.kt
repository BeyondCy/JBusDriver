package me.jbusdriver.plugin.magnet.loaderImpl

import android.util.Log

import me.jbusdriver.plugin.magnet.IMagnetLoader
import org.json.JSONObject
import org.jsoup.Jsoup

class BtdiggsMagnetLoaderImpl : IMagnetLoader {
    //  key -> page
    private val search = "https://www.btdigg.xyz/search/%s/%s/1/0.html"

    override var hasNexPage: Boolean = true
    val TAG = "Btdiggs"

    override fun loadMagnets(key: String, page: Int): List<JSONObject> {
        val url = search.format(encode(key), page)
        Log.w(TAG, "load url :$url")
        val doc = Jsoup.connect(url).initHeaders().get()
        Log.w(TAG, "load doc :${doc.title()}")
        hasNexPage = doc.select(".page-split :last-child[title]").size > 0
        return doc.select(".list dl").map {
            val href = it.select("dt a")
            val title = href.text()
            val url = href.attr("href")

            val realUrl = when {
                url.startsWith("www.") -> "https://$url"
                url.startsWith("/magnet") -> {
                    IMagnetLoader.MagnetFormatPrefix + url.removePrefix("/magnet/").removeSuffix(".html")
                }
                else -> "https://www.btdigg.xyz$url"
            }

            val labels = it.select(".attr span")
            JSONObject().apply {
               put("name", title)
               put("size", labels.component2().text())
               put("date", labels.component1().text())
               put("link", realUrl)
            }


        }

    }

    override fun fetchMagnetLink(url: String): String {
        return (IMagnetLoader.MagnetFormatPrefix + Jsoup.connect(url).get().select(".content .infohash").text().trim())
    }
}