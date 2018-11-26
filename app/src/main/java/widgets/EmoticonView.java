package widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.smartemoji.R;
import java.util.ArrayList;
import java.util.List;
import entity.EmoticonEntity;
import entity.EmoticonEntityItem;
import utils.ScreenUtil;

/**
 * created by shonary on 18/10/23
 * email： xiaonaxi.mail@gmail.com
 */
public class EmoticonView extends RelativeLayout implements OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String BACKSPACE = "backspace";
    private static final String BACKSPACE_ICON = "emoji/default_emo_back.png";

    private static final int EMOJI_COUNT_IN_ONE_PAGE = 20;
    private static final int EMOJI_ROW_COUNT_IN_ONE_PAGE = 7;
    private static final int GIF_COUNT_IN_ONE_PAGE = 8;
    private static final int GIF_ROW_COUNT_IN_ONE_PAGE = 4;
    private static final int IMAGE_COUNT_IN_ONE_PAGE = 8;
    private static final int IMAGE_ROW_COUNT_IN_ONE_PAGE = 4;

    private List<EmoticonEntity> mEmojis = null;
    private List<Integer> mPages = new ArrayList<>();
    private List<Integer> mPageRecorder = new ArrayList<>();

    private EventListener mListener;

    private ViewPager mPagerView;
    private LinearLayout mDotView;
    private PagerSlidingTabStripView mTabView;

    public EmoticonView(Context context) {
        super(context);
        initViewData();
    }

    public EmoticonView(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);

        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.widgets_emoticon_layout, this, true);
        mPagerView = rootView.findViewById(R.id.emoji_viewPager);
        mDotView = rootView.findViewById(R.id.emoji_linearLayout);
        mTabView = rootView.findViewById(R.id.emoji_tabs);

        initViewData();
    }

    public void initViewData() {
        mEmojis = EmoticonHelper.getInstance().getEmojis();
        if (mEmojis == null || mEmojis.size() == 0) {
            return;
        }
        initGridViews(mEmojis);
        setDefaultTabBar();
    }

    public void setDefaultTabBar(){
        if (mEmojis.size()>1){
            mTabView.setVisibility(View.VISIBLE);
        }else{
            mTabView.setVisibility(View.GONE);
            LayoutParams params = (LayoutParams) mDotView.getLayoutParams();
            params.setMargins(params.leftMargin, -ScreenUtil.dp2px(getContext(),30), params.rightMargin, 25);
            mDotView.setLayoutParams(params);
        }
    }

    private synchronized void initGridViews(List<EmoticonEntity> emojiList) {
        if (emojiList == null || emojiList.size() == 0) {
            return;
        }

        List<GridView> gridViews = new ArrayList<>();
        for (EmoticonEntity emoji : emojiList) {
            List<List<EmoticonEntityItem>> gridViewItems = getGridViewItems(emoji);
            for (List<EmoticonEntityItem> listItem : gridViewItems) {
                GridView gridView = new GridView(getContext());
                gridView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));

                EmoticonEntityItem emojiItem = listItem.get(0);
                if (emojiItem.type == EmoticonParserHelper.EMOJI_UNICODE) {
                    gridView.setNumColumns(EMOJI_ROW_COUNT_IN_ONE_PAGE);
                    gridView.setVerticalSpacing(ScreenUtil.dp2px(getContext(),5));
                    gridView.setHorizontalSpacing(ScreenUtil.dp2px(getContext(),15));
                    gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                    gridView.setPadding(20, 20, 20, 20);

                } else if (emojiItem.type == EmoticonParserHelper.EMOJI_IMAGE) {
                    gridView.setNumColumns(IMAGE_ROW_COUNT_IN_ONE_PAGE);
                    gridView.setPadding(8, 8, 8, 0);
                    gridView.setVerticalSpacing(20);
                    gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

                } else if (emojiItem.type == EmoticonParserHelper.EMOJI_GIF) {
                    gridView.setNumColumns(GIF_ROW_COUNT_IN_ONE_PAGE);
                    gridView.setPadding(8, 8, 8, 0);
                    gridView.setVerticalSpacing(20);
                    gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
                }

                gridView.setVerticalScrollBarEnabled(false);
                gridView.setHorizontalScrollBarEnabled(false);

                EmojiGridAdapter adapter = new EmojiGridAdapter(getContext(), gridView, listItem);
                gridView.setAdapter(adapter);

                gridView.setSelector(R.drawable.emoticon_item_selector);
                gridView.setOnItemClickListener(this);
                gridView.setOnItemLongClickListener(this);

                gridViews.add(gridView);
            }
        }

        EmojiPagerAdapter pagerAdapter = new EmojiPagerAdapter(getContext(), gridViews);
        mPagerView.setAdapter(pagerAdapter);
        mPagerView.setCurrentItem(0);

        moveToDot(0, 0);

        PageChangeListener pageChangeListener = new PageChangeListener();
        mTabView.setOnPageChangeListener(pageChangeListener);
        mTabView.setViewPager(mPagerView);
        mTabView.setPageRecorder(mPageRecorder);
    }

    private List<List<EmoticonEntityItem>> getGridViewItems(EmoticonEntity emoji) {
        List<List<EmoticonEntityItem>> lists = null;
        if (emoji.type == EmoticonParserHelper.EMOJI_GIF) {
            lists = getEmojiItemList(emoji, GIF_COUNT_IN_ONE_PAGE, false);
        }

        if (emoji.type == EmoticonParserHelper.EMOJI_IMAGE) {
            lists = getEmojiItemList(emoji, IMAGE_COUNT_IN_ONE_PAGE, false);
        }

        if (emoji.type == EmoticonParserHelper.EMOJI_UNICODE) {
            lists = getEmojiItemList(emoji, EMOJI_COUNT_IN_ONE_PAGE, true);
        }

        return lists;
    }

    private List<List<EmoticonEntityItem>> getEmojiItemList(EmoticonEntity emoji, int pageCount, boolean isDefaultEmoji) {
        List<List<EmoticonEntityItem>> lists = new ArrayList<>();

        int page = (int) Math.ceil(emoji.items.size() * 1.0 / pageCount);
        mPages.add(page);

        if (page == 0) {
            if (isDefaultEmoji) {
                EmoticonEntityItem emojiItem = new EmoticonEntityItem();
                emojiItem.connName = BACKSPACE;
                emojiItem.fileName = BACKSPACE_ICON;
                emoji.items.add(emojiItem);
            }
            lists.add(emoji.items);
            return lists;
        }

        for (int i = 0, num = 0; i < page; i++) {
            List<EmoticonEntityItem> items = new ArrayList<>();
            for (int j = 0; j < pageCount; j++, num++) {
                if (num == emoji.items.size()) {
                    break;
                }
                items.add(emoji.items.get(num));
            }
            if (isDefaultEmoji) {
                EmoticonEntityItem emojiItem = new EmoticonEntityItem();
                emojiItem.connName = BACKSPACE;
                emojiItem.fileName = BACKSPACE_ICON;
                items.add(emojiItem);
            }
            lists.add(items);
        }

        return lists;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        EmoticonEntityItem selected = (EmoticonEntityItem) view.getTag(view.getId());
        if (mListener == null || selected == null) {
            return;
        }
        if (selected.type == EmoticonParserHelper.EMOJI_GIF) {
            mListener.onGifSelected(selected);
            return;
        }
        if (selected.type == EmoticonParserHelper.EMOJI_IMAGE) {
            mListener.onImageSelected(selected);
            return;
        }
        if (TextUtils.equals(selected.connName, BACKSPACE)) {
            mListener.onBackspace();
            return;
        }
        if (selected.type == EmoticonParserHelper.EMOJI_UNICODE) {
            mListener.onEmojiSelected(selected.connName);
            return;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    private void moveToDot(int tabIndex, int pageIndex) {
        mDotView.removeAllViews();
        if (mPages.size() == 0) {
            return;
        }
        int pageSize = mPages.get(tabIndex);
        if (pageSize <= 1) {
            return;
        }
        for (int i = 0; i < pageSize; i++) {
            ImageView image = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
            params.setMargins(ScreenUtil.dp2px(getContext(),10), 0, ScreenUtil.dp2px(getContext(),10), 0);
            image.setBackgroundResource(R.drawable.emoticon_page_dot);

            if (pageIndex == i) {
                image.setEnabled(false);
            } else {
                image.setEnabled(true);
            }

            mDotView.addView(image, params);
        }
    }

    public void recycleEmoji() {
        if (mPagerView != null) {
            mPagerView.removeAllViews();
        }
    }

    public void setEventListener(EventListener listener) {
        this.mListener = listener;
    }

    public interface EventListener {

        void onBackspace();

        void onEmojiSelected(String emojiName);

        void onGifSelected(EmoticonEntityItem item);

        void onImageSelected(EmoticonEntityItem item);
    }

    class EmojiGridAdapter extends BaseAdapter {
        private Context mContext = null;
        private List<EmoticonEntityItem> mItems = null;
        private LayoutInflater mInflater = null;
        private GridView mGridView = null;

        public EmojiGridAdapter(Context context, GridView gridView, List<EmoticonEntityItem> items) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mGridView = gridView;
            mItems = items;
        }

        public void setEmoji(List<EmoticonEntityItem> emojis) {
            mItems = emojis;
        }

        @Override
        public int getCount() {
            return mItems != null ? mItems.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mItems != null ? mItems.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_emoji, null);
                viewHolder = new ViewHolder();
                viewHolder.mEmojiImage = convertView.findViewById(R.id.emoji_gird_item_image);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            String iconPath = mItems.get(position).fileName;
            Drawable drawable = EmoticonParserHelper.getInstance().getDrawable(mContext, iconPath);
            viewHolder.mEmojiImage.setImageDrawable(drawable);

            viewHolder.mEmojiImage.setTag(viewHolder.mEmojiImage.getId(), mItems.get(position));

            int height = 0;
            if (mItems.get(position).type == EmoticonParserHelper.EMOJI_GIF ||
                    mItems.get(position).type == EmoticonParserHelper.EMOJI_IMAGE) {
                height = mGridView.getHeight() / 2 - 25;
            } else {
                height = mGridView.getHeight() / 4;
            }

            AbsListView.LayoutParams param = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, height);

            convertView.setLayoutParams(param);
            convertView.setPadding(8, 2, 8, 2);
            return convertView;
        }
    }

    static class ViewHolder {
        public ImageView mEmojiImage;
    }

    class EmojiPagerAdapter extends PagerAdapter implements PagerSlidingTabStripView.IconTabProvider {
        private Context mContext = null;
        private List<GridView> mGridViews = null;

        private EmojiPagerAdapter(Context context, List<GridView> gridViews) {
            mContext = context;
            mGridViews = gridViews;
        }

        @Override
        public int getCount() {
            return mGridViews != null ? mGridViews.size() : 0;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View gridView;
            gridView = mGridViews.get(position);
            container.addView(gridView);
            return gridView;
        }

        @Override
        public boolean isViewFromObject(View paramView, Object paramObject) {
            return paramView == paramObject;
        }

        @Override
        public void destroyItem(ViewGroup paramViewGroup, int paramInt, Object paramObject) {
            View localObject;
            localObject = mGridViews.get(paramInt);
            paramViewGroup.removeView(localObject);
        }

        @Override
        public Drawable getPageIcon(int position) {
            try {
                String iconPath = mEmojis.get(position).icon;
                return EmoticonParserHelper.getInstance().getDrawable(mContext, iconPath);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public int getPageCount() {
            return mEmojis == null ? 0 : mEmojis.size();
        }
    }

    class PageChangeListener implements ViewPager.OnPageChangeListener {
        private List<Integer> startPageIndexForEveryType = new ArrayList<>();
        private int lastPosition = 0;


        public PageChangeListener() {
            startPageIndexForEveryType.add(0);
            for (int i = 0, length = mPages.size() - 1; i < length; i++) {
                startPageIndexForEveryType.add(mPages.get(i) + startPageIndexForEveryType.get(i));
            }

            mPageRecorder.addAll(startPageIndexForEveryType);
        }


        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0, length = startPageIndexForEveryType.size(); i < length; i++) {
                if (position > startPageIndexForEveryType.get(length - 1)) {
                    moveToDot(length - 1, position - startPageIndexForEveryType.get(length - 1));
                    break;
                }

                if (i == 0 && position == 0) {
                    moveToDot(0, 0);
                    break;
                }

                if (position == startPageIndexForEveryType.get(i)) {
                    moveToDot(i, position - startPageIndexForEveryType.get(i));
                    break;
                }

                if (position == startPageIndexForEveryType.get(i) - 1 &&
                        lastPosition == startPageIndexForEveryType.get(i)) {
                    moveToDot(i - 1, position - startPageIndexForEveryType.get(i - 1));
                    break;
                }

                if (position > startPageIndexForEveryType.get(i) &&
                        position < startPageIndexForEveryType.get(i + 1)) {
                    if (lastPosition != startPageIndexForEveryType.get(i + 1)) {
                        moveToDot(i, position - startPageIndexForEveryType.get(i));
                        break;
                    }
                }
            }

            lastPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
