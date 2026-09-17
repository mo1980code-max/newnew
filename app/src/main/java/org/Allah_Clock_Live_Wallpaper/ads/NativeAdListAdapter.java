package org.Allah_Clock_Live_Wallpaper.ads;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.nativead.NativeAd;

import org.Allah_Clock_Live_Wallpaper.R;

/**
 * Wraps any existing {@link RecyclerView.Adapter} and injects a single native-ad card in
 * the middle of the list, spanning the whole grid width.
 *
 * <p>The wrapped adapter keeps working exactly as before — positions are transparently
 * remapped, so click callbacks still receive the original list index. Nothing is inserted
 * when consent is missing, when the list is too short, or when no ad fills.</p>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class NativeAdListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** Large enough never to collide with a view type returned by a wrapped adapter. */
    public static final int VIEW_TYPE_NATIVE_AD = 98765001;

    @NonNull
    private final Activity activity;
    @NonNull
    private final RecyclerView.Adapter inner;
    private final int spanCount;

    private NativeAd nativeAd;
    private int adPosition = RecyclerView.NO_POSITION;
    private boolean loadAttempted;

    private final RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyDataSetChanged();
        }
    };

    public NativeAdListAdapter(@NonNull Activity activity,
                               @NonNull RecyclerView.Adapter inner,
                               int spanCount) {
        this.activity = activity;
        this.inner = inner;
        this.spanCount = Math.max(1, spanCount);
        inner.registerAdapterDataObserver(observer);
    }

    /** Installs a {@link GridLayoutManager} whose ad row spans every column. */
    public void attachTo(@NonNull RecyclerView recyclerView) {
        GridLayoutManager manager = new GridLayoutManager(recyclerView.getContext(), spanCount);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return isAdPosition(position) ? spanCount : 1;
            }
        });
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(this);
    }

    /** Requests the native card. One attempt per list; a failure simply leaves the list ad-free. */
    public void loadNativeAd() {
        if (loadAttempted || nativeAd != null) {
            return;
        }
        if (inner.getItemCount() < AdConfig.NATIVE_AD_MIN_LIST_SIZE) {
            return;
        }
        if (!AdManager.canRequestAds()) {
            return;
        }
        loadAttempted = true;
        AdManager.loadNative(activity, new AdManager.NativeAdCallback() {
            @Override
            public void onNativeAdLoaded(@NonNull NativeAd ad) {
                releaseAd();
                nativeAd = ad;
                adPosition = computeAdPosition(inner.getItemCount());
                if (adPosition != RecyclerView.NO_POSITION) {
                    notifyItemInserted(adPosition);
                }
            }

            @Override
            public void onNativeAdFailed() {
                // leave the list untouched
            }
        });
    }

    /** Must be called from the host Activity's {@code onDestroy}. */
    public void destroy() {
        try {
            inner.unregisterAdapterDataObserver(observer);
        } catch (Throwable ignored) {
        }
        releaseAd();
    }

    private void releaseAd() {
        if (nativeAd != null) {
            nativeAd.destroy();
            nativeAd = null;
        }
        adPosition = RecyclerView.NO_POSITION;
    }

    private int computeAdPosition(int contentCount) {
        if (contentCount <= 0) {
            return RecyclerView.NO_POSITION;
        }
        int position = Math.round(contentCount * AdConfig.NATIVE_AD_POSITION_RATIO);
        return Math.max(0, Math.min(position, contentCount));
    }

    private boolean isAdPosition(int position) {
        return nativeAd != null && position == adPosition;
    }

    /** Outer (ad-aware) position → position inside the wrapped adapter. */
    private int toInnerPosition(int position) {
        if (nativeAd != null && position > adPosition) {
            return position - 1;
        }
        return position;
    }

    @Override
    public int getItemCount() {
        return inner.getItemCount() + (nativeAd != null ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (isAdPosition(position)) {
            return VIEW_TYPE_NATIVE_AD;
        }
        return inner.getItemViewType(toInnerPosition(position));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_NATIVE_AD) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_native_ad, parent, false);
            return new NativeAdViewHolder(view);
        }
        return inner.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isAdPosition(position)) {
            ((NativeAdViewHolder) holder).bind(nativeAd);
            return;
        }
        inner.onBindViewHolder(holder, toInnerPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return inner.getItemId(toInnerPosition(position));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        inner.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        inner.onDetachedFromRecyclerView(recyclerView);
    }

    /**
     * The ad card is ours, not the inner adapter's: forwarding its holder would let an
     * inner implementation cast it to its own ViewHolder type and crash. Content holders
     * (positions that are not the ad) are still forwarded untouched.
     */
    private boolean isOwnAdHolder(@NonNull RecyclerView.ViewHolder holder) {
        return holder instanceof NativeAdViewHolder;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (!isOwnAdHolder(holder)) {
            inner.onViewAttachedToWindow(holder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (!isOwnAdHolder(holder)) {
            inner.onViewDetachedFromWindow(holder);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (!isOwnAdHolder(holder)) {
            inner.onViewRecycled(holder);
        }
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        if (isOwnAdHolder(holder)) {
            return false;
        }
        return inner.onFailedToRecycleView(holder);
    }
}
