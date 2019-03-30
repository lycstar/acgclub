package com.luyucheng.acgclub.minterface;


import com.luyucheng.acgclub.bean.AcgBean;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AcgApi {
    /*@baseurl:https://www.rabtman.com
     *@category:分类
     * @offset:页数
     * */
    @GET("api/v2/acgclub/category/{category}/pictures")
    Call<AcgBean> ACG_BEAN_CALL(@Path("category") String category, @Query("offset") int offset);
}
