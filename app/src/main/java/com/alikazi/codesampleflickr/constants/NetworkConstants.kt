package com.alikazi.codesampleflickr.constants

/**
 * Created by kazi_ on 22-May-18.
 */
class NetworkConstants {

    companion object {

        const val SCHEME_HTTPS = "https"

//        https://api.flickr.com/services/feeds/photos_public.gne?format=json&nojsoncallback=1
        const val URL_AUTHORITY = "api.flickr.com"
        const val URL_PATH_SERVICES = "services"
        const val URL_PATH_FEEDS = "feeds"
        const val URL_PATH_PHOTOS_PUBLIC_GNE = "photos_public.gne"
        const val URL_QUERY_FORMAT = "format"
        const val URL_QUERY_FORMAT_VALUE = "json"
        const val URL_QUERY_NO_JSON_CALLBACK = "nojsoncallback"
    }

}
