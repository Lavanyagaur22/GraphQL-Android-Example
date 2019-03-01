package com.lavanya.graphqlandroid

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.apollographql.apollo.*
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.sample.FindQuery
import kotlinx.android.synthetic.main.repo_layout.*
import okhttp3.OkHttpClient


class MainActivity : AppCompatActivity() {

    private val BASE_URL = "https://api.github.com/graphql"

    private lateinit var client: ApolloClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        client = setUpApollo()

        button_find.setOnClickListener {
            progress_bar.visibility = View.VISIBLE

            client.query(
                FindQuery
                    .builder()
                    .name(repo_name_edittext.toString())
                    .owner(owner_name_edittext.toString())
                    .build()
            )
                .enqueue(object : ApolloCall.Callback<FindQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        Log.e("TAG1", e.message.toString())
                    }

                    override fun onResponse(response: Response<FindQuery.Data>) {

                        Log.e("TAG2", "${response.data()?.repository()}")

                        runOnUiThread {
                            progress_bar.visibility = View.GONE

                            name_text_view.text = String.format(
                                getString(R.string.name_text),
                                response.data()?.repository()?.name()
                            )

                            description_text_view.text = String.format(
                                getString(R.string.description_text),
                                response.data()?.repository()?.description()
                            )
                            forks_text_view.text = String.format(
                                getString(R.string.fork_count_text),
                                response.data()?.repository()?.forkCount().toString()
                            )
                            url_text_view.text = String.format(
                                getString(R.string.url_count_text),
                                response.data()?.repository()?.url().toString()
                            )


                        }


                    }

                })
        }


    }


    private fun setUpApollo(): ApolloClient {
        val okHttp = OkHttpClient.Builder()
            //application interceptor
            //it->chain
            .addInterceptor {
                val original = it.request()
                val builder = original.newBuilder().method(
                    original.method(),
                    original.body()
                )
                builder.addHeader(
                    "Authorisation",
                    "Bearer" + getString(R.string.AUTH_TOKEN)
                )
                it.proceed(builder.build())
            }
            .build()

        return ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttp)
            .build()

    }
}
