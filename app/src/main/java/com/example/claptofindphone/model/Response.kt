package com.example.claptofindphone.clapToFindPhone.model


data class ResponseData(
    val code: Int,
    val message: String,
    val data: Data
) {

    data class Data(
        val platform: String,
        val screenshot: List<String>,
        val opened: Int,
        val installed: Int,
        val name: String,
        val logo: String,
        val banner: String,
        val packageName: String,
        val description: String,
        val adsProvider: List<AdsProvider>,
        val id: String
    ) {

        data class AdsProvider(
            val _id: String,
            val name: String,
            val fields: List<Fields>
        ) {
            data class Fields(
                val name: String,
                val value: String
            )
        }
    }
}



