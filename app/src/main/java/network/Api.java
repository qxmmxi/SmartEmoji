package network;

import entity.EmoticonConfigs;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface Api {

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl);

    @GET("xxx/xxx/xxxx")
    Call<EmoticonConfigs> ExpressionConfig();
}
