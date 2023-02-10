package ru.samsung.case2022.ui;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import ru.samsung.case2022.objects.RecResult;

public interface RecognitionService {

    @Multipart
    @POST("api/v1/beta/recognition/")
    @Headers({
            "enctype: multipart/form-data"
    })
    Call<RecResult> recognize(@Part MultipartBody.Part image);

}
