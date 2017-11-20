package google.architecture.coremodel.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

import google.architecture.coremodel.datamodel.http.ApiClient;
import google.architecture.coremodel.datamodel.http.ApiConstants;
import google.architecture.coremodel.datamodel.http.entities.GirlsData;
import google.architecture.coremodel.datamodel.http.service.DynamicApiService;
import google.architecture.coremodel.datamodel.http.service.GankDataService;
import google.architecture.coremodel.util.JsonUtil;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by dxx on 2017/11/20.
 */

public class BaseViewModel<T> extends AndroidViewModel {

    //生命周期观察的数据
    private MutableLiveData<T>  liveObservableData = new MutableLiveData<>();
    //UI使用可观察的数据 ObservableField是一个包装类
    public ObservableField<T> uiObservableData = new ObservableField<>();

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private static final MutableLiveData ABSENT = new MutableLiveData();
    {
        //noinspection unchecked
        ABSENT.setValue(null);
    }


    public BaseViewModel(@NonNull Application application, String fullUrl) {
        super(application);
        ApiClient.initService(ApiConstants.fuliHost, DynamicApiService.class).getDynamicData(fullUrl).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(ResponseBody value) {
               if(null != value){
                   try {
                       liveObservableData.setValue(JsonUtil.Str2JsonBean(value.string(), getTClass()));
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * LiveData支持了lifecycle生命周期检测
     * @return
     */
    public LiveData<T> getLiveObservableData() {
        return liveObservableData;
    }

    /**
     * 当主动改变数据时重新设置被观察的数据
     * @param product
     */
    public void setUiObservableData(T product) {
        this.uiObservableData.set(product);
    }

    public Class<T> getTClass(){
        Class<T> tClass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return tClass;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mDisposable.clear();
    }
}