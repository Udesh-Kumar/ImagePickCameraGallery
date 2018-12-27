package com.example.cc.imagepickthroughcam;

        import okhttp3.MultipartBody;
        import okhttp3.RequestBody;
        import okhttp3.ResponseBody;
        import retrofit2.Call;
        import retrofit2.Response;
        import retrofit2.http.Multipart;
        import retrofit2.http.POST;
        import retrofit2.http.Part;

public interface Api {
    @Multipart
    @POST("manageDoctorDocument")
    Call<ModelClass> postImage(@Part  MultipartBody.Part image,
                               @Part("documentImage")RequestBody doctor_id);
}
