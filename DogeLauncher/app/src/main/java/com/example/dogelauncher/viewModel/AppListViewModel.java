package com.example.dogelauncher.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dogelauncher.model.AppData;
import com.example.dogelauncher.utils.AppDataUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppListViewModel extends ViewModel {

    private static final String TAG = "AppListViewModel";

    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<AppData>> mPkgList = new MutableLiveData<>(AppDataUtil.getInstalledApps());

    private final Runnable mRunnable = () -> {
        final List<AppData> list = AppDataUtil.getAppDataByPMAndSave();
        Log.e(TAG, "$mRunnable: list.size: "+list.size() );
        mPkgList.postValue(list);
    };

    public AppListViewModel() {
        super();
    }

    public LiveData<List<AppData>> getPackageList() {
        return mPkgList;
    }

    public List<AppData> getData(){
        return mPkgList.getValue();
    }

    public void loadPackageList() {
        mExecutor.execute(mRunnable);
    }

    public void removeItem(int pos) {
        Log.e(TAG, "removeItem: " );
        List<AppData> list = mPkgList.getValue();
        if(!list.isEmpty() && list.size()>pos){
            list.remove(pos);
        }
    }

    public void changePositions(int partId, int fromPosition, int toPosition){
        Log.e(TAG, "changePositions: " );
        List<AppData> dataList = getData();
        fromPosition = getInnerIdx(partId,fromPosition);
        toPosition = getInnerIdx(partId,toPosition);
        AppData appData = dataList.get(fromPosition);
        if (fromPosition > toPosition) {
            for (int i = fromPosition; i > toPosition; i--) {
                dataList.set(i, dataList.get(i - 1));
            }
            dataList.set(toPosition, appData);
        } else {
            for (int i = fromPosition; i < toPosition; i++) {
                dataList.set(i, dataList.get(i + 1));
            }
            dataList.set(toPosition, appData);
        }

        Log.e(TAG, "changePositions: " );
    }

    public List<AppData> changePositions(int fromPosition, int toPosition){
        Log.e(TAG, "changePositions: " );
        if (fromPosition == toPosition)
            return null;
//        List<AppData> list = mPkgList.getValue();
//        AppData appData = list.get(fromPosition);
//        if (fromPosition > toPosition) {
//            for (int i = fromPosition; i > toPosition; i--) {
//                list.set(i, list.get(i - 1));
//            }
//            list.set(toPosition, appData);
//        } else {
//            for (int i = fromPosition; i < toPosition; i++) {
//                list.set(i, list.get(i + 1));
//            }
//            list.set(toPosition, appData);
//        }

        List<AppData> dataList = new ArrayList<>(mPkgList.getValue());
        AppData appData = dataList.get(fromPosition);
        if (fromPosition > toPosition) {
            for (int i = fromPosition; i > toPosition; i--) {
                dataList.set(i, dataList.get(i - 1));
            }
            dataList.set(toPosition, appData);
        } else {
            for (int i = fromPosition; i < toPosition; i++) {
                dataList.set(i, dataList.get(i + 1));
            }
            dataList.set(toPosition, appData);
        }
        return dataList;
//        mPkgList.setValue(dataList);
    }

    public AppData getSlicePartItem(int partId, int idx){

        return getData().get(getInnerIdx(partId,idx));
    }

    public int getSlicePartSize(int partId){
        return partSizes[partId];
    }

    public void remove (int partId, int idx){
        getData().remove(getInnerIdx(partId,idx));
    }

    int mPageNum =1;
    int[] partSizes = null;

    private int getInnerIdx(int partId, int idx){
        int size = getData().size();
        int perSize = size/mPageNum;
        int inerIdx = 0;
        for (int i = 0; i < partId; i++) {
            inerIdx += perSize;
        }
        inerIdx += idx;
        return inerIdx;
    }

    public void sliceTo(int pageNum) {
        mPageNum = pageNum;
        int size = getData().size();
        int perSize = size/mPageNum;
        partSizes = new int[pageNum];

        int sum = 0;
        for (int i = 0; i < pageNum; i++) {
            if(sum + perSize < size) {
                sum += perSize;
                partSizes[i] = perSize;
            } else {
                partSizes[i] = size - sum;
            }
        }
        Log.e(TAG, "sliceTo: " );
    }

    //更新数据一定要refreshSlice
    public void refreshSlice(List<AppData> data, int pageNum){
        mPageNum = pageNum;
        int size = data.size();
        int perSize = size/mPageNum;
        partSizes = new int[pageNum];

        int lastSize = 0;
        for (int i = 0; i < pageNum; i++) {
            int curSize = (i+1)*perSize;
            if(curSize > size) curSize = size;
            lastSize = curSize - lastSize;
            partSizes[i] = lastSize;
        }
    }

}
