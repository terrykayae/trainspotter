package uk.co.tezk.trainspotter.network;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import uk.co.tezk.trainspotter.model.ApiMessage;
import uk.co.tezk.trainspotter.model.ClassNumbers;
import uk.co.tezk.trainspotter.model.TrainDetail;
import uk.co.tezk.trainspotter.model.TrainListItem;

import static uk.co.tezk.trainspotter.model.Constant.CLASS_LIST_API;
import static uk.co.tezk.trainspotter.model.Constant.TRAIN_DETAIL_API;
import static uk.co.tezk.trainspotter.model.Constant.TRAIN_LIST_API;
import static uk.co.tezk.trainspotter.model.Constant.TRAIN_SIGHTING_API;
import static uk.co.tezk.trainspotter.model.Constant.TRAIN_SPOTTING_BASE_URL;


/**
 * Interface for retrofit api
 */

public interface ITrainSpottingRetrofit {
    @GET(TRAIN_SPOTTING_BASE_URL+CLASS_LIST_API)
    public Observable<ClassNumbers> getClassNumbers() ;

    @GET(TRAIN_SPOTTING_BASE_URL+TRAIN_LIST_API)
    public Observable<List<TrainListItem>> getTrains(@Path("classId") String classNumber) ;

    @GET(TRAIN_SPOTTING_BASE_URL+TRAIN_DETAIL_API)
    public Observable<TrainDetail> getTrainDetails(@Path("classId") String classNumber, @Path("trainId") String trainId) ;

    @POST(TRAIN_SPOTTING_BASE_URL+TRAIN_SIGHTING_API)
    public Observable<ApiMessage> addTrainSighting(
            @Path("classId") String classNumber,
            @Path("trainId") String trainId,
            @Query("date") String date,
            @Query("lat") float lat,
            @Query("lon") float lon,
            @Query("api_key") String apiKey
    );
}
