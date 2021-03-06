package ceui.lisa.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import ceui.lisa.activities.Shaft;
import ceui.lisa.adapters.BaseAdapter;
import ceui.lisa.adapters.IAdapter;
import ceui.lisa.core.BaseRepo;
import ceui.lisa.core.FilterMapper;
import ceui.lisa.core.RemoteRepo;
import ceui.lisa.databinding.FragmentBaseListBinding;
import ceui.lisa.http.Retro;
import ceui.lisa.model.ListIllust;
import ceui.lisa.models.IllustsBean;
import ceui.lisa.utils.Common;
import ceui.lisa.utils.Params;
import ceui.lisa.utils.PixivOperate;
import ceui.lisa.viewmodel.SearchModel;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class FragmentSearchIllust extends NetListFragment<FragmentBaseListBinding, ListIllust,
        IllustsBean> {

    private SearchModel searchModel;
    private boolean isPopular = false;

    public static FragmentSearchIllust newInstance(boolean popular) {
        Bundle args = new Bundle();
        args.putBoolean(Params.IS_POPULAR, popular);
        FragmentSearchIllust fragment = new FragmentSearchIllust();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        searchModel = new ViewModelProvider(requireActivity()).get(SearchModel.class);
        searchModel.getNowGo().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mRefreshLayout.autoRefresh();
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initBundle(Bundle bundle) {
        isPopular = bundle.getBoolean(Params.IS_POPULAR);
    }

    @Override
    public BaseAdapter<?, ? extends ViewDataBinding> adapter() {
        return new IAdapter(allItems, mContext);
    }

    @Override
    public BaseRepo repository() {
        return new RemoteRepo<ListIllust>() {
            @Override
            public Observable<ListIllust> initApi() {
                if (isPopular) {
                    return Retro.getAppApi().popularPreview(token(),
                            searchModel.getKeyword().getValue());
                } else {
                    Common.showLog(className + searchModel.getSortType().getValue());
                    PixivOperate.insertSearchHistory(searchModel.getKeyword().getValue(), 0);
                    return Retro.getAppApi().searchIllust(
                            token(),
                            searchModel.getKeyword().getValue() +
                                    (Shaft.sSettings.getSearchFilter().contains("无限制") ?
                                            "" : " " + (Shaft.sSettings.getSearchFilter())),
                            searchModel.getSortType().getValue(),
                            searchModel.getSearchType().getValue());
                }
            }

            @Override
            public Observable<ListIllust> initNextApi() {
                return Retro.getAppApi().getNextIllust(token(), mModel.getNextUrl());
            }

            @Override
            public Function<ListIllust, ListIllust> mapper() {
                return new FilterMapper();
            }
        };
    }

    @Override
    public boolean showToolbar() {
        return false;
    }

    @Override
    public void initRecyclerView() {
        staggerRecyclerView();
    }
}
