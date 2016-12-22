package cn.ucai.androidannotationsdemo.controller.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.androidannotationsdemo.R;

public class BoutiqueChildActivity extends BaseActivity {

    @BindView(R.id.tv_common_title)
    TextView mTvCommonTitle;
    @BindView(R.id.tv_refresh)
    TextView mTvRefresh;
    @BindView(R.id.rv)
    RecyclerView mRv;
    @BindView(R.id.srl)
    SwipeRefreshLayout mSrl;

    BoutiqueChildActivity mContext;
    cn.ucai.androidannotationsdemo.controller.adapter.GoodsAdapter mAdapter;
    ArrayList<cn.ucai.androidannotationsdemo.bean.NewGoodsBean> mList;
    int pageId = 1;
    GridLayoutManager glm;
    cn.ucai.androidannotationsdemo.bean.BoutiqueBean boutique;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_boutique_child);
        ButterKnife.bind(this);
        boutique = (cn.ucai.androidannotationsdemo.bean.BoutiqueBean) getIntent().getSerializableExtra(cn.ucai.androidannotationsdemo.I.Boutique.CAT_ID);
        if(boutique == null){
            finish();
        }
        mContext = this;
        mList = new ArrayList<>();
        mAdapter = new cn.ucai.androidannotationsdemo.controller.adapter.GoodsAdapter(mContext,mList);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        mSrl.setColorSchemeColors(
                getResources().getColor(R.color.google_blue),
                getResources().getColor(R.color.google_green),
                getResources().getColor(R.color.google_red),
                getResources().getColor(R.color.google_yellow)
        );
        glm = new GridLayoutManager(mContext, cn.ucai.androidannotationsdemo.I.COLUM_NUM);
        mRv.setLayoutManager(glm);
        mRv.setHasFixedSize(true);
        mRv.setAdapter(mAdapter);
        mRv.addItemDecoration(new cn.ucai.androidannotationsdemo.view.SpaceItemDecoration(12));
        mTvCommonTitle.setText(boutique.getTitle());
    }


    @Override
    protected void setListener() {
        setPullUpListener();
        setPullDownListener();
    }

    private void setPullDownListener() {
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSrl.setRefreshing(true);
                mTvRefresh.setVisibility(View.VISIBLE);
                pageId = 1;
                downloadNewGoods(cn.ucai.androidannotationsdemo.I.ACTION_PULL_DOWN);
            }
        });
    }

    private void downloadNewGoods(final int action) {
        cn.ucai.androidannotationsdemo.model.net.NetDao.downloadNewGoods(mContext,boutique.getId(), pageId, new cn.ucai.androidannotationsdemo.model.utils.OkHttpUtils.OnCompleteListener<cn.ucai.androidannotationsdemo.bean.NewGoodsBean[]>() {
            @Override
            public void onSuccess(cn.ucai.androidannotationsdemo.bean.NewGoodsBean[] result) {
                mSrl.setRefreshing(false);
                mTvRefresh.setVisibility(View.GONE);
                mAdapter.setMore(true);
                cn.ucai.androidannotationsdemo.model.utils.L.e("result="+result);
                if(result!=null && result.length>0){
                    ArrayList<cn.ucai.androidannotationsdemo.bean.NewGoodsBean> list = cn.ucai.androidannotationsdemo.model.utils.ConvertUtils.array2List(result);
                    if(action== cn.ucai.androidannotationsdemo.I.ACTION_DOWNLOAD || action == cn.ucai.androidannotationsdemo.I.ACTION_PULL_DOWN) {
                        mAdapter.initData(list);
                    }else{
                        mAdapter.addData(list);
                    }
                    if(list.size()< cn.ucai.androidannotationsdemo.I.PAGE_SIZE_DEFAULT){
                        mAdapter.setMore(false);
                    }
                }else{
                    mAdapter.setMore(false);
                }
            }

            @Override
            public void onError(String error) {
                mSrl.setRefreshing(false);
                mTvRefresh.setVisibility(View.GONE);
                mAdapter.setMore(false);
                cn.ucai.androidannotationsdemo.model.utils.CommonUtils.showShortToast(error);
                cn.ucai.androidannotationsdemo.model.utils.L.e("error:"+error);
            }
        });
    }

    private void setPullUpListener() {
        mRv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastPosition = glm.findLastVisibleItemPosition();
                if(newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastPosition == mAdapter.getItemCount()-1
                        && mAdapter.isMore()){
                    pageId++;
                    downloadNewGoods(cn.ucai.androidannotationsdemo.I.ACTION_PULL_UP);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstPosition = glm.findFirstVisibleItemPosition();
                mSrl.setEnabled(firstPosition==0);
            }
        });
    }

    @Override
    protected void initData() {
        downloadNewGoods(cn.ucai.androidannotationsdemo.I.ACTION_DOWNLOAD);
    }

    @OnClick(R.id.backClickArea)
    public void onClick() {
        cn.ucai.androidannotationsdemo.model.utils.MFGT.finish(this);
    }
}
