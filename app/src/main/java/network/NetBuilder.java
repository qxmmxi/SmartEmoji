package network;

import common.SysConstant;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class NetBuilder {
    Api api;
    String baseUrl = "";
    static NetBuilder netBuilder=null;
    static Object object = new Object();

    public  static NetBuilder getIntance(){
        if (netBuilder == null){
            synchronized (object){
                if (netBuilder == null){
                    netBuilder = new NetBuilder();
                }
            }
        }
        return netBuilder;
    }

    private NetBuilder(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SysConstant.URL.EXPRESSION_CONFIG_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(Api.class);
    }



    public Api getApi() {
        return api;
    }
}
