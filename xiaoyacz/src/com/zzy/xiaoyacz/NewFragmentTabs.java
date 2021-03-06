package com.zzy.xiaoyacz;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.zzy.xiaoyacz.ResultDialogFragment.QuizDataAdapter;
import com.zzy.xiaoyacz.util.FeiwoUtil;

public class NewFragmentTabs extends FragmentActivity implements QuizDataAdapter{
	private TabHost mTabHost;
	private ViewPager viewPager;
	private List<FragmentInfo> infos;
	// 退出时间
	private long currentBackPressedTime = 0;
	// 退出间隔
	private static final int BACK_PRESSED_INTERVAL = 2000;
	private static final String LIST_TAG="list";
	private static final String TYPE_TAG="type";
	private static final String AUTHOR_TAG="author";
	private static final String COLLECT_TAG="collect";
	private static final String TEST_TAG="test";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.fragment_tabs);
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        viewPager=(ViewPager) findViewById(R.id.viewpager);
        
        infos=new ArrayList<FragmentInfo>();
        infos.add(new FragmentInfo(LIST_TAG,"列表",ListAllFragment.class));
        infos.add(new FragmentInfo(TYPE_TAG,"分类",ByTypeFragment.class));
        infos.add(new FragmentInfo(AUTHOR_TAG,"诗人",ByAuthorFragment.class));
        infos.add(new FragmentInfo(COLLECT_TAG,"收藏",CollectFragment.class));
        infos.add(new FragmentInfo(TEST_TAG,"练习",TestFragment.class));
        for(FragmentInfo info:infos){
        	mTabHost.addTab(mTabHost.newTabSpec(info.tag).setIndicator(info.title).setContent(new EmptyTabFactory(this)));
        }
        viewPager.setAdapter(new ViewPagerAdapter(this));
        viewPager.setOffscreenPageLimit(infos.size()-1);//保留几个Fragment
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener(){

    		@Override
    		public void onTabChanged(String tabId) {
    			int ind=0;
    			int size=infos.size();
    			for(int i=0;i<size;i++){
    				FragmentInfo info=infos.get(i);
    				if(tabId.equals(info.tag)){
    					ind=i;
    					break;
    				}
    			}
    			viewPager.setCurrentItem(ind);
    			
    		}
    		
    	});
        viewPager.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}

			@Override
			public void onPageSelected(int arg0) {
				mTabHost.setCurrentTab(arg0);
			}
        	
        });
        getOverflowMenu();//TODO 这个要研究一下
        //Feiwo ad
        LinearLayout adlayout =(LinearLayout)findViewById(R.id.AdLinearLayout);
        FeiwoUtil.addFeiwoAd(adlayout,this);
    }

    private void getOverflowMenu() {

        try {
           ViewConfiguration config = ViewConfiguration.get(this);
           Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
           if(menuKeyField != null) {
               menuKeyField.setAccessible(true);
               menuKeyField.setBoolean(config, false);
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.menu_settings){
			Intent i=new Intent(this,SettingsActivity.class);
			startActivity(i);
		}else if(item.getItemId()==R.id.menu_about){
			AboutDialog newFragment = new AboutDialog();
		    newFragment.show(getSupportFragmentManager(), "dialog");
		}
		return true;
	}
	@Override
	public void onBackPressed() {
		// 判断时间间隔
		if (System.currentTimeMillis() - currentBackPressedTime > BACK_PRESSED_INTERVAL) {
			currentBackPressedTime = System.currentTimeMillis();
			Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
		} else {
			// 退出
			finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private class ViewPagerAdapter extends FragmentPagerAdapter{
		FragmentActivity mActivity;
		
		public ViewPagerAdapter(FragmentActivity mActivity) {
			super(mActivity.getSupportFragmentManager());
			this.mActivity=mActivity;
		}

		@Override
		public Fragment getItem(int index) {
			FragmentInfo info=infos.get(index);
			if(info.frag==null){
				info.frag=Fragment.instantiate(mActivity, info.cla.getName());
			}
			return info.frag;
		}

		@Override
		public int getCount() {
			return infos.size();
		}
		
	}
	private class FragmentInfo{
		public FragmentInfo(String tag,String title,Class<?> cla){
			this.tag=tag;
			this.title=title;
			this.cla=cla;
		}
		String tag;
		Class<?> cla;
		String title;
		Fragment frag;
	}
	private class EmptyTabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public EmptyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }
	//重新加载练习试题
	@Override
	public void reloadQuiz() {
		for(FragmentInfo info:infos){
			if(info.tag.equals(TEST_TAG)){//练习的Fragment
				TestFragment frag=(TestFragment) info.frag;
				frag.refreshQuestion();
				break;
			}
		}
		
	}
}
