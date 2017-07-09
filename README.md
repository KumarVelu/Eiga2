# Eiga2 #
This app adds additional functionality to the stage 1 of [Eiga](https://github.com/KumarVelu/Eiga)

## Features ##
* Sort Movies by popularity and rating.
* View a particular movie detail
* Play/Share Movie trailer
* Read review of a movie
* Bookmark Movies as favorite
* Offline access to bookmarked movies

## Getting Started

This app uses The Movie Database API (https://www.themoviedb.org/documentation/api). You have to provide your own API key in order to run the app.
You can create you own key with this link https://www.themoviedb.org/account/signup?language=en-EN
Once you get the API key, paste it in Eiga2/gradle.properties
as API_KEY="YOUR_API_KEY" (If you don't find gradle.properties file you can create one yourself)

(Note: In case if you are cloning this project, gradle.properties file won't be created, you will have to create it manually in the root folder of your project Eiga/gradle.properties)

## Library used

[Picasso](http://square.github.io/picasso/) - A powerful image downloading and caching library for Android.

[Retrofit](http://square.github.io/retrofit/) - A type-safe HTTP client for Android and Java.

[Butter Knife](http://jakewharton.github.io/butterknife/) - Field and method binding for Android views
