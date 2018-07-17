package com.rsinukov.shutterstockclient

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.rsinukov.shutterstockclient.features.templateslist.ui.SearchFragment

class AppActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.activity_container, SearchFragment())
                .commit()
        }
    }
}
