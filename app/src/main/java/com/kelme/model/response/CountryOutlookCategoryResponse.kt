package com.kelme.model.response

import com.google.gson.annotations.SerializedName


/**
 * Created by Gaurav Kumar on 15/07/21.
 */
data class CountryOutlookCategoryResponse(@SerializedName("status") var status : Boolean,
                                          @SerializedName("message") var message : String,
                                          @SerializedName("data") var data : List<CountryOutlookCategoryData>,
                                          @SerializedName("code") var code : Int)

data class CountryOutlookCategoryData(@SerializedName("category_id") var categoryId : String,
                                      @SerializedName("category_title") var categoryTitle : String)