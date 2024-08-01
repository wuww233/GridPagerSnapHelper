package com.www233.gridpagersnaphelper;

import android.graphics.Rect;
import android.os.Trace;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.Objects;

public class GridPagerSnapHelper extends SnapHelper {
    private static final String TAG = "GridPageSnapHelper";
    private int currentPositionVertical = 0, currentPositionHorizontal = 0; // 记录当前页的首个index
    private final int row, page_limit;  // row限制行数/列数, page_limit每页最大数量
    private int all_item = 0;   // 控件总数
    private OrientationHelper mHorizontalHelper, mVerticalHelper;
    RecyclerView mRecyclerView;
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
     * @param page_limit 每一页最多有多少控件
     */
    public GridPagerSnapHelper(int row, int page_limit) {
        this.row = row;
        this.page_limit = page_limit;
    }

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        super.attachToRecyclerView(recyclerView);
        if (recyclerView != null) {
            all_item = Objects.requireNonNull(recyclerView.getAdapter()).getItemCount();
            mRecyclerView = recyclerView;
            recyclerView.addItemDecoration(new ButtonPageScrollDecoration());
        }
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
        if (Math.abs(targetPosition - currentPosition) >= page_limit) {
            // 目标view已加载出来，不用再计算移动距离而是可以直接获取: 滑动时会调用findTargetSnapPosition()直接得到targetView
            result = helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
            currentPageStart = targetPosition;
        } else {    // 移动半块及以上时会滑动页面: 拖动时只会调用findSnapView()从而获取到距离当前页面最左边最近的view
            int dis = Math.abs(helper.getDecoratedStart(targetView) - helper.getStartAfterPadding());

            if (targetPosition < currentPosition - row
                    || dis <= (helper.getDecoratedMeasurement(targetView) / 2) && targetPosition < currentPosition) {
                // 左移且移动半块以上
                currentPageStart = currentPosition - page_limit;

            } else if (targetPosition >= currentPosition + row
                    || dis >= (helper.getDecoratedMeasurement(targetView) / 2) && targetPosition >= currentPosition) {
                // 右移且移动半块以上
                currentPageStart = currentPosition + page_limit;
            }

            int columnWidth = helper.getDecoratedMeasurement(targetView);
            int distance = ((targetPosition - currentPageStart) / row) * columnWidth;
            final int childStart = helper.getDecoratedStart(targetView);

            result = childStart - distance;
        }

        if (direction == Direction.HORIZONTAL && currentPageStart != currentPositionHorizontal) {
            onPageChangeListener.onChange(currentPositionHorizontal / page_limit,
                    currentPageStart / page_limit);
            currentPositionHorizontal = currentPageStart;
        } else if (direction == Direction.VERTICAL && currentPageStart != currentPositionVertical) {
            onPageChangeListener.onChange(currentPositionVertical / page_limit,
                    currentPageStart / page_limit);
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
            if (currentPosition + page_limit > all_item)
                return currentPosition;
            else
                return currentPosition + page_limit;

        } else if (velocity < 0) // 向左滑动
        {
            if (currentPosition - page_limit < 0)
                return currentPosition;
            else
                return currentPosition - page_limit;
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
                int line = (position % page_limit) / row + 1;
                int all_line = page_limit / row;

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
            return currentPositionHorizontal / page_limit;
        } else {
            return currentPositionVertical / page_limit;
        }
    }

    public int getPageCount() {
        return all_item / page_limit + 1;
    }

    /**
     * 滑动至特定页面
     *
     * @param page_index 页面的索引 start from 0
     * @return success(true) or not(false)
     */
    public boolean smoothScrollToPage(int page_index) {
        Log.i(TAG, String.format("scrollToPage: %d", page_index));
        if (page_index <= all_item / page_limit) {
            if (mRecyclerView.getLayoutManager().canScrollHorizontally()) {
                mRecyclerView.smoothScrollBy(mRecyclerView.getLayoutManager().getWidth()
                        * (page_index - currentPositionHorizontal / page_limit), 0);
                currentPositionHorizontal = page_index * page_limit;
            } else {
                mRecyclerView.smoothScrollBy(0, mRecyclerView.getLayoutManager().getHeight()
                        * (page_index - currentPositionVertical / page_limit));
                currentPositionVertical = page_index * page_limit;
            }
            return true;
        } else return false;
    }
    public boolean scrollToPage(int page_index) {
        Log.i(TAG, String.format("scrollToPage: %d", page_index));
        if (page_index <= all_item / page_limit) {
            if (mRecyclerView.getLayoutManager().canScrollHorizontally()) {
                mRecyclerView.scrollBy(mRecyclerView.getLayoutManager().getWidth()
                        * (page_index - currentPositionHorizontal / page_limit), 0);
                currentPositionHorizontal = page_index * page_limit;
            } else {
                mRecyclerView.scrollBy(0, mRecyclerView.getLayoutManager().getHeight()
                        * (page_index - currentPositionVertical / page_limit));
                currentPositionVertical = page_index * page_limit;
            }
            return true;
        } else return false;
    }

}
