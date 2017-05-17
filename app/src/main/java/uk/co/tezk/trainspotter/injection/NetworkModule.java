package uk.co.tezk.trainspotter.injection;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.co.tezk.trainspotter.network.ITrainSpottingRetrofit;

import static uk.co.tezk.trainspotter.model.Constant.TRAIN_SPOTTING_BASE_URL;

/**
 * Created by tezk on 11/05/17.
 */
@Module
public class NetworkModule {

    @Provides
    public HttpLoggingInterceptor provideInterceptor() {
        return new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC);
    }

    @Provides
    public OkHttpClient provideOkHttpclient(HttpLoggingInterceptor interceptor) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();
        return okHttpClient;
    }

    @Provides
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TRAIN_SPOTTING_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        return retrofit;
    }

    @Provides
    public ITrainSpottingRetrofit provideApi(Retrofit retrofit) {
        return retrofit.create(ITrainSpottingRetrofit.class);
    }
}