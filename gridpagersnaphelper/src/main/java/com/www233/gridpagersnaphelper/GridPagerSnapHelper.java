package com.www233.gridpagersnaphelper;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.Objects;

public class GridPagerSnapHelper extends SnapHelper {
    private static final String TAG = "GridPageSnapHelper";
    private int currentPositionVertical = 0, currentPositionHorizontal = 0; // 记录当前页的首个index
    private int row, pageLimit;  // row限制行数/列数, pageLimit每页最大数量
    private int all_item = 0;   // 控件总数
    private OrientationHelper mHorizontalHelper, mVerticalHelper;
    RecyclerView mRecyclerView;
    int decorationIndex = -1;
    static final float MILLISECONDS_PER_INCH = 100f;
    private static final int MAX_SCROLL_ON_FLING_DURATION = 100; // ms
    private OnPageChangeListener onPageChangeListener = (pageBeforeChange, pageAfterChange) -> {
    };

    private static class Direction {
        static final int HORIZONTAL = 0, VERTICAL = 1;
    }

    public interface OnPageChangeListener {
        void onChange(int pageBeforeChange, int pageAfterChange);
    }

    /**
     * 初始化
     *
     * @param row        不可移动的方向上有多少列/行
     * @param pageLimit 每一页最多有多少控件
     */
    public GridPagerSnapHelper(int row, int pageLimit) {
        this.row = row;
        this.pageLimit = pageLimit;
    }
    /**
     * 如果之后要把recyclerView连到其他的snapHelper，则需要先调用之前snapHelper的detachToRecyclerView方法。
     * 调用attachToRecyclerView时会为recyclerView的最后一行/列设置装饰器以填充剩余空间，
     * 因此需要用detachToRecyclerView去掉此装饰器。
     * @param recyclerView The RecyclerView instance to which you want to add this helper or
     *                     {@code null} if you want to remove SnapHelper from the current
     *                     RecyclerView.
     *
     * @throws IllegalStateException
     * @see #detachToRecyclerView()
     */
    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        super.attachToRecyclerView(recyclerView);
        if (recyclerView != null) {
            all_item = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
            mRecyclerView = recyclerView;
            recyclerView.addItemDecoration(new ButtonPageScrollDecoration());
            decorationIndex = recyclerView.getItemDecorationCount() - 1;
        }
    }
    public void detachToRecyclerView(){
        if(mRecyclerView != null)
        {
            mRecyclerView.removeItemDecorationAt(decorationIndex);
            mRecyclerView.setOnFlingListener(null);
            mRecyclerView.clearOnScrollListeners();
            mRecyclerView = null;
        }
    }

    
    @Override
    protected void finalize() throws Throwable {
        detachToRecyclerView();
        super.finalize();
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setPageLimit(int pageLimit) {
        this.pageLimit = pageLimit;
    }

    @NonNull
    private OrientationHelper getVerticalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null || mVerticalHelper.getLayoutManager() != layoutManager) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    @NonNull
    private OrientationHelper getHorizontalHelper(
            @NonNull RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null || mHorizontalHelper.getLayoutManager() != layoutManager) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }


    public void setOnPageChangeListener(@NonNull OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    private int distanceToNextPage(@NonNull RecyclerView.LayoutManager layoutManager, OrientationHelper helper, @NonNull View targetView, int direction) {
        int targetPosition = layoutManager.getPosition(targetView);
        int currentPosition = (direction == Direction.HORIZONTAL ? currentPositionHorizontal : currentPositionVertical);
        int currentPageStart = currentPosition;
        int result;
        if (Math.abs(targetPosition - currentPosition) >= pageLimit) {
            // 目标view已加载出来，不用再计算移动距离而是可以直接获取: 滑动时会调用findTargetSnapPosition()直接得到targetView
            result = helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
            currentPageStart = targetPosition;
        } else {    // 移动半块及以上时会滑动页面: 拖动时只会调用findSnapView()从而获取到距离当前页面最左边最近的view
            int dis = Math.abs(helper.getDecoratedStart(targetView) - helper.getStartAfterPadding());

            if (targetPosition < currentPosition - row
                    || dis <= (helper.getDecoratedMeasurement(targetView) / 2) && targetPosition < currentPosition) {
                // 左移且移动半块以上
                currentPageStart = currentPosition - pageLimit;

            } else if (targetPosition >= currentPosition + row
                    || dis >= (helper.getDecoratedMeasurement(targetView) / 2) && targetPosition >= currentPosition) {
                // 右移且移动半块以上
                currentPageStart = currentPosition + pageLimit;
            }

            int columnWidth = helper.getDecoratedMeasurement(targetView);
            int distance = ((targetPosition - currentPageStart) / row) * columnWidth;
            final int childStart = helper.getDecoratedStart(targetView);

            result = childStart - distance;
        }

        if (direction == Direction.HORIZONTAL && currentPageStart != currentPositionHorizontal) {
            onPageChangeListener.onChange(currentPositionHorizontal / pageLimit,
                    currentPageStart / pageLimit);
            currentPositionHorizontal = currentPageStart;
        } else if (direction == Direction.VERTICAL && currentPageStart != currentPositionVertical) {
            onPageChangeListener.onChange(currentPositionVertical / pageLimit,
                    currentPageStart / pageLimit);
            currentPositionVertical = currentPageStart;
        }

        return result;
    }

    /**
     * 计算到目标页需要移动的距离
     *
     * @param layoutManager the {@link RecyclerView.LayoutManager} associated with the attached
     *                      {@link RecyclerView}
     * @param targetView    the target view that is chosen as the view to snap
     * @return x/y上的距离
     */
    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToNextPage(layoutManager, getHorizontalHelper(layoutManager), targetView, Direction.HORIZONTAL);
        }
        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToNextPage(layoutManager, getVerticalHelper(layoutManager), targetView, Direction.VERTICAL);
        }
        return out;
    }

    private View findNextPage(RecyclerView.LayoutManager layoutManager, OrientationHelper helper) {

        final int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }
        View closestChild = null;
        final int start = helper.getStartAfterPadding();
        int closest = Integer.MAX_VALUE;

        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            if (child == null) continue;

            int childStart = helper.getDecoratedStart(child)
                    + (helper.getDecoratedMeasurement(child));
            int distance = Math.abs(childStart - start);

            if (distance < closest) {
                closest = distance;
                closestChild = child;
            }

        }
        return closestChild;
    }

    /**
     * 寻找用于移动的基准view(当前页距离最左边最近的view)
     *
     * @param layoutManager the {@link RecyclerView.LayoutManager} associated with the attached
     *                      {@link RecyclerView}
     * @return view
     */
    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return findNextPage(layoutManager, getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            return findNextPage(layoutManager, getHorizontalHelper(layoutManager));
        }
        return null;
    }

    private int findNextPagePosition(int velocity, int currentPosition) {
        if (velocity > 0)   // 向右滑动
        {
            if (currentPosition + pageLimit > all_item)
                return currentPosition;
            else
                return currentPosition + pageLimit;

        } else if (velocity < 0) // 向左滑动
        {
            if (currentPosition - pageLimit < 0)
                return currentPosition;
            else
                return currentPosition - pageLimit;
        }
        return RecyclerView.NO_POSITION;
    }

    @Nullable
    private OrientationHelper getOrientationHelper(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return getVerticalHelper(layoutManager);
        } else if (layoutManager.canScrollHorizontally()) {
            return getHorizontalHelper(layoutManager);
        } else {
            return null;
        }
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {

        final int itemCount = layoutManager.getItemCount();
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION;
        }

        final OrientationHelper orientationHelper = getOrientationHelper(layoutManager);
        if (orientationHelper == null) {
            return RecyclerView.NO_POSITION;
        }

        if (layoutManager.canScrollVertically()) {
            return findNextPagePosition(velocityY, currentPositionVertical);
        } else if (layoutManager.canScrollHorizontally()) {
            return findNextPagePosition(velocityX, currentPositionHorizontal);
        } else {
            return RecyclerView.NO_POSITION;
        }

    }

    @Nullable
    @Override
    protected RecyclerView.SmoothScroller createScroller(RecyclerView.LayoutManager layoutManager) {

        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return null;
        }
        return new LinearSmoothScroller(mRecyclerView.getContext()) {

            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                int[] snapDistances = calculateDistanceToFinalSnap(mRecyclerView.getLayoutManager(),
                        targetView);
                final int dx = snapDistances[0];
                final int dy = snapDistances[1];
                final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

            @Override
            protected int calculateTimeForScrolling(int dx) {
                return Math.min(MAX_SCROLL_ON_FLING_DURATION, super.calculateTimeForScrolling(dx));
            }

        };
    }

    private class ButtonPageScrollDecoration extends RecyclerView.ItemDecoration {
        int last_line_st = all_item - ((all_item - 1) % row) - 1;

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int position = parent.getChildAdapterPosition(view);
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (position >= last_line_st) { // 最后一列/列边距到末尾
                int line = (position % pageLimit) / row + 1;
                int all_line = pageLimit / row;

                assert layoutManager != null;
                if (layoutManager.canScrollHorizontally())
                    outRect.set(outRect.left, outRect.top, outRect.right + parent.getWidth() / all_line * (all_line - line), outRect.bottom);
                if (layoutManager.canScrollVertically())
                    outRect.set(outRect.left, outRect.top, outRect.right, outRect.bottom + parent.getHeight() / all_line * (all_line - line));
            }

        }
    }

    public int getCurrentPageIndex() {
        if (mRecyclerView.getLayoutManager().canScrollHorizontally()) {
            return currentPositionHorizontal / pageLimit;
        } else {
            return currentPositionVertical / pageLimit;
        }
    }

    public int getPageCount() {
        if (all_item % pageLimit == 0) return all_item / pageLimit;
        return all_item / pageLimit + 1;
    }

    /**
     * 滑动至特定页面
     *
     * @param page_index 页面的索引 start from 0
     * @return success(true) or not(false)
     */
    public boolean smoothScrollToPage(int page_index) {
        Log.i(TAG, String.format("scrollToPage: %d", page_index));
        if (page_index <= all_item / pageLimit) {
            if (mRecyclerView.getLayoutManager().canScrollHorizontally()) {
                mRecyclerView.smoothScrollBy(mRecyclerView.getLayoutManager().getWidth()
                        * (page_index - currentPositionHorizontal / pageLimit), 0);
                currentPositionHorizontal = page_index * pageLimit;
            } else {
                mRecyclerView.smoothScrollBy(0, mRecyclerView.getLayoutManager().getHeight()
                        * (page_index - currentPositionVertical / pageLimit));
                currentPositionVertical = page_index * pageLimit;
            }
            return true;
        } else return false;
    }

    public boolean scrollToPage(int page_index) {
        Log.i(TAG, String.format("scrollToPage: %d", page_index));
        if (page_index <= all_item / pageLimit) {
            if (mRecyclerView.getLayoutManager().canScrollHorizontally()) {
                mRecyclerView.scrollBy(mRecyclerView.getLayoutManager().getWidth()
                        * (page_index - currentPositionHorizontal / pageLimit), 0);
                currentPositionHorizontal = page_index * pageLimit;
            } else {
                mRecyclerView.scrollBy(0, mRecyclerView.getLayoutManager().getHeight()
                        * (page_index - currentPositionVertical / pageLimit));
                currentPositionVertical = page_index * pageLimit;
            }
            return true;
        } else return false;
    }

}
